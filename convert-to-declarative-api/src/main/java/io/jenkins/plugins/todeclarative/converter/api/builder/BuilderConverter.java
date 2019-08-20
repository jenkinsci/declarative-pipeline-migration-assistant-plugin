package io.jenkins.plugins.todeclarative.converter.api.builder;

import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * All implementation of this extension will be used to convert {@link Builder}.
 * Your extension can either return a {@link ModelASTStage} or modify the model from the {@link ConverterResult} instance
 */
public interface BuilderConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder );

    /**
     *
     * @param builder the builder to convert
     * @return <code>true</code> if the extension is able to convert the {@link Builder}
     */
    boolean canConvert( Builder builder );

    /**
     * if needed this will wrap your step with build wrapper such withCredential, configFile
     *
     * @param converterResult the current result
     * @param mainStep the step to add/wrap
     * @param mainBranch the current branch
     */
    default void wrapBranch( ConverterResult converterResult, ModelASTStep mainStep, ModelASTBranch mainBranch )
    {
        if ( !converterResult.getWrappingTreeSteps().isEmpty() )
        {
            Iterator<Supplier<ModelASTTreeStep>> treeStepsIterator = converterResult.getWrappingTreeSteps().iterator();
            ModelASTTreeStep treeStep = treeStepsIterator.next().get();
            ModelASTTreeStep last = treeStep;
            while ( treeStepsIterator.hasNext() )
            {
                last = treeStepsIterator.next().get();
                treeStep.getChildren().add( last );
            }
            last.getChildren().add( mainStep );
            mainBranch.setSteps( Arrays.asList( treeStep ) );
        }
        else
        {
            mainBranch.setSteps( Arrays.asList( mainStep ) );
        }
    }

}
