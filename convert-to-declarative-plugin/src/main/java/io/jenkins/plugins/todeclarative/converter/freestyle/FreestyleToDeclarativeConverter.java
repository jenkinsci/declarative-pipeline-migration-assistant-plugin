package io.jenkins.plugins.todeclarative.converter.freestyle;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Label;
import hudson.scm.SCM;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.api.builder.BuilderConverter;
import io.jenkins.plugins.todeclarative.converter.api.buildwrapper.BuildWrapperConverter;
import io.jenkins.plugins.todeclarative.converter.api.jobproperty.JobPropertyConverter;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import io.jenkins.plugins.todeclarative.converter.api.scm.ScmConverter;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;
import java.util.ArrayList;
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
        try
        {
            FreeStyleProject freeStyleProject = (FreeStyleProject) converterRequest.getJob();
            if ( converterRequest.isCreateProject() )
            {
                WorkflowJob workflowJob = Jenkins.get().createProject( WorkflowJob.class,
                                                                       // FIXME find something better than foo as default name :)
                                                                       converterRequest.getCreatedProjectName() == null
                                                                           ? "foo"
                                                                           : converterRequest.getCreatedProjectName() );
                converterResult.setJob( workflowJob );

                workflowJob.makeDisabled( freeStyleProject.isDisabled() );
                workflowJob.setQuietPeriod( freeStyleProject.getQuietPeriod() );
                workflowJob.setDescription( freeStyleProject.getDescription() );
                if ( !freeStyleProject.getTriggers().isEmpty() )
                {
                    workflowJob.setTriggers( new ArrayList<>( freeStyleProject.getTriggers().values() ) );
                }
            }

            convertBuildWrappers( converterRequest, converterResult, freeStyleProject.getBuildWrappersList() );

            { // label
                Label label = freeStyleProject.getAssignedLabel();
                if ( label != null )
                {
                    ModelASTAgent agent = new ModelASTAgent( this );
                    ModelASTKey agentKey = new ModelASTKey( this );
                    agentKey.setKey( "'" + label.getName() + "'" );

                    agent.setAgentType( agentKey );
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

            if ( converterRequest.isCreateProject() )
            {

                String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();
                // FIXME make sandbox configurable
                ( (WorkflowJob) converterResult.getJob() ).setDefinition( new CpsFlowDefinition( groovy, true ) );
                converterResult.getJob().save();
            }

        }
        catch ( IOException e )
        {
            throw new ConverterException( e.getMessage(), e );
        }
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
            findBuildWrapperConverter( wrapper ).stream().forEach( buildWrapperConverterConverter -> {
                ModelASTStage stage =
                    buildWrapperConverterConverter.convert( converterRequest, converterResult, wrapper );
                if ( stage != null )
                {
                    converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                }
            } );
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
        List<PublisherConverter> converters = Jenkins.get().getExtensionList( PublisherConverter.class );
        if ( converters == null || converters.isEmpty() )
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
            converters.stream().filter( converter -> converter.canConvert( publisher ) ).forEach( converter -> {
                ModelASTStage stage = converter.convert( converterRequest, converterResult, publisher );
                if ( stage != null )
                {
                    converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                }
            } );
        }
    }

    protected void convertScm( ConverterRequest converterRequest, ConverterResult converterResult, SCM scm )
        throws ConverterException
    {
        List<ScmConverter> converters = Jenkins.get().getExtensionList( ScmConverter.class );
        if ( converters == null || converters.isEmpty() )
        {
            return;
        }
        converters.stream() //
            .filter( scmConverter -> scmConverter.canConvert( scm ) ) //
            .forEach( scmConverter -> scmConverter.convert( converterRequest, converterResult, scm ) );
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
            findBuilderConverter( builder ).stream().forEach( builderConverter -> {
                ModelASTStage stage = builderConverter.convert( converterRequest, converterResult, builder );
                if ( stage != null )
                {
                    converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                }
            } );
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
                    jobPropertyConverter -> jobPropertyConverter.doConvert( converterRequest, converterResult, //
                                                                            entry.getKey(), entry.getValue() )

                );
            }
            else
            {
                if ( converterRequest.isCreateProject() )
                {
                    try
                    {
                        converterResult.getJob().addProperty( entry.getValue() );
                    }
                    catch ( IOException e )
                    {
                        // FIXME better exception handling here
                        e.printStackTrace();
                    }
                }
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

    protected List<BuilderConverter> findBuilderConverter( Builder builder )
    {

        List<BuilderConverter> converters = Jenkins.get().getExtensionList( BuilderConverter.class );
        return converters.stream().filter( converter -> converter.canConvert( builder ) ).collect(
            Collectors.toList() );
    }

    protected List<BuildWrapperConverter> findBuildWrapperConverter( BuildWrapper wrapper )
    {

        List<BuildWrapperConverter> converters = Jenkins.get().getExtensionList( BuildWrapperConverter.class );
        return converters.stream().filter( converter -> converter.canConvert( wrapper ) ).collect(
            Collectors.toList() );
    }

    @Override
    public boolean canConvert( Job job )
    {
        return job instanceof FreeStyleProject;
    }
}
