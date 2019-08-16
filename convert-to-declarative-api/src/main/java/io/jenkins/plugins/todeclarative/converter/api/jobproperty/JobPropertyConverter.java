package io.jenkins.plugins.todeclarative.converter.api.jobproperty;

import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;


/**
 * All implementation of this extension will be used to convert {@link JobProperty} from a {@link JobPropertyDescriptor}.
 * Your extension can either return a {@link ModelASTStage} or modify the model from the {@link ConverterResult} instance
 */
public interface JobPropertyConverter
{
    void convert( ConverterRequest request, ConverterResult converterResult,
                  JobPropertyDescriptor jobPropertyDescriptor, JobProperty jobProperty );

    /**
     * @return <code>true</code> if the extension is able to convert the {@link Builder}
     */
    boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor, JobProperty jobProperty );

}
