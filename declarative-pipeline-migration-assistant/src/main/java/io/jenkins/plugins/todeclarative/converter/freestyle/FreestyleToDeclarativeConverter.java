package io.jenkins.plugins.todeclarative.converter.freestyle;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Describable;
import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.scm.SCM;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.BaseConverter;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import io.jenkins.plugins.todeclarative.converter.builder.NoBuilderConverter;
import io.jenkins.plugins.todeclarative.converter.publisher.NoPublisherConverter;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Extension
public class FreestyleToDeclarativeConverter extends SingleTypedConverter<FreeStyleProject>
{
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) throws ConverterException {
        FreeStyleProject freeStyleProject = (FreeStyleProject) target;

        {
            // manage general warning
            if(freeStyleProject.isDisabled()){
                result.addWarning( new Warning( "Current Freestyle project is disable", null ) );
            }
            if(!freeStyleProject.isConcurrentBuild()){
                result.addWarning( new Warning( "Current Freestyle project is marked as no concurrent build but it is default option for pipeline", null ) );
            }
        }

        ExtensionList<BaseConverter> converterList = BaseConverter.all();
        convertTypeList(request, result, converterList, freeStyleProject.getBuildWrappersList());

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

            result.getModelASTPipelineDef().setAgent( agent );
        }

        { // scm
            SCM scm = freeStyleProject.getScm();
            if ( scm != null )
            {
                convertTypeList(request, result, converterList, Arrays.asList(scm));
            }
        }

        convertTypeList(request, result, converterList, freeStyleProject.getTriggers().values());
        Map<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> projectProperties = freeStyleProject.getProperties();
        // ignore JiraProjectProperty as it is not removed when removing jira publishers so we avoid false positive
        projectProperties.entrySet().removeIf(entry -> entry.getKey().clazz.getName().equals("hudson.plugins.jira.JiraProjectProperty"));
        convertTypeList(request, result, converterList, projectProperties.values());
        convertTypeList(request, result, converterList, freeStyleProject.getBuilders());
        convertTypeList(request, result, converterList, freeStyleProject.getPublishersList());
        return true;
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
                        result.addConvertedType(type.getClass());
                    } else
                    {
                        // TODO: log addition information for failed conversion?
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
}
