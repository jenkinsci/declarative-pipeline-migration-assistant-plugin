package io.jenkins.plugins.todeclarative.converter.jobproperty;

import static io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils.buildKeyPairArg;

import hudson.plugins.buildblocker.BuildBlockerProperty;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = {"build-blocker-plugin"})
public class BuildBlockerPropertyConverter extends SingleTypedConverter<BuildBlockerProperty> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        BuildBlockerProperty buildBlockerProperty = (BuildBlockerProperty) target;

        ModelASTOption buildBlockerOption = new ModelASTOption(this);
        buildBlockerOption.setName("buildBlocker");
        //  buildBlocker (useBuildBlocker: true, blockLevel: 'NODE', scanQueueFor: 'ALL', blockingJobs: 'foo-.*')

        List<ModelASTMethodArg> optionArgs = new ArrayList<>();
        optionArgs.add(buildKeyPairArg("useBuildBlocker", buildBlockerProperty.isUseBuildBlocker()));

        if (buildBlockerProperty.getBlockLevel() != null) {
            optionArgs.add(buildKeyPairArg(
                    "blockLevel", buildBlockerProperty.getBlockLevel().toString()));
        }
        if (buildBlockerProperty.getScanQueueFor() != null) {
            optionArgs.add(buildKeyPairArg(
                    "scanQueueFor", buildBlockerProperty.getScanQueueFor().toString()));
        }
        if (StringUtils.isNotBlank(buildBlockerProperty.getBlockingJobs())) {
            optionArgs.add(buildKeyPairArg("blockingJobs", buildBlockerProperty.getBlockingJobs()));
        }
        buildBlockerOption.setArgs(optionArgs);

        ModelASTUtils.addOption(result.getModelASTPipelineDef(), buildBlockerOption);
        return true;
    }
}
