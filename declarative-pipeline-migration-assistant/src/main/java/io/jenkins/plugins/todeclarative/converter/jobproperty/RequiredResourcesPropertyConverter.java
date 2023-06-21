package io.jenkins.plugins.todeclarative.converter.jobproperty;

import static io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils.buildKeyPairArg;

import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = {"lockable-resources"})
public class RequiredResourcesPropertyConverter extends SingleTypedConverter<RequiredResourcesProperty> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {

        RequiredResourcesProperty requiredResourcesProperty = (RequiredResourcesProperty) target;

        // lock(label: 'label', resource: 'resource')
        ModelASTOption option = new ModelASTOption(this);
        option.setName("lock");

        List<ModelASTMethodArg> lockArgs = new ArrayList<>();

        if (StringUtils.isNotBlank(requiredResourcesProperty.getLabelName())) {
            lockArgs.add(buildKeyPairArg("label", requiredResourcesProperty.getLabelName()));
        }

        if (StringUtils.isNotBlank(requiredResourcesProperty.getResourceNames())) {
            lockArgs.add(buildKeyPairArg("resource", requiredResourcesProperty.getResourceNames()));
        }

        option.setArgs(lockArgs);

        ModelASTUtils.addOption(result.getModelASTPipelineDef(), option);
        return true;
    }
}
