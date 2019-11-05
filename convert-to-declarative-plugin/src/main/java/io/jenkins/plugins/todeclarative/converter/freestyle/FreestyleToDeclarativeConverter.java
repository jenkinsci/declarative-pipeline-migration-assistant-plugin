package io.jenkins.plugins.todeclarative.converter.freestyle;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.scm.SCM;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import io.jenkins.plugins.todeclarative.converter.api.builder.BuilderConverter;
import io.jenkins.plugins.todeclarative.converter.api.buildwrapper.BuildWrapperConverter;
import io.jenkins.plugins.todeclarative.converter.api.jobproperty.JobPropertyConverter;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import io.jenkins.plugins.todeclarative.converter.api.scm.ScmConverter;
import io.jenkins.plugins.todeclarative.converter.builder.NoConverterBuilder;
import io.jenkins.plugins.todeclarative.converter.publisher.NoPublisherConverter;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
                converterResult.addWarning( new Warning( "Current Freestyle project is disable", null ) );
            }
            if(!freeStyleProject.isConcurrentBuild()){
                converterResult.addWarning( new Warning( "Current Freestyle project is marked as no concurrent build but it is default option for pipeline", null ) );
            }
        }

        convertBuildWrappers( converterRequest, converterResult, freeStyleProject.getBuildWrappersList() );

        { // label
            String label = freeStyleProject.getAssignedLabelString();
            if ( StringUtils.isNotBlank(label) )
            {
                ModelASTAgent agent = new ModelASTAgent( this );
                ModelASTKey agentType = new ModelASTKey( this );

                // to avoid NPE in ModelASTAgent...
                agentType.setKey( "{ label '"+ label +"' }" );
                agent.setAgentType( agentType );
                converterResult.getModelASTPipelineDef().setAgent( agent );
            }
            else
            {
                ModelASTAgent agent = new ModelASTAgent( this );
                ModelASTKey agentKey = new ModelASTKey( this );
                agentKey.setKey( "any" );

                agent.setAgentType( agentKey );
                converterResult.getModelASTPipelineDef().setAgent( agent );
            }
        }

        { // scm
            SCM scm = freeStyleProject.getScm();
            if ( scm != null )
            {
                convertScm( converterRequest, converterResult, scm );
            }
        }
        convertJobProperties( converterRequest, converterResult, freeStyleProject.getProperties() );

        convertBuilders( converterRequest, converterResult, freeStyleProject.getBuilders() );

        convertPublishers( converterRequest, converterResult, freeStyleProject.getPublishersList() );

    }

    protected void convertBuildWrappers( ConverterRequest converterRequest, ConverterResult converterResult,
                                         List<BuildWrapper> wrappers )
        throws ConverterException
    {
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
                    ModelASTStage stage =
                        buildWrapperConverterConverter.convert( converterRequest, converterResult, wrapper );
                    if ( stage != null )
                    {
                        converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                    }
                } );
            }
            else
            {
                converterResult.addWarning( new Warning( "Converter not found", wrapper.getClass().getName() ) );
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
                converters.stream().forEach( converter -> {
                    ModelASTStage stage = converter.convert( converterRequest, converterResult, publisher );
                    if ( stage != null )
                    {
                        converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                    }
                } );
            }
            else
            {
                converterResult.addWarning(
                    new Warning( "Converter not found", publisher.getClass().getName() ) );
                // add fake post with commented step named with the plugin class name
                Jenkins.get().getExtensionList( NoPublisherConverter.class ).iterator()
                    .next().convert( converterRequest, converterResult, publisher );
            }
        }
    }

    protected void convertScm( ConverterRequest converterRequest, ConverterResult converterResult, SCM scm )
        throws ConverterException
    {
        List<ScmConverter> converters = findBuildScmConverters( scm );
        if ( !converters.isEmpty() )
        {
            converters.stream() //
                .forEach( scmConverter -> scmConverter.convert( converterRequest, converterResult, scm ) );
        }
        else
        {
            converterResult.addWarning( new Warning( "SCM Converter not found", scm.getClass().getName() ) );
        }
    }

    protected void convertBuilders( ConverterRequest converterRequest, ConverterResult converterResult,
                                    List<Builder> builders )
        throws ConverterException
    {
        ModelASTStages stages = converterResult.getModelASTPipelineDef().getStages();
        if ( stages == null )
        {
            stages = new ModelASTStages( this );
            converterResult.getModelASTPipelineDef().setStages( stages );
        }
        for ( Builder builder : builders )
        {
            Consumer<? super BuilderConverter> consumer = builderConverter -> {
                ModelASTStage stage = builderConverter.convert( converterRequest, converterResult, builder );
                if ( stage != null )
                {
                    converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                }
            };
            if ( builder instanceof Maven ) // Maven is a special one and we can apply only one converter so we pick the first one
            {
                List<BuilderConverter> builderConverters = findBuilderConverters( builder );
                if ( !builderConverters.isEmpty() )
                {
                    builderConverters.subList( 0, 1 ).stream().forEach( consumer );
                }
            }
            else
            {
                List<BuilderConverter> converters = findBuilderConverters( builder );
                if ( !converters.isEmpty() )
                {
                    converters.stream().forEach( consumer );
                }
                else
                {
                    converterResult.addWarning(
                        new Warning( "Builder Converter not found", builder.getClass().getName() ) );
                    // add fake stage with commented step named with the plugin class name
                    ModelASTStage stage = Jenkins.get().getExtensionList( NoConverterBuilder.class ).iterator()
                        .next().convert( converterRequest, converterResult, builder );
                    converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                }
            }
        }
    }

    protected void convertJobProperties( ConverterRequest converterRequest, ConverterResult converterResult,
                                         Map<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> map )
    {

        for ( Map.Entry<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> entry : map.entrySet() )
        {
            List<JobPropertyConverter> converters = findJobPropertyConverters( entry.getKey(), entry.getValue() );
            // if any special converters we used them otherwise simply copy the property
            if ( !converters.isEmpty() )
            {
                converters.stream().forEach(
                    jobPropertyConverter -> jobPropertyConverter.convert( converterRequest, converterResult, //
                                                                          entry.getKey(), entry.getValue() )

                );
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
                    new Warning( "Converter not found", entry.getKey().getClass().getName() ) );

            }
        }

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
