package io.jenkins.plugins.todeclarative.converter.builders;

import hudson.Extension;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ModelASTUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTScriptBlock;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Extension
public class ShellConverter implements BuilderConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, Builder builder )
    {
        Shell shell = (Shell) builder;
         shell.getCommand();
        ModelASTStage stage = new ModelASTStage( this );
        stage.setName( "Shell script" );
        ModelASTBranch branch = new ModelASTBranch( this );
        stage.setBranches( Arrays.asList(branch) );
        ModelASTScriptBlock modelASTScriptBlock = new ModelASTScriptBlock( this );
        branch.setSteps( Arrays.asList( modelASTScriptBlock ) );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( this );
        singleArgument.setValue( ModelASTValue.fromConstant( "sh \"" + ( (Shell) builder ).getCommand() + "\"" , this ));
        modelASTScriptBlock.setArgs( singleArgument );
        return stage;
    }

    @Override
    public boolean canConvert( Builder builder )
    {
        return builder.getClass().isAssignableFrom( Shell.class );
    }
}
