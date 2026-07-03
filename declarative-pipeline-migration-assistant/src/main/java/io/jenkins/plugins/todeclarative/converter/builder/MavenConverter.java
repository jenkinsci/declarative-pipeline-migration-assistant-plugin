package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.JDK;
import hudson.tasks.Maven;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.Arrays;
import java.util.Map;
import jenkins.model.Jenkins;
import jenkins.mvn.FilePathGlobalSettingsProvider;
import jenkins.mvn.FilePathSettingsProvider;
import jenkins.mvn.GlobalSettingsProvider;
import jenkins.mvn.SettingsProvider;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTEnvironment;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTEnvironmentValue;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

@Extension(ordinal = 100)
/**
 * This is a basic converter Maven converter to a simple sh command.
 */
public class MavenConverter extends SingleTypedConverter<Maven> {
    public static final String MAVEN_NUMBER_KEY = ShellConverter.class.getName() + ".shell.number";

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        Maven maven = (Maven) target;
        Maven.MavenInstallation mavenInstallation = maven.getMaven();
        if (mavenInstallation != null) {
            ModelASTKey key = new ModelASTKey(this);
            key.setKey("maven");
            ModelASTUtils.addTool(
                    result.getModelASTPipelineDef(),
                    key,
                    ModelASTValue.fromConstant(mavenInstallation.getName(), this));
        }

        FreeStyleProject freeStyleProject = (FreeStyleProject) request.getJob();
        JDK jdk = freeStyleProject.getJDK();
        // default jdk may have configured automatically but we don't want that
        if (jdk != null
                && !(Jenkins.get().getJDKs().size() == 1 && "default".equalsIgnoreCase(jdk.getName()))) {
            ModelASTKey key = new ModelASTKey(this);
            key.setKey("jdk");
            ModelASTUtils.addTool(
                    result.getModelASTPipelineDef(), key, ModelASTValue.fromConstant(jdk.getName(), this));
        }

        ModelASTStage stage = new ModelASTStage(this);
        int stageNumber = request.getAndIncrement(MAVEN_NUMBER_KEY);
        stage.setName("Maven Build " + stageNumber);
        ModelASTBranch branch = new ModelASTBranch(this);
        stage.setBranches(Arrays.asList(branch));
        ModelASTStep step = new ModelASTStep(this);

        branch.setSteps(Arrays.asList(step));

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add("mvn");
        String targets = maven.getTargets();
        if (targets != null && !targets.isBlank()) {
            Arrays.stream(targets.trim().split("\\s+")).forEach(args::add);
        }
        if (maven.usePrivateRepository) {
            args.add("-Dmaven.repo.local=.repository");
        }

        if (maven.pom != null && !maven.pom.isBlank()) {
            args.add("-f", maven.pom);
        }

        if (maven.properties != null && !maven.properties.isBlank()) {
            args.add(maven.properties);
        }

        {
            SettingsProvider settingsProvider = maven.getSettings();
            if (settingsProvider instanceof FilePathSettingsProvider) {
                String settingsPath = ((FilePathSettingsProvider) settingsProvider).getPath();
                if (settingsPath != null && !settingsPath.isBlank()) {
                    args.add("-s", settingsPath);
                }
            }
        }

        {
            GlobalSettingsProvider globalSettingsProvider = maven.getGlobalSettings();
            if (globalSettingsProvider instanceof FilePathGlobalSettingsProvider) {
                String settingsPath = ((FilePathGlobalSettingsProvider) globalSettingsProvider).getPath();
                if (settingsPath != null && !settingsPath.isBlank()) {
                    args.add("-gs", settingsPath);
                }
            }
        }

        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument(this);
        singleArgument.setValue(ModelASTValue.fromConstant(args.toString(), this));

        step.setArgs(singleArgument);
        step.setName("sh");

        if (maven.jvmOptions != null && !maven.jvmOptions.isBlank()) {
            ModelASTEnvironment environment = stage.getEnvironment();
            if (environment == null) {
                environment = new ModelASTEnvironment(this);
                stage.setEnvironment(environment);
            }
            Map<ModelASTKey, ModelASTEnvironmentValue> environmentVariables = environment.getVariables();
            ModelASTKey key = new ModelASTKey(this);
            key.setKey("MAVEN_OPTS");
            environmentVariables.put(key, ModelASTValue.fromConstant(maven.jvmOptions, this));
        }

        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);
        return true;
    }
}
