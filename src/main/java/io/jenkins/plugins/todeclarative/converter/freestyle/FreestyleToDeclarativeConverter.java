package io.jenkins.plugins.todeclarative.converter.freestyle;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Label;
import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.ConverterException;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.ToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.builders.BuilderConverter;
import io.jenkins.plugins.todeclarative.converter.jobproperty.JobPropertyConverter;
import jenkins.model.Jenkins;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Extension
public class FreestyleToDeclarativeConverter implements ToDeclarativeConverter
{
    @Override
    public void convert( ConverterRequest converterRequest, ConverterResult converterResult ) throws ConverterException
    {
        try
        {
            FreeStyleProject freeStyleProject = (FreeStyleProject) converterRequest.getJob();
            if(converterRequest.isCreateProject()) {
                WorkflowJob workflowJob = Jenkins.get().createProject( WorkflowJob.class,
                                                                       // FIXME find something better than foo as default name :)
                                                                       converterRequest.getCreatedProjectName()==null?"foo":converterRequest.getCreatedProjectName() );
                converterResult.setJob( workflowJob );

                workflowJob.makeDisabled(freeStyleProject.isDisabled());
                workflowJob.setQuietPeriod( freeStyleProject.getQuietPeriod() );
                workflowJob.setTriggers( new ArrayList<>(freeStyleProject.getTriggers().values()));
                workflowJob.setDescription( freeStyleProject.getDescription() );

            }
            Label label = freeStyleProject.getAssignedLabel();
            if ( label!=null){
                ModelASTAgent agent = new ModelASTAgent( this );
                ModelASTKey agentKey = new ModelASTKey( this );
                agentKey.setKey( "'" + label.getName() + "'" );

                agent.setAgentType( agentKey );
                converterResult.getModelASTPipelineDef().setAgent( agent );
            } else {
                ModelASTAgent agent = new ModelASTAgent( this );
                ModelASTKey agentKey = new ModelASTKey( this );
                agentKey.setKey( "any" );

                agent.setAgentType( agentKey );
                converterResult.getModelASTPipelineDef().setAgent( agent );
            }

            convertJobProperties(converterRequest, converterResult, freeStyleProject.getProperties());

            convertBuilders( converterRequest, converterResult, freeStyleProject.getBuilders());

            if(converterRequest.isCreateProject()) {

                String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();
                //groovy = StringUtils.removeStart( groovy, "pipeline {" );
                //groovy = StringUtils.removeEnd( groovy, "}" );

                // FIXME make sandbox configurable
                ((WorkflowJob)converterResult.getJob()).setDefinition(new CpsFlowDefinition( groovy, true ));
                converterResult.getJob().save();
            }

        }
        catch ( IOException e )
        {
            throw new ConverterException( e.getMessage(), e);
        }

    }

    protected void convertBuilders(ConverterRequest converterRequest, ConverterResult converterResult, List<Builder> builders) {
        ModelASTStages stages = converterResult.getModelASTPipelineDef().getStages();
        if(stages==null){
            stages = new ModelASTStages( this );
            converterResult.getModelASTPipelineDef().setStages( stages );
        }
        for(Builder builder:builders){
            findBuilderConverter(builder).stream().forEach( builderConverter -> {
                ModelASTStage stage = builderConverter.convert( converterRequest, builder );
                if(stage!=null){
                    converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );
                }
            } );
        }
    }

    protected void convertJobProperties( ConverterRequest converterRequest, ConverterResult converterResult,
                                         Map<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> map ) {

        for( Map.Entry<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> entry : map.entrySet() ) {
            List<JobPropertyConverter> converters = findJobPropertyConverters( entry.getKey(), entry.getValue() );
            // if any special converters we used them otherwise simply copy the property
            if(!converters.isEmpty()) {
                converters.stream().forEach( jobPropertyConverter ->
                    jobPropertyConverter.doConvert( converterRequest, converterResult, //
                                                        entry.getKey(), entry.getValue() )

                );
            } else {
                if(converterRequest.isCreateProject()) {
                    try
                    {
                        converterResult.getJob().addProperty( entry.getValue() );
                    } catch ( IOException e ) {
                        // FIXME better exception handling here
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    protected List<JobPropertyConverter> findJobPropertyConverters( JobPropertyDescriptor jobPropertyDescriptor,
                                                                    JobProperty jobProperty) {

        List<JobPropertyConverter> converters = Jenkins.get().getExtensionList( JobPropertyConverter.class );
        return converters.stream()
            .filter( converter -> converter.canConvert( jobPropertyDescriptor, jobProperty ) )
            .collect(Collectors.toList());
    }

    protected List<BuilderConverter> findBuilderConverter( Builder builder) {

        List<BuilderConverter> converters = Jenkins.get().getExtensionList( BuilderConverter.class );
        return converters.stream()
            .filter( converter -> converter.canConvert( builder ) )
            .collect(Collectors.toList());
    }

    @Override
    public boolean canConvert( Job job )
    {
        return job instanceof FreeStyleProject;
    }
}
