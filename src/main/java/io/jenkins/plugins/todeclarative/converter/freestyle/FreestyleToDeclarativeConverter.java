package io.jenkins.plugins.todeclarative.converter.freestyle;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.ConverterException;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.ToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.jobproperty.JobPropertyConverter;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;
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
            }

            convertJobProperties(converterRequest, converterResult, freeStyleProject.getProperties());

            if(converterRequest.isCreateProject()) {
                converterResult.getJob().save();
            }

        }
        catch ( IOException e )
        {
            throw new ConverterException( e.getMessage(), e);
        }

    }

    protected void convertJobProperties( ConverterRequest converterRequest, ConverterResult converterResult,
                                         Map<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> map ) {

        ModelASTPipelineDef pipelineDef = converterResult.getModelASTPipelineDef();
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

    @Override
    public boolean canConvert( Job job )
    {
        return job instanceof FreeStyleProject;
    }
}
