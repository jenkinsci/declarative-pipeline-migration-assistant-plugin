package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.Extension;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import jenkins.tasks.SimpleBuildStep;
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
        if (!(publisher instanceof SimpleBuildStep )) {
            // can't use automatic conversion
            return null;
        }
        ArtifactArchiver artifactArchiver = (ArtifactArchiver) publisher;
        ModelASTBuildCondition buildCondition;
        // FIXME manage result condition
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

        Class<Publisher> actualPublisherClass = (Class<Publisher>)publisher.getClass();
        DescribableModel<Publisher> model = DescribableModel.of( actualPublisherClass);
        Map<String, Object> uninstantiated = model.uninstantiate2(publisher).toMap();

        // archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
        ModelASTStep archiveArtifacts = new ModelASTStep( this );
        archiveArtifacts.setName( "archiveArtifacts" ); // publisher.getDescriptor().getClass().getAnnotation( Symbol.class ).value()[0]
        branch.getSteps().add( archiveArtifacts );

        Map<ModelASTKey, ModelASTValue> args = new HashMap<>();

        ModelASTStep step = new ModelASTStep( this );
        step.setName("step");
        for (Map.Entry<String, Object> arg: uninstantiated.entrySet()) {
            // add the args to the step, will be something like ([$class: 'ArtifactArchiver', artifacts: 'something'])
            ModelASTKey key = new ModelASTKey( this );
            key.setKey( arg.getKey() );
            ModelASTValue value = ModelASTValue.fromConstant( arg.getValue(), this );
            args.put( key, value );
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
