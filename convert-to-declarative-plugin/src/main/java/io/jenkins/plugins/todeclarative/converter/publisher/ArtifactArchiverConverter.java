package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.Extension;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.structs.describable.DescribableModel;

import java.util.HashMap;
import java.util.Map;

@Extension
public class ArtifactArchiverConverter implements PublisherConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher )
    {
        if (!(publisher instanceof ArtifactArchiver )) {
            // can't use automatic conversion
            return null;
        }
        ArtifactArchiver artifactArchiver = (ArtifactArchiver) publisher;
        ModelASTBuildCondition buildCondition;
        if(artifactArchiver.isOnlyIfSuccessful())
        {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "success" );
        } else {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
        }

        ModelASTBranch branch = buildCondition.getBranch();
        if(branch==null){
            branch =new ModelASTBranch( this );
            buildCondition.setBranch( branch );
        }

        ModelASTStep archiveArtifacts = buildGenericStep(publisher);
        branch.getSteps().add( archiveArtifacts );

        return null;
    }

    @Override
    public boolean canConvert( Publisher publisher )
    {
        return publisher instanceof ArtifactArchiver;
    }
}
