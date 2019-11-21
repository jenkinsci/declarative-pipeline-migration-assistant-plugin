package io.jenkins.plugins.todeclarative.converter.trigger;

import hudson.Extension;
import hudson.model.Result;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.trigger.TriggerConverter;
import jenkins.triggers.ReverseBuildTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.ArrayList;
import java.util.List;

@Extension
public class ReverseBuildTriggerConverter implements TriggerConverter
{
    @Override
    public void convert( ConverterRequest request, ConverterResult converterResult, TriggerDescriptor triggerDescriptor,
                         Trigger<?> trigger )
    {
        ReverseBuildTrigger reverseBuildTrigger = (ReverseBuildTrigger) trigger;

        String upstreamProjects = reverseBuildTrigger.getUpstreamProjects();
        Result threshold = reverseBuildTrigger.getThreshold();

        ModelASTTrigger modelASTTrigger = new ModelASTTrigger( this );
        modelASTTrigger.setName( "upstream" );
        List<ModelASTMethodArg> args = new ArrayList<>( );

        {
            ModelASTKeyValueOrMethodCallPair projects = new ModelASTKeyValueOrMethodCallPair( this );
            ModelASTKey key = new ModelASTKey( this );
            key.setKey( "upstreamProjects" );
            projects.setKey( key );
            projects.setValue( ModelASTValue.fromConstant( upstreamProjects, this ) );
            args.add( projects );
        }

        if(threshold!=null)
        {
            ModelASTKeyValueOrMethodCallPair thresholdArg = new ModelASTKeyValueOrMethodCallPair(this);
            ModelASTKey key = new ModelASTKey(this);
            key.setKey( "threshold" );
            thresholdArg.setKey( key  );
            thresholdArg.setValue( ModelASTValue.fromGString( threshold.getClass().getName() + "." + threshold.toString(), this ) );
            args.add( thresholdArg );
        }

        modelASTTrigger.setArgs( args );

        ModelASTUtils.addTrigger( converterResult.getModelASTPipelineDef(), modelASTTrigger );
    }

    @Override
    public boolean canConvert( TriggerDescriptor triggerDescriptor, Trigger<?> trigger )
    {
        return trigger instanceof ReverseBuildTrigger;
    }
}
