package io.jenkins.plugins.todeclarative.converter.jobproperty;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildParameter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildParameters;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;

@Extension
public class ParameterPropertyConverter extends SingleTypedConverter<ParametersDefinitionProperty> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        ParametersDefinitionProperty parametersDefinitionProperty = (ParametersDefinitionProperty) target;
        if (parametersDefinitionProperty.getParameterDefinitions().isEmpty()) {
            return true;
        }
        // now in the Jenkinsfile as well
        ModelASTPipelineDef model = result.getModelASTPipelineDef();
        if (model.getParameters() == null) {
            model.setParameters(new ModelASTBuildParameters(this));
        }

        int numWarnings = result.getWarnings().size();
        parametersDefinitionProperty
                .getParameterDefinitions() //
                .forEach(parameterDefinition -> {
                    ModelASTBuildParameter parameter = build(parameterDefinition, result);
                    if (parameter != null) {
                        ModelASTUtils.addParameter(model, parameter);
                    }
                });
        if (result.getWarnings().size() > numWarnings) {
            return false;
        }
        return true;
    }

    protected ModelASTBuildParameter build(ParameterDefinition parameterDefinition, ConverterResult converterResult) {
        // maybe an extension point here if users have their parameter definition type?
        List<ModelASTMethodArg> args = new ArrayList<>();
        if (StringParameterDefinition.class.getSimpleName().equals(parameterDefinition.getType())) {
            ModelASTBuildParameter parameter = new ModelASTBuildParameter(this);
            parameter.setName("string");
            args.add(ModelASTUtils.buildKeyPairArg("name", parameterDefinition.getName()));
            args.add(ModelASTUtils.buildKeyPairArg(
                    "defaultValue", ((StringParameterDefinition) parameterDefinition).getDefaultValue()));
            args.add(ModelASTUtils.buildKeyPairArg("description", parameterDefinition.getDescription()));
            parameter.setArgs(args);
            return parameter;
        }

        // FIXME List<String> for choices cannot be converted
        if (ChoiceParameterDefinition.class.getSimpleName().equals(parameterDefinition.getType())) {
            ModelASTBuildParameter parameter = new ModelASTBuildParameter(this);
            parameter.setName("choice");
            args.add(ModelASTUtils.buildKeyPairArg("name", parameterDefinition.getName()));
            args.add(ModelASTUtils.buildKeyPairArg(
                    "choices", ((ChoiceParameterDefinition) parameterDefinition).getChoices()));
            args.add(ModelASTUtils.buildKeyPairArg("description", parameterDefinition.getDescription()));
            parameter.setArgs(args);
            return parameter;
        }

        if (BooleanParameterDefinition.class.getSimpleName().equals(parameterDefinition.getType())) {
            ModelASTBuildParameter parameter = new ModelASTBuildParameter(this);
            parameter.setName("booleanParam");
            args.add(ModelASTUtils.buildKeyPairArg("name", parameterDefinition.getName()));
            args.add(ModelASTUtils.buildKeyPairArg(
                    "defaultValue", getDefaultBooleanParameterValue((BooleanParameterDefinition) parameterDefinition)));
            args.add(ModelASTUtils.buildKeyPairArg("description", parameterDefinition.getDescription()));
            parameter.setArgs(args);
            return parameter;
        }

        converterResult.addWarning(new Warning(
                "Cannot convert property of type: " + parameterDefinition.getType(), parameterDefinition.getClass()));
        return null;
    }

    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Superclass method is @CheckForNull, but subclass impl never returns null")
    private boolean getDefaultBooleanParameterValue(BooleanParameterDefinition def) {
        return def.getDefaultParameterValue().value;
    }
}
