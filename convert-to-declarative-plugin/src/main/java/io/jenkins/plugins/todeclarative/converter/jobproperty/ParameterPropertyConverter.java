package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.Extension;
import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import io.jenkins.plugins.todeclarative.converter.api.jobproperty.JobPropertyConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildParameter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildParameters;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;

import java.util.ArrayList;
import java.util.List;

@Extension
public class ParameterPropertyConverter
    implements JobPropertyConverter
{
    @Override
    public void convert( ConverterRequest request, ConverterResult converterResult, //
                         JobPropertyDescriptor jobPropertyDescriptor, //
                         JobProperty jobProperty )
    {
        ParametersDefinitionProperty parametersDefinitionProperty = (ParametersDefinitionProperty)jobProperty;
        if(parametersDefinitionProperty.getParameterDefinitions().isEmpty()){
            return;
        }
        // now in the Jenkinsfile as well
        ModelASTPipelineDef model = converterResult.getModelASTPipelineDef();
        if(model.getParameters()==null){
            model.setParameters( new ModelASTBuildParameters( this ) );
        }

        parametersDefinitionProperty.getParameterDefinitions() //
            .forEach( parameterDefinition -> {
                ModelASTBuildParameter parameter = build(parameterDefinition, converterResult);
                if(parameter!=null){
                    model.getParameters().getParameters().add(parameter);
                }
            } );
    }

    protected ModelASTBuildParameter build( ParameterDefinition parameterDefinition, ConverterResult converterResult ){
        // maybe an extension point here if users have their parameter definition type?
        List<ModelASTMethodArg> args = new ArrayList<>();
        if( StringParameterDefinition.class.getSimpleName().equals(parameterDefinition.getType())){
            ModelASTBuildParameter parameter = new ModelASTBuildParameter(this);
            parameter.setName( "string" );
            args.add( ModelASTUtils.buildKeyPairArg( "name", parameterDefinition.getName()));
            args.add( ModelASTUtils.buildKeyPairArg( "defaultValue", ((StringParameterDefinition)parameterDefinition).getDefaultValue()));
            args.add( ModelASTUtils.buildKeyPairArg( "description", parameterDefinition.getDescription()));
            parameter.setArgs( args );
            return parameter;
        }

        // FIXME List<String> for choices cannot be converted
        if( ChoiceParameterDefinition.class.getSimpleName().equals( parameterDefinition.getType())){
            ModelASTBuildParameter parameter = new ModelASTBuildParameter(this);
            parameter.setName( "choice" );
            args.add( ModelASTUtils.buildKeyPairArg( "name", parameterDefinition.getName()));
            args.add( ModelASTUtils.buildKeyPairArg( "choices", ((ChoiceParameterDefinition)parameterDefinition).getChoices()));
            args.add( ModelASTUtils.buildKeyPairArg( "description", parameterDefinition.getDescription()));
            parameter.setArgs( args );
            return parameter;
        }

        if( BooleanParameterDefinition.class.getSimpleName().equals( parameterDefinition.getType())){
            ModelASTBuildParameter parameter = new ModelASTBuildParameter(this);
            parameter.setName( "booleanParam" );
            args.add( ModelASTUtils.buildKeyPairArg( "name", parameterDefinition.getName()));
            args.add( ModelASTUtils.buildKeyPairArg( "defaultValue", ((BooleanParameterDefinition)parameterDefinition).getDefaultParameterValue().value));
            args.add( ModelASTUtils.buildKeyPairArg( "description", parameterDefinition.getDescription()));
            parameter.setArgs( args );
            return parameter;
        }

        converterResult.addWarning( new Warning( "Cannot convert property of type: " + parameterDefinition.getType(),
                                                 getClass().getName() ) );
        return null;
    }



    @Override
    public boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor, JobProperty jobProperty )
    {
        return jobProperty.getClass().isAssignableFrom( ParametersDefinitionProperty.class );
    }
}
