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
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;
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
        shell.getCommand();
        ModelASTStage stage = new ModelASTStage( this );
        int shellNumber = request.getInt( SHELL_NUMBER_KEY );
        stage.setName( "Shell script " + shellNumber );
        request.setInt( SHELL_NUMBER_KEY, ++shellNumber );
        ModelASTBranch branch = new ModelASTBranch( this );
        stage.setBranches( Arrays.asList( branch ) );
        ModelASTStep step = new ModelASTStep( this );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( this );
        // TODO olamy escape shell command?? not sure as might be done when running it
        singleArgument.setValue( ModelASTValue.fromConstant( ( (Shell) builder ).getCommand(), this ) );
        step.setArgs( singleArgument );
        step.setName( "sh" );

        if(request.getWithCredentials().get()!=null)
        {
            // FIXME make a deep clone of WithCredentials to not using same instance all the time!!
            ModelASTTreeStep treeStep = request.getWithCredentials().get();
            treeStep.getChildren().add( step );
            branch.getSteps().add(treeStep);
        } else {
            branch.setSteps( Arrays.asList( step ) );
        }



        return stage;
    }

    @Override
    public boolean canConvert( Builder builder )
    {
        return builder.getClass().isAssignableFrom( Shell.class );
    }
}
