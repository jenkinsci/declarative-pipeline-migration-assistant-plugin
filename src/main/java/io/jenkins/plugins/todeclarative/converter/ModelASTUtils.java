package io.jenkins.plugins.todeclarative.converter;

import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.List;

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

    public static ModelASTKeyValueOrMethodCallPair buildKeyPairArg( String key, List<String> value){
        // FIXME
        return null;
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
