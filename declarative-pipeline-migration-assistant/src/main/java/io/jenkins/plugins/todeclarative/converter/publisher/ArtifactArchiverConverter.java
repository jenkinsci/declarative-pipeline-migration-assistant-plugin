package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.Extension;
import hudson.tasks.ArtifactArchiver;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;

@Extension
public class ArtifactArchiverConverter extends SingleTypedConverter<ArtifactArchiver> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {

        ArtifactArchiver artifactArchiver = (ArtifactArchiver) target;
        ModelASTBuildCondition buildCondition;
        if (artifactArchiver.isOnlyIfSuccessful()) {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition(result.getModelASTPipelineDef(), "success");
        } else {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition(result.getModelASTPipelineDef(), "always");
        }

        ModelASTStep archiveArtifacts = ModelASTUtils.buildGenericStep(artifactArchiver, this);
        ModelASTUtils.addStep(buildCondition, archiveArtifacts);

        return true;
    }
}
