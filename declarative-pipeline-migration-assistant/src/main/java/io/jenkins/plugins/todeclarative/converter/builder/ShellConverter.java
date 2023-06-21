package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.tasks.Shell;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.Arrays;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

@Extension
public class ShellConverter extends SingleTypedConverter<Shell> {
    public static final String SHELL_NUMBER_KEY = ShellConverter.class.getName() + ".shell.number";

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        Shell shell = (Shell) target;
        ModelASTStage stage = new ModelASTStage(this);
        int stageNumber = request.getAndIncrement(SHELL_NUMBER_KEY);
        stage.setName("Shell script " + stageNumber);
        ModelASTBranch branch = new ModelASTBranch(this);
        stage.setBranches(Arrays.asList(branch));
        ModelASTStep step = new ModelASTStep(this);
        step.setName("sh");
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument(this);
        singleArgument.setValue(ModelASTValue.fromConstant(shell.getCommand(), this));
        step.setArgs(singleArgument);
        ModelASTUtils.wrapBranch(result, step, branch);
        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);

        return true;
    }
}
