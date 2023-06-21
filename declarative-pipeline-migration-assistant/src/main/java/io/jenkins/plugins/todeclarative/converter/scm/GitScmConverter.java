package io.jenkins.plugins.todeclarative.converter.scm;

import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = {"git"})
public class GitScmConverter extends SingleTypedConverter<GitSCM> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        List<UserRemoteConfig> repoList = ((GitSCM) target).getUserRemoteConfigs();
        if (repoList.isEmpty()) {
            return true;
        }
        ModelASTStage stage = new ModelASTStage(this);
        stage.setName("Checkout Scm");
        // git url: "", branch: '',changelog: '', credentialsId: '', pool: ''
        List<ModelASTStep> steps = new ArrayList<>();
        for (UserRemoteConfig userRemoteConfig : repoList) {
            ModelASTStep git = new ModelASTStep(null);
            git.setName("git");

            Map<ModelASTKey, ModelASTValue> args = new HashMap<>();

            { // url
                ModelASTKey url = new ModelASTKey(this);
                url.setKey("url");
                ModelASTValue urlValue = ModelASTValue.fromConstant(userRemoteConfig.getUrl(), this);
                args.put(url, urlValue);
            }

            if (StringUtils.isNotBlank(userRemoteConfig.getRefspec())) {
                ModelASTKey branch = new ModelASTKey(this);
                branch.setKey("branch");
                ModelASTValue branchValue = ModelASTValue.fromConstant(userRemoteConfig.getRefspec(), this);
                args.put(branch, branchValue);
            }

            if (StringUtils.isNotBlank(userRemoteConfig.getCredentialsId())) {
                ModelASTKey credentialsId = new ModelASTKey(this);
                credentialsId.setKey("credentialsId");
                ModelASTValue credentialsIdValue =
                        ModelASTValue.fromConstant(userRemoteConfig.getCredentialsId(), this);
                args.put(credentialsId, credentialsIdValue);
            }

            ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList(null);
            stepArgs.setArguments(args);
            git.setArgs(stepArgs);
            steps.add(git);
        }

        ModelASTBranch branch = new ModelASTBranch(this);
        branch.setSteps(steps);
        stage.setBranches(Arrays.asList(branch));
        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);
        return true;
    }
}
