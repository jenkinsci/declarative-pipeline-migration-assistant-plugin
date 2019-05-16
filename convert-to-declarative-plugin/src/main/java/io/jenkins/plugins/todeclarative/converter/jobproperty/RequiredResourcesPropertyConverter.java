package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import org.apache.commons.lang3.StringUtils;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;

import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.todeclarative.converter.ModelASTUtils.buildKeyPairArg;

@Extension
public class RequiredResourcesPropertyConverter
    implements JobPropertyConverter
{
    @Override
    public boolean doConvert( ConverterRequest request, ConverterResult converterResult, //
                                  JobPropertyDescriptor jobPropertyDescriptor, //
                                  JobProperty jobProperty )
    {

        RequiredResourcesProperty requiredResourcesProperty = (RequiredResourcesProperty) jobProperty;

        //lock(label: 'label', resource: 'resource')
        ModelASTOption option = new ModelASTOption( this );
        option.setName( "lock" );

        List<ModelASTMethodArg> lockArgs = new ArrayList<>();

        if( StringUtils.isNotBlank(requiredResourcesProperty.getLabelName())){
            lockArgs.add( buildKeyPairArg("label", requiredResourcesProperty.getLabelName()));
        }

        if( StringUtils.isNotBlank(requiredResourcesProperty.getResourceNames())){
            lockArgs.add( buildKeyPairArg("resource", requiredResourcesProperty.getResourceNames()));
        }

        option.setArgs( lockArgs );

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
