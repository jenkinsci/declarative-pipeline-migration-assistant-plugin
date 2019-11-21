package io.jenkins.plugins.todeclarative.converter.trigger;

import hudson.Extension;
import hudson.triggers.TimerTrigger;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.trigger.TriggerConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.Arrays;

@Extension
public class TimerTriggerConverter
    implements TriggerConverter
{
    @Override
    public void convert( ConverterRequest request, ConverterResult converterResult, TriggerDescriptor triggerDescriptor,
                         Trigger<?> trigger )
    {
        TimerTrigger timerTrigger = (TimerTrigger) trigger;

        String cronValue = timerTrigger.getSpec();

        ModelASTTrigger modelASTTrigger = new ModelASTTrigger( this );
        modelASTTrigger.setName( "cron" );
        modelASTTrigger.setArgs( Arrays.asList(ModelASTValue.fromConstant( cronValue, this )) );

        ModelASTUtils.addTrigger( converterResult.getModelASTPipelineDef(), modelASTTrigger );
    }

    @Override
    public boolean canConvert( TriggerDescriptor triggerDescriptor, Trigger<?> trigger )
    {
        return trigger instanceof TimerTrigger;
    }
}
