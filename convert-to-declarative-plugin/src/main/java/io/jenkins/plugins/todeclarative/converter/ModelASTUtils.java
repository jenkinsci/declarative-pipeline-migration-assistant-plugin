package io.jenkins.plugins.todeclarative.converter;

import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.List;
import java.util.Optional;

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

//
//    public static class StringList extends ModelASTValue {
//
//        public StringList(Object sourceLocation, List<String> v) {
//            super(sourceLocation,v);
//        }
//        @Override
//        public String toGroovy()
//        {
//            return null;
//        }
//
//        @Override
//        public boolean isLiteral()
//        {
//            return true;
//        }
//    }
}
