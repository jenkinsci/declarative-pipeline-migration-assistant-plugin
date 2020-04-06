package io.jenkins.plugins.todeclarative.converter.trigger;

import hudson.Extension;
import hudson.triggers.TimerTrigger;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.Arrays;

@Extension
public class TimerTriggerConverter extends SingleTypedConverter<TimerTrigger>
{
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        TimerTrigger timerTrigger = (TimerTrigger) target;

        String cronValue = timerTrigger.getSpec();

        ModelASTTrigger modelASTTrigger = new ModelASTTrigger( this );
        modelASTTrigger.setName( "cron" );
        modelASTTrigger.setArgs( Arrays.asList(ModelASTValue.fromConstant( cronValue, this )) );

        ModelASTUtils.addTrigger( result.getModelASTPipelineDef(), modelASTTrigger );
        return true;
    }
}
