package io.jenkins.plugins.todeclarative.converter.trigger;

import hudson.Extension;
import hudson.model.Result;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.ArrayList;
import java.util.List;
import jenkins.triggers.ReverseBuildTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

@Extension
public class ReverseBuildTriggerConverter extends SingleTypedConverter<ReverseBuildTrigger> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        ReverseBuildTrigger reverseBuildTrigger = (ReverseBuildTrigger) target;

        String upstreamProjects = reverseBuildTrigger.getUpstreamProjects();
        Result threshold = reverseBuildTrigger.getThreshold();

        ModelASTTrigger modelASTTrigger = new ModelASTTrigger(this);
        modelASTTrigger.setName("upstream");
        List<ModelASTMethodArg> args = new ArrayList<>();

        {
            ModelASTKeyValueOrMethodCallPair projects = new ModelASTKeyValueOrMethodCallPair(this);
            ModelASTKey key = new ModelASTKey(this);
            key.setKey("upstreamProjects");
            projects.setKey(key);
            projects.setValue(ModelASTValue.fromConstant(upstreamProjects, this));
            args.add(projects);
        }

        if (threshold != null) {
            ModelASTKeyValueOrMethodCallPair thresholdArg = new ModelASTKeyValueOrMethodCallPair(this);
            ModelASTKey key = new ModelASTKey(this);
            key.setKey("threshold");
            thresholdArg.setKey(key);
            thresholdArg.setValue(
                    ModelASTValue.fromGString(threshold.getClass().getName() + "." + threshold.toString(), this));
            args.add(thresholdArg);
        }

        modelASTTrigger.setArgs(args);

        ModelASTUtils.addTrigger(result.getModelASTPipelineDef(), modelASTTrigger);
        return true;
    }
}
