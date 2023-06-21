package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import static io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils.buildKeyPairArg;

import hudson.plugins.build_timeout.BuildTimeOutStrategy;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;
import hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OptionalExtension(requirePlugins = {"build-timeout"})
public class BuildTimeoutWrapperConverter extends SingleTypedConverter<BuildTimeoutWrapper> {
    private Logger LOGGER = LoggerFactory.getLogger(BuildTimeoutWrapperConverter.class);

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        BuildTimeoutWrapper timeoutWrapper = (BuildTimeoutWrapper) target;
        BuildTimeOutStrategy strategy = timeoutWrapper.getStrategy();

        if (!(strategy instanceof AbsoluteTimeOutStrategy)) {
            result.addWarning(new Warning(
                    "we can only convert Absolute timeout and not "
                            + strategy.getDescriptor().getDisplayName(),
                    BuildTimeOutStrategy.class));
            return false;
        }

        ModelASTOption timeout = new ModelASTOption(this);
        timeout.setName("timeout");

        List<ModelASTMethodArg> timeoutArgs = new ArrayList<>();

        AbsoluteTimeOutStrategy absoluteTimeOutStrategy = ((AbsoluteTimeOutStrategy) strategy);

        timeoutArgs.add(buildKeyPairArg("time", Long.parseLong(absoluteTimeOutStrategy.getTimeoutMinutes())));
        timeoutArgs.add(buildKeyPairArg("unit", "MINUTES"));

        timeout.setArgs(timeoutArgs);

        ModelASTPipelineDef modelASTPipelineDef = result.getModelASTPipelineDef();
        if (modelASTPipelineDef.getOptions() == null) {
            modelASTPipelineDef.setOptions(new ModelASTOptions(this));
        }
        ModelASTUtils.addOption(modelASTPipelineDef, timeout);
        return true;
    }
}
