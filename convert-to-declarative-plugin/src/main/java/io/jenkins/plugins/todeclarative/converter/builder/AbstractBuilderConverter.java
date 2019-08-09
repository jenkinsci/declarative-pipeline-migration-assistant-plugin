package io.jenkins.plugins.todeclarative.converter.builder;

import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.builder.BuilderConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

public abstract class AbstractBuilderConverter
    implements BuilderConverter
{

    /**
     * if needed this will wrap your step with build wrapper such withCredential, configFile
     *
     * @param converterResult the current result
     * @param mainStep the step to add/wrap
     * @param mainBranch the current branch
     */
    protected void wrapBranch( ConverterResult converterResult, ModelASTStep mainStep, ModelASTBranch mainBranch )
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
