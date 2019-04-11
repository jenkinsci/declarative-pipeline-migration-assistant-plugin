package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import jenkins.model.BuildDiscarderProperty;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.pipeline.modeldefinition.validator.ModelValidator;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Extension
public class RequiredResourcesPropertyPropertyConverter
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
        //  daysToKeepStr,  numToKeepStr,  artifactDaysToKeepStr,  artifactNumToKeepStr
        //}
        ModelASTOption option = new ModelASTOption( this );
        option.setName( "buildDiscarder" );
        ModelASTOption logRotator = new ModelASTOption( this);
        logRotator.setName( "logRotator" );

        Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
        ModelASTKey key = new ModelASTKey(this);
        key.setKey( "withMvnKey" );
        ModelASTValue value = ModelASTValue.fromConstant( "withMvnArgs", this);
        args.put( key, value);
        logRotator.getArgs().add( args.values().iterator().next() );

        option.getArgs().add( logRotator );

        ModelASTPipelineDef modelASTPipelineDef = converterResult.getModelASTPipelineDef();
        if(modelASTPipelineDef.getOptions()==null){
            modelASTPipelineDef.setOptions( new ModelASTOptions( this ) );
        }
        modelASTPipelineDef.getOptions().getOptions().add( option );

        return true;
    }

    @Override
    public boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor, JobProperty jobProperty )
    {
        return jobProperty.getClass().isAssignableFrom( RequiredResourcesProperty.class );
    }
}
