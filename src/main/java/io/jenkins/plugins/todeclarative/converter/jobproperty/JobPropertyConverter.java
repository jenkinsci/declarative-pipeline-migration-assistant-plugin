package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface JobPropertyConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult converterResult,
                           JobPropertyDescriptor jobPropertyDescriptor,
                           JobProperty<? super FreeStyleProject> jobProperty );

    boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor,
                        JobProperty<? super FreeStyleProject> jobProperty );

}
