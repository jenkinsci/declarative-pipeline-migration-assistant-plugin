package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.Extension;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;

import static io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils.addStep;

@Extension
public class ArtifactArchiverConverter implements PublisherConverter {
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher ) {

        ArtifactArchiver artifactArchiver = (ArtifactArchiver) publisher;
        ModelASTBuildCondition buildCondition;
        if(artifactArchiver.isOnlyIfSuccessful()) {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "success" );
        } else {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
        }

        ModelASTStep archiveArtifacts = buildGenericStep(publisher);
        addStep(buildCondition, archiveArtifacts);

        return null;
    }

    @Override
    public boolean canConvert( Publisher publisher ) {
        return publisher instanceof ArtifactArchiver;
    }
}
