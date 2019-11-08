package io.jenkins.plugins.todeclarative.converter.api.trigger;

import hudson.ExtensionPoint;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;


/**
 * All implementations of this extension will be used to convert {@link JobProperty} from a {@link JobPropertyDescriptor}.
 * Your extension must modify the model from the {@link ConverterResult} instance.
 */
public interface TriggerConverter
    extends ExtensionPoint
{
    void convert( ConverterRequest request, ConverterResult converterResult,
                  TriggerDescriptor triggerDescriptor, Trigger<?> trigger );

    /**
     * @return <code>true</code> if the extension is able to convert the {@link Trigger}
     */
    boolean canConvert( TriggerDescriptor triggerDescriptor, Trigger<?> trigger );

}
