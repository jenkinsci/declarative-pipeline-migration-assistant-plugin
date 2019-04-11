package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.tasks.LogRotator;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import jenkins.model.BuildDiscarder;
import jenkins.model.BuildDiscarderProperty;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKeyValueOrMethodCallPair;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
public class BuildDiscarderPropertyConverter
    implements JobPropertyConverter
{
    @Override
    public boolean doConvert( ConverterRequest request, ConverterResult converterResult, //
                                  JobPropertyDescriptor jobPropertyDescriptor, //
                                  JobProperty jobProperty )
    {

        BuildDiscarderProperty buildDiscarderProperty = (BuildDiscarderProperty) jobProperty;
        //options {
        //    buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
        // args String daysToKeepStr, String numToKeepStr, String artifactDaysToKeepStr, String artifactNumToKeepStr
        //}

        if(buildDiscarderProperty.getStrategy()==null){
            // nothing to do
            return true;
        }

        BuildDiscarder buildDiscarder = buildDiscarderProperty.getStrategy();
        if(!(buildDiscarder instanceof LogRotator )){
            // nothing to do
            return true;
        }

        LogRotator logRotator = (LogRotator) buildDiscarder;

        ModelASTOption option = new ModelASTOption( this );
        option.setName( "buildDiscarder" );

        ModelASTOption logRotatorOption = new ModelASTOption( this);
        logRotatorOption.setName( "logRotator" );
        option.getArgs().add( logRotatorOption );

        List<ModelASTMethodArg> rotatorArgs = new ArrayList<>();

        if( StringUtils.isNotBlank(logRotator.getArtifactDaysToKeepStr())){
            rotatorArgs.add( buildKeyPairArg("artifactDaysToKeepStr",  logRotator.getArtifactDaysToKeepStr()));
        }
        if( StringUtils.isNotBlank(logRotator.getDaysToKeepStr())){
            rotatorArgs.add( buildKeyPairArg("daysToKeepStr",  logRotator.getDaysToKeepStr()));
        }
        if( StringUtils.isNotBlank(logRotator.getNumToKeepStr())){
            rotatorArgs.add( buildKeyPairArg("numToKeepStr",  logRotator.getNumToKeepStr()));
        }
        if( StringUtils.isNotBlank(logRotator.getArtifactNumToKeepStr())){
            rotatorArgs.add( buildKeyPairArg("artifactNumToKeepStr",  logRotator.getArtifactNumToKeepStr()));
        }
        logRotatorOption.setArgs( rotatorArgs );

        ModelASTPipelineDef modelASTPipelineDef = converterResult.getModelASTPipelineDef();
        if(modelASTPipelineDef.getOptions()==null){
            modelASTPipelineDef.setOptions( new ModelASTOptions( this ) );
        }
        modelASTPipelineDef.getOptions().getOptions().add( option );

        return true;
    }


    private ModelASTKeyValueOrMethodCallPair buildKeyPairArg(String key, String value){
        ModelASTKey astKey = new ModelASTKey(this);
        astKey.setKey(key);
        ModelASTKeyValueOrMethodCallPair keyPairArg = new ModelASTKeyValueOrMethodCallPair(this);
        keyPairArg.setKey( astKey );
        keyPairArg.setValue( ModelASTValue.fromConstant(value, this) );
        return keyPairArg;
    }

    @Override
    public boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor, JobProperty jobProperty )
    {
        return jobProperty.getClass().isAssignableFrom( BuildDiscarderProperty.class );
    }
}
