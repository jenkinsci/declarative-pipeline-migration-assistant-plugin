package io.jenkins.plugins.todeclarative.converter.api;

import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTools;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTriggers;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.Optional;

/**
 * Some util methods to work with Declarative Model
 */
public class ModelASTUtils
{
    private ModelASTUtils()
    {
        // no op
    }

    public static ModelASTKeyValueOrMethodCallPair buildKeyPairArg( String key, Object value){
        ModelASTKey astKey = new ModelASTKey( ModelASTUtils.class);
        astKey.setKey(key);
        ModelASTKeyValueOrMethodCallPair keyPairArg = new ModelASTKeyValueOrMethodCallPair(ModelASTUtils.class);
        keyPairArg.setKey( astKey );
        keyPairArg.setValue( ModelASTValue.fromConstant( value, ModelASTUtils.class) );
        return keyPairArg;
    }

    public static ModelASTBuildCondition buildOrFindBuildCondition( ModelASTPipelineDef modelASTPipelineDef, String condition) {
        ModelASTPostBuild postBuild = modelASTPipelineDef.getPostBuild();
        if(postBuild==null){
            postBuild = new ModelASTPostBuild( modelASTPipelineDef );
            modelASTPipelineDef.setPostBuild( postBuild );
        }
        Optional<ModelASTBuildCondition> optional = postBuild.getConditions().stream()
             .filter( modelASTBuildCondition -> modelASTBuildCondition.getCondition().equals( condition ) )
             .findFirst();
        if(optional.isPresent()){
            return optional.get();
        }
        ModelASTBuildCondition modelASTBuildCondition = new ModelASTBuildCondition( modelASTPipelineDef );
        modelASTBuildCondition.setCondition( condition );
        postBuild.getConditions().add( modelASTBuildCondition );
        return modelASTBuildCondition;
    }

    public static void addStage( ModelASTPipelineDef modelASTPipelineDef, ModelASTStage stage) {
        if(modelASTPipelineDef.getStages()==null){
            modelASTPipelineDef.setStages( new ModelASTStages( modelASTPipelineDef ) );
        }
        modelASTPipelineDef.getStages().getStages().add( stage );
    }

    public static void addOption(ModelASTPipelineDef modelASTPipelineDef, ModelASTOption option) {
        if(modelASTPipelineDef.getOptions()==null){
            modelASTPipelineDef.setOptions( new ModelASTOptions( modelASTPipelineDef ) );
        }
        modelASTPipelineDef.getOptions().getOptions().add( option );
    }

    public static void addStep(ModelASTBuildCondition buildCondition, ModelASTStep step) {
        ModelASTBranch branch = buildCondition.getBranch();
        if(branch==null){
            branch = new ModelASTBranch( buildCondition );
            buildCondition.setBranch( branch );
        }
        branch.getSteps().add( step );
    }

    public static void addTool(ModelASTPipelineDef modelASTPipelineDef, ModelASTKey key, ModelASTValue value) {
        ModelASTTools tools = modelASTPipelineDef.getTools();
        if ( tools == null )
        {
            tools = new ModelASTTools( modelASTPipelineDef );
            modelASTPipelineDef.setTools( tools );
        }
        tools.getTools().put( key, value );
    }

    public static void addTrigger(ModelASTPipelineDef modelASTPipelineDef, ModelASTTrigger trigger) {
        ModelASTTriggers modelASTTriggers = modelASTPipelineDef.getTriggers();
        if ( modelASTTriggers == null )
        {
            modelASTTriggers = new ModelASTTriggers( modelASTPipelineDef );
            modelASTPipelineDef.setTriggers( modelASTTriggers );

        }
        modelASTTriggers.getTriggers().add( trigger );
    }

}
