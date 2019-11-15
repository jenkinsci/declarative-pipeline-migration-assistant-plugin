package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.builder.BuilderConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.Arrays;


@Extension
public class ShellConverter
    implements BuilderConverter
{
    public static final String SHELL_NUMBER_KEY = ShellConverter.class.getName() + ".shell.number";

    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder )
    {
        Shell shell = (Shell) builder;
        ModelASTStage stage = new ModelASTStage( this );
        int stageNumber = request.getAndIncrement( SHELL_NUMBER_KEY );
        stage.setName( "Shell script " + stageNumber );
        ModelASTBranch branch = new ModelASTBranch( this );
        stage.setBranches( Arrays.asList( branch ) );
        ModelASTStep step = new ModelASTStep( this );
        step.setName( "sh" );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( this );
        singleArgument.setValue( ModelASTValue.fromConstant( shell.getCommand(), this ) );
        step.setArgs( singleArgument );
        wrapBranch(converterResult, step, branch);

        return stage;
    }

    @Override
    public boolean canConvert( Builder builder )
    {
        return builder.getClass().isAssignableFrom( Shell.class );
    }
}
