package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.tasks.BatchFile;
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
public class BatchFileConverter extends SingleTypedConverter<BatchFile> {
    public static final String BATCH_NUMBER_KEY = BatchFileConverter.class.getName() + ".shell.number";

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        BatchFile batchFile = (BatchFile) target;
        ModelASTStage stage = new ModelASTStage(this);
        int stageNumber = request.getAndIncrement(BATCH_NUMBER_KEY);
        stage.setName("Batch script " + stageNumber);
        ModelASTBranch branch = new ModelASTBranch(this);
        stage.setBranches(Arrays.asList(branch));
        ModelASTStep step = new ModelASTStep(this);
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument(this);
        singleArgument.setValue(ModelASTValue.fromConstant(batchFile.getCommand(), this));
        step.setArgs(singleArgument);
        step.setName("bat");
        ModelASTUtils.wrapBranch(result, step, branch);
        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);
        return true;
    }
}
