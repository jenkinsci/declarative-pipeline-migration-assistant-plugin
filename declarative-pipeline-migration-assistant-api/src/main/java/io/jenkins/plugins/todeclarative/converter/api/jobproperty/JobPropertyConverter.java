package io.jenkins.plugins.todeclarative.converter.api.jobproperty;

import hudson.ExtensionPoint;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;


/**
 * All implementations of this extension will be used to convert {@link JobProperty} from a {@link JobPropertyDescriptor}.
 * Your extension must modify the model from the {@link ConverterResult} instance.
 */
public interface JobPropertyConverter extends ExtensionPoint
{
    void convert( ConverterRequest request, ConverterResult converterResult,
                  JobPropertyDescriptor jobPropertyDescriptor, JobProperty jobProperty );

    /**
     * @return <code>true</code> if the extension is able to convert the {@link JobProperty}
     */
    boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor, JobProperty jobProperty );

}
