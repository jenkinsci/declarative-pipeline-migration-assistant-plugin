package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.tasks.BatchFile;
import hudson.tasks.Builder;
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
public class BatchFileConverter
    implements BuilderConverter
{
    public static final String BATCH_NUMBER_KEY = BatchFileConverter.class.getName() + ".shell.number";

    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder )
    {
        BatchFile batchFile = (BatchFile) builder;
        ModelASTStage stage = new ModelASTStage( this );
        int shellNumber = request.getInt( BATCH_NUMBER_KEY );
        stage.setName( "Batch script " + shellNumber );
        request.setInt( BATCH_NUMBER_KEY, ++shellNumber );
        ModelASTBranch branch = new ModelASTBranch( this );
        stage.setBranches( Arrays.asList( branch ) );
        ModelASTStep step = new ModelASTStep( this );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( this );
        // TODO olamy escape command?? not sure as might be done when running it
        singleArgument.setValue( ModelASTValue.fromConstant( batchFile.getCommand(), this ) );
        step.setArgs( singleArgument );
        step.setName( "bat" );

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
        return builder.getClass().isAssignableFrom( BatchFile.class );
    }
}
