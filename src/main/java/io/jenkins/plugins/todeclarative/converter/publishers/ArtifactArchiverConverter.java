package io.jenkins.plugins.todeclarative.converter.publishers;

import hudson.Extension;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.ModelASTUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.HashMap;
import java.util.Map;

@Extension
public class ArtifactArchiverConverter implements PublisherConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher )
    {
        ArtifactArchiver artifactArchiver = (ArtifactArchiver) publisher;
        ModelASTBuildCondition buildCondition;
        // FIXME manage result condition
        if(artifactArchiver.isOnlyIfSuccessful())
        {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "success" );
        } else {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
        }
        ModelASTBranch branch = new ModelASTBranch( this );
        buildCondition.setBranch( branch );

        // archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
        ModelASTStep archiveArtifacts = new ModelASTStep( this );
        archiveArtifacts.setName( "archiveArtifacts" );
        branch.getSteps().add( archiveArtifacts );

        Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
        // archiveArtifacts allowEmptyArchive: true, artifacts: 'fpp', caseSensitive: false, defaultExcludes: false,
        // excludes: 'fr*', fingerprint: true, onlyIfSuccessful: true

        { // allowEmptyArchive
            ModelASTKey allowEmptyArchive = new ModelASTKey( this );
            allowEmptyArchive.setKey( "allowEmptyArchive" );
            ModelASTValue urlValue = ModelASTValue.fromConstant( artifactArchiver.getAllowEmptyArchive(), this );
            args.put( allowEmptyArchive, urlValue );
        }
        { // artifacts
            ModelASTKey artifacts = new ModelASTKey( this );
            artifacts.setKey( "artifacts" );
            ModelASTValue urlValue = ModelASTValue.fromConstant( artifactArchiver.getArtifacts(), this );
            args.put( artifacts, urlValue );
        }
        { // caseSensitive
            ModelASTKey caseSensitive = new ModelASTKey( this );
            caseSensitive.setKey( "caseSensitive" );
            ModelASTValue urlValue = ModelASTValue.fromConstant( artifactArchiver.isCaseSensitive(), this );
            args.put( caseSensitive, urlValue );
        }
        { // defaultExcludes
            ModelASTKey defaultExcludes = new ModelASTKey( this );
            defaultExcludes.setKey( "defaultExcludes" );
            ModelASTValue urlValue = ModelASTValue.fromConstant( artifactArchiver.isDefaultExcludes(), this );
            args.put( defaultExcludes, urlValue );
        }
        { // excludes
            ModelASTKey excludes = new ModelASTKey( this );
            excludes.setKey( "excludes" );
            ModelASTValue urlValue = ModelASTValue.fromConstant( artifactArchiver.getExcludes(), this );
            args.put( excludes, urlValue );
        }
        { // fingerprint
            ModelASTKey fingerprint = new ModelASTKey( this );
            fingerprint.setKey( "fingerprint" );
            ModelASTValue urlValue = ModelASTValue.fromConstant( artifactArchiver.isFingerprint(), this );
            args.put( fingerprint, urlValue );
        }

        ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList( null);
        stepArgs.setArguments( args );
        archiveArtifacts.setArgs( stepArgs );

        return null;
    }

    @Override
    public boolean canConvert( Publisher publisher )
    {
        return publisher instanceof ArtifactArchiver;
    }
}
