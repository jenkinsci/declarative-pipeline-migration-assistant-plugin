package io.jenkins.plugins.todeclarative.converter.trigger;

import hudson.Extension;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import java.util.Arrays;

@Extension
public class SCMTriggerConverter extends SingleTypedConverter<SCMTrigger>
{
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        SCMTrigger scmTrigger = (SCMTrigger) target;

        String cronValue = scmTrigger.getSpec();

        ModelASTTrigger modelASTTrigger = new ModelASTTrigger( this );
        modelASTTrigger.setName( "pollSCM" );
        modelASTTrigger.setArgs( Arrays.asList(ModelASTValue.fromConstant( cronValue, this )) );

        ModelASTUtils.addTrigger( result.getModelASTPipelineDef(), modelASTTrigger );
        return true;
    }
}
