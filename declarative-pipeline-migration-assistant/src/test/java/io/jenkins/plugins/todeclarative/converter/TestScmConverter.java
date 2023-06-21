package io.jenkins.plugins.todeclarative.converter;

import hudson.Extension;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jvnet.hudson.test.ExtractResourceSCM;

@Extension
public class TestScmConverter extends SingleTypedConverter<ExtractResourceSCM> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        try {
            ExtractResourceSCM extractResourceSCM = (ExtractResourceSCM) target;
            URL urlZip = getZipUrl(extractResourceSCM);
            String filePath = urlZip.getFile();

            ModelASTStage stage = new ModelASTStage(this);
            stage.setName("Checkout Scm");
            // git url: "", branch: '',changelog: '', credentialsId: '', pool: ''
            List<ModelASTStep> steps = new ArrayList<>();

            ModelASTStep step = new ModelASTStep(this);
            ModelASTSingleArgument singleArgument = new ModelASTSingleArgument(this);
            // TODO escape shell command?? not sure as might be done when running it
            singleArgument.setValue(ModelASTValue.fromGString("zipFile:'" + filePath + "'", this));
            step.setArgs(singleArgument);
            step.setName("unzip");
            steps.add(step);

            ModelASTBranch branch = new ModelASTBranch(this);
            branch.setSteps(steps);
            stage.setBranches(Arrays.asList(branch));
            ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private URL getZipUrl(ExtractResourceSCM extractResourceSCM) throws Exception {
        Field[] fields = extractResourceSCM.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (StringUtils.equals("zip", field.getName())) {
                field.setAccessible(true);
                return (URL) field.get(extractResourceSCM);
            }
        }
        return null;
    }
}
