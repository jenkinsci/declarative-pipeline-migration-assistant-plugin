package io.jenkins.plugins.todeclarative.converter.jobproperty;

import static io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils.buildKeyPairArg;

import hudson.Extension;
import hudson.tasks.LogRotator;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.BuildDiscarder;
import jenkins.model.BuildDiscarderProperty;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;

@Extension
public class BuildDiscarderPropertyConverter extends SingleTypedConverter<BuildDiscarderProperty> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        BuildDiscarderProperty buildDiscarderProperty = (BuildDiscarderProperty) target;

        BuildDiscarder buildDiscarder = buildDiscarderProperty.getStrategy();
        if (!(buildDiscarder instanceof LogRotator)) {
            // nothing to do
            result.addWarning(new Warning(
                    "We cannot convert BuildDiscarder strategy "
                            + (buildDiscarder == null
                                    ? "'null'"
                                    : buildDiscarder.getClass().getName()),
                    getClass()));
            return false;
        }

        LogRotator logRotator = (LogRotator) buildDiscarder;

        ModelASTOption option = new ModelASTOption(this);
        option.setName("buildDiscarder");

        ModelASTOption logRotatorOption = new ModelASTOption(this);
        logRotatorOption.setName("logRotator");
        option.getArgs().add(logRotatorOption);

        List<ModelASTMethodArg> rotatorArgs = new ArrayList<>();

        if (StringUtils.isNotBlank(logRotator.getArtifactDaysToKeepStr())) {
            rotatorArgs.add(buildKeyPairArg("artifactDaysToKeepStr", logRotator.getArtifactDaysToKeepStr()));
        }
        if (StringUtils.isNotBlank(logRotator.getDaysToKeepStr())) {
            rotatorArgs.add(buildKeyPairArg("daysToKeepStr", logRotator.getDaysToKeepStr()));
        }
        if (StringUtils.isNotBlank(logRotator.getNumToKeepStr())) {
            rotatorArgs.add(buildKeyPairArg("numToKeepStr", logRotator.getNumToKeepStr()));
        }
        if (StringUtils.isNotBlank(logRotator.getArtifactNumToKeepStr())) {
            rotatorArgs.add(buildKeyPairArg("artifactNumToKeepStr", logRotator.getArtifactNumToKeepStr()));
        }
        logRotatorOption.setArgs(rotatorArgs);

        ModelASTUtils.addOption(result.getModelASTPipelineDef(), option);
        return true;
    }
}
