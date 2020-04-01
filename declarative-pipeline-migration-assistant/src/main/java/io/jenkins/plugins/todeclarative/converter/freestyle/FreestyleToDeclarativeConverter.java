package io.jenkins.plugins.todeclarative.converter.freestyle;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Describable;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.scm.SCM;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.todeclarative.converter.api.BaseConverter;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import io.jenkins.plugins.todeclarative.converter.api.ToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import io.jenkins.plugins.todeclarative.converter.api.builder.BuilderConverter;
import io.jenkins.plugins.todeclarative.converter.api.buildwrapper.BuildWrapperConverter;
import io.jenkins.plugins.todeclarative.converter.api.jobproperty.JobPropertyConverter;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import io.jenkins.plugins.todeclarative.converter.api.scm.ScmConverter;
import io.jenkins.plugins.todeclarative.converter.api.trigger.TriggerConverter;
import io.jenkins.plugins.todeclarative.converter.builder.NoBuilderConverter;
import io.jenkins.plugins.todeclarative.converter.publisher.NoPublisherConverter;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Extension
public class FreestyleToDeclarativeConverter
    implements ToDeclarativeConverter
{
    @Override
    public void convert( ConverterRequest converterRequest, ConverterResult converterResult )
        throws ConverterException
    {

        FreeStyleProject freeStyleProject = (FreeStyleProject) converterRequest.getJob();

        {
            // manage general warning
            if(freeStyleProject.isDisabled()){
                converterResult.addWarning( new Warning( "Current Freestyle project is disable", freeStyleProject.getClass() ) );
            }
            if(!freeStyleProject.isConcurrentBuild()){
                converterResult.addWarning( new Warning( "Current Freestyle project is marked as no concurrent build but it is default option for pipeline", freeStyleProject.getClass() ) );
            }
        }

        ExtensionList<BaseConverter> converterList = BaseConverter.all();
        convertTypeList(converterRequest, converterResult, converterList, freeStyleProject.getBuildWrappersList());

        { // label customWorkspace etc...
            String label = freeStyleProject.getAssignedLabelString();
            ModelASTAgent agent = new ModelASTAgent( this );

            String customWorkspace = freeStyleProject.getCustomWorkspace();

            if(StringUtils.isBlank( label ) && StringUtils.isBlank( customWorkspace )) {
                ModelASTKey agentKey = new ModelASTKey( this );
                agentKey.setKey( "any" );
                agent.setAgentType( agentKey );
            } else
            {

                ModelASTKey agentType = new ModelASTKey( this ){
                    // so ugly to avoid NPE in ModelASTAgent...
                    @Override
                    public String toGroovy()
                    {
                        StringBuilder groovy = new StringBuilder( "{\n node { \n" );
                        if(StringUtils.isNotBlank( label )){
                            groovy.append( "    label '" + label + "'\n" );
                        } else {
                            groovy.append( "    label ''\n" );
                        }
                        if(StringUtils.isNotBlank( customWorkspace )){
                            groovy.append( "    customWorkspace \"" + customWorkspace + "\"\n" );
                        }
                        groovy.append( "    } \n}" );
                        return groovy.toString();
                    }
                };

                agent.setAgentType( agentType );

            }

            converterResult.getModelASTPipelineDef().setAgent( agent );
        }

        { // scm
            SCM scm = freeStyleProject.getScm();
            if ( scm != null )
            {
                convertTypeList(converterRequest, converterResult, converterList, Arrays.asList(scm));
            }
        }

        convertTypeList(converterRequest, converterResult, converterList, freeStyleProject.getTriggers().values());
        Map<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> projectProperties = freeStyleProject.getProperties();
        // ignore JiraProjectProperty as it is not removed when removing jira publishers so we avoid false positive
        projectProperties.entrySet().removeIf(entry -> entry.getKey().clazz.getName().equals("hudson.plugins.jira.JiraProjectProperty"));
        convertTypeList(converterRequest, converterResult, converterList, projectProperties.values());
        convertTypeList(converterRequest, converterResult, converterList, freeStyleProject.getBuilders());
        convertTypeList(converterRequest, converterResult, converterList, freeStyleProject.getPublishersList());
    }

    protected <T> void convertTypeList(ConverterRequest request, ConverterResult result, ExtensionList<BaseConverter> converterList, Collection<T> typeList) throws ConverterException {
        ModelASTStages stages = result.getModelASTPipelineDef().getStages();
        if ( stages == null )
        {
            stages = new ModelASTStages( this );
            result.getModelASTPipelineDef().setStages( stages );
        }
        for ( T type : typeList )
        {
            boolean matched = false;
            for ( BaseConverter converter : converterList )
            {
                if ( converter.canConvert(type) )
                {
                    matched = true;
                    if ( converter.convert(request, result, type) )
                    {
                        result.addConvertedType(type.getClass().getName());
                    } else
                    {
                        // TODO add to failed conversion, but get more detailed info?
                    }
                    if ( type instanceof Maven ) {
                        // Maven is a special one and we can apply only one converter so we pick the first one
                        break;
                    }
                }
            }
            if ( !matched )
            {
                result.addWarning( new Warning( "Converter not found '" + getDisplayName((Describable) type) + "'", type.getClass() ) );
                SingleTypedConverter noConverter = null;
                if ( type instanceof Builder )
                {
                    noConverter = Jenkins.get().getExtensionList( NoBuilderConverter.class ).get(0);
                } else if ( type instanceof Publisher )
                {
                    noConverter = Jenkins.get().getExtensionList( NoPublisherConverter.class ).get(0);
                }
                if ( noConverter != null )
                {
                    noConverter.convert( request, result,type );
                }
                // TODO add to failed conversion, but get more detailed info?
            }
        }
    }

    protected void convertBuildTriggers( ConverterRequest converterRequest, ConverterResult converterResult,
                                         Map<TriggerDescriptor, Trigger<?>> triggers )
        throws ConverterException
    {

        List<Warning> warnings = converterResult.getWarnings();
        for ( Map.Entry<TriggerDescriptor, Trigger<?>> entry : triggers.entrySet() )
        {
            List<TriggerConverter> converters = findTriggerConverters( entry.getKey(), entry.getValue() );
            if ( !converters.isEmpty() )
            {
                converters.stream().forEach( triggerConverter -> {
                    int numWarnings = warnings.size();
                    triggerConverter.convert(converterRequest, converterResult, entry.getKey(), entry.getValue());
                    if (warnings.size() == numWarnings) {
                        converterResult.addConvertedType(triggerConverter.getClass().getName());
                    }
                        }
                );
            }
            else
            {
                converterResult.addWarning( new Warning( "Converter not found '" +
                                                             getDisplayName(entry.getValue()) +
                                                             "'",
                                                         entry.getValue().getClass() ) );
            }
        }
    }

    protected void convertBuildWrappers( ConverterRequest converterRequest, ConverterResult converterResult,
                                         List<BuildWrapper> wrappers )
        throws ConverterException
    {
        List<Warning> warnings = converterResult.getWarnings();
        ModelASTStages stages = converterResult.getModelASTPipelineDef().getStages();
        if ( stages == null )
        {
            stages = new ModelASTStages( this );
            converterResult.getModelASTPipelineDef().setStages( stages );
        }
        for ( BuildWrapper wrapper : wrappers )
        {
            List<BuildWrapperConverter> converters = findBuildWrapperConverters( wrapper );
            if ( !converters.isEmpty() )
            {
                converters.stream().forEach( buildWrapperConverterConverter -> {
                    int numWarnings = warnings.size();
                    ModelASTStage stage =
                        buildWrapperConverterConverter.convert( converterRequest, converterResult, wrapper );
                    if ( stage != null )
                    {
                        ModelASTUtils.addStage(converterResult.getModelASTPipelineDef(), stage);
                    }
                    if (warnings.size() == numWarnings) {
                        // TODO: plugin -> converted type
                        converterResult.addConvertedType(buildWrapperConverterConverter.getClass().getName());
                    }
                } );
            }
            else
            {
                converterResult.addWarning( new Warning( "Converter not found '" + getDisplayName(wrapper) + "'", wrapper.getClass() ) );
            }
        }
    }

    protected void convertPublishers( ConverterRequest converterRequest, ConverterResult converterResult,
                                      List<Publisher> publishers )
        throws ConverterException
    {
        if ( publishers == null || publishers.isEmpty() )
        {
            return;
        }

        List<Warning> warnings = converterResult.getWarnings();
        ModelASTStages stages = converterResult.getModelASTPipelineDef().getStages();
        if ( stages == null )
        {
            stages = new ModelASTStages( this );
            converterResult.getModelASTPipelineDef().setStages( stages );
        }
        for ( Publisher publisher : publishers )
        {
            List<PublisherConverter> converters = findPublisherConverters( publisher );
            if ( !converters.isEmpty() )
            {
                converters.stream().forEach( publisherConverter -> {
                    int numWarnings = warnings.size();
                    ModelASTStage stage = publisherConverter.convert( converterRequest, converterResult, publisher );
                    if ( stage != null )
                    {
                        ModelASTUtils.addStage(converterResult.getModelASTPipelineDef(), stage);
                    }
                    if (warnings.size() == numWarnings) {
                        converterResult.addConvertedType(publisherConverter.getClass().getName());
                    }
                } );
            }
            else
            {
                converterResult.addWarning(
                    new Warning( "Converter not found '" + getDisplayName(publisher) + "'", publisher.getClass() ) );
                // add fake post with commented step named with the plugin class name
                Jenkins.get().getExtensionList( NoPublisherConverter.class ).iterator()
                    .next().convert( converterRequest, converterResult, publisher );
            }
        }
    }

    /**
     *
     * @param describable the {@link Describable} instance to find displayName
     * @return the displayName or empty String
     */
    private String getDisplayName( Describable describable ) {
        try
        {
            return describable.getDescriptor() == null ? "" : describable.getDescriptor().getDisplayName();
        }
        catch ( Throwable e )
        {
            return "";
        }
    }

    protected void convertScm( ConverterRequest converterRequest, ConverterResult converterResult, SCM scm )
        throws ConverterException
    {
        List<ScmConverter> converters = findBuildScmConverters( scm );
        if ( !converters.isEmpty() )
        {
            List<Warning> warnings = converterResult.getWarnings();
            converters.stream().forEach( scmConverter -> {
                int numWarnings = warnings.size();
                scmConverter.convert( converterRequest, converterResult, scm );
                if (warnings.size() == numWarnings) {
                    converterResult.addConvertedType(scmConverter.getClass().getName());
                }
            } );
        }
        else
        {
            converterResult.addWarning( new Warning( "SCM Converter not found '" + getDisplayName(scm) + "'", scm.getClass() ) );
        }
    }

//    protected void convertBuilders( ConverterRequest converterRequest, ConverterResult converterResult,
//                                    List<Builder> builders )
//        throws ConverterException
//    {
//        List<Warning> warnings = converterResult.getWarnings();
//        ModelASTStages stages = converterResult.getModelASTPipelineDef().getStages();
//        if ( stages == null )
//        {
//            stages = new ModelASTStages( this );
//            converterResult.getModelASTPipelineDef().setStages( stages );
//        }
//        for ( Builder builder : builders )
//        {
//            Consumer<? super BuilderConverter> consumer = builderConverter -> {
//                int  numWarnings = warnings.size();
//                ModelASTStage stage = builderConverter.convert( converterRequest, converterResult, builder );
//                if ( stage != null )
//                {
//                    ModelASTUtils.addStage(converterResult.getModelASTPipelineDef(), stage);
//                }
//                if (warnings.size() == numWarnings) {
//                    converterResult.addConvertedType(builderConverter.getClass().getName());
//                }
//            };
//            if ( builder instanceof Maven ) // Maven is a special one and we can apply only one converter so we pick the first one
//            {
//                List<BuilderConverter> builderConverters = findBuilderConverters( builder );
//                if ( !builderConverters.isEmpty() )
//                {
//                    builderConverters.subList( 0, 1 ).stream().forEach( consumer );
//                }
//            }
//            else
//            {
//                List<BuilderConverter> converters = findBuilderConverters( builder );
//                if ( !converters.isEmpty() )
//                {
//                    converters.stream().forEach( consumer );
//                }
//                else
//                {
//                    converterResult.addWarning(
//                        new Warning( "Builder Converter not found '" + getDisplayName(builder)+ "'", builder.getClass().getName() ) );
//                    // add fake stage with commented step named with the plugin class name
//                    ModelASTStage stage = Jenkins.get().getExtensionList( NoBuilderConverter.class ).iterator()
//                        .next().convert( converterRequest, converterResult, builder );
//                    ModelASTUtils.addStage(converterResult.getModelASTPipelineDef(), stage);
//                }
//            }
//        }
//    }

    protected void convertJobProperties( ConverterRequest converterRequest, ConverterResult converterResult,
                                         Map<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> map )
    {

        List<Warning> warnings = converterResult.getWarnings();
        for ( Map.Entry<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> entry : map.entrySet() )
        {
            List<JobPropertyConverter> converters = findJobPropertyConverters( entry.getKey(), entry.getValue() );
            // if any special converters we used them otherwise simply copy the property
            if ( !converters.isEmpty() )
            {
                converters.stream().forEach(jobPropertyConverter -> {
                    int numWarnings = warnings.size();
                    jobPropertyConverter.convert( converterRequest, converterResult, entry.getKey(), entry.getValue() );
                    if (warnings.size() == numWarnings) {
                        converterResult.addConvertedType(entry.getKey().getClass().getName());
                    }
                        });
            }
            else
            {
                if(entry.getKey().getClass().getName().startsWith( "hudson.plugins.jira.JiraProjectProperty" ))
                {
                    // we ignore this one as it is not removed when removing jira publishers
                    // so we avoid false positive
                    continue;
                }
                converterResult.addWarning(
                    new Warning( "Converter not found '" + entry.getKey().getDisplayName() + "'", entry.getKey().getClass() ) );

            }
        }

    }

    protected List<TriggerConverter> findTriggerConverters( TriggerDescriptor triggerDescriptor, Trigger<?> trigger )
    {
        List<TriggerConverter> converters = Jenkins.get().getExtensionList( TriggerConverter.class );
        return converters.stream().filter(
            converter -> converter.canConvert( triggerDescriptor, trigger ) ).collect( Collectors.toList() );
    }

    protected List<JobPropertyConverter> findJobPropertyConverters( JobPropertyDescriptor jobPropertyDescriptor,
                                                                    JobProperty jobProperty )
    {
        List<JobPropertyConverter> converters = Jenkins.get().getExtensionList( JobPropertyConverter.class );
        return converters.stream().filter(
            converter -> converter.canConvert( jobPropertyDescriptor, jobProperty ) ).collect( Collectors.toList() );
    }

    protected List<BuilderConverter> findBuilderConverters( Builder builder )
    {
        List<BuilderConverter> converters = Jenkins.get().getExtensionList( BuilderConverter.class );
        return converters.stream().filter( converter -> converter.canConvert( builder ) ).collect(
            Collectors.toList() );
    }

    protected List<PublisherConverter> findPublisherConverters( Publisher publisher )
    {
        List<PublisherConverter> converters = Jenkins.get().getExtensionList( PublisherConverter.class );
        return converters.stream().filter( converter -> converter.canConvert( publisher ) ).collect(
            Collectors.toList() );
    }

    protected List<BuildWrapperConverter> findBuildWrapperConverters( BuildWrapper wrapper )
    {
        List<BuildWrapperConverter> converters = Jenkins.get().getExtensionList( BuildWrapperConverter.class );
        return converters.stream().filter( converter -> converter.canConvert( wrapper ) ).collect(
            Collectors.toList() );
    }

    protected List<ScmConverter> findBuildScmConverters( SCM scm )
    {
        List<ScmConverter> converters = Jenkins.get().getExtensionList( ScmConverter.class );
        return converters.stream().filter( converter -> converter.canConvert( scm ) ).collect( Collectors.toList() );
    }


    @Override
    public boolean canConvert( Job job )
    {
        return job instanceof FreeStyleProject;
    }
}
