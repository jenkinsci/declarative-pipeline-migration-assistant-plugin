package io.jenkins.plugins.todeclarative.converter.api.buildwrapper;

import hudson.tasks.BuildWrapper;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

/**
 * All implementation of this extension will be used to convert {@link BuildWrapper}.
 * Your extension can either return a {@link ModelASTStage} or modify the model from the {@link ConverterResult} instance
 */
public interface BuildWrapperConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, BuildWrapper wrapper );

    /**
     * @param wrapper the builder to convert
     * @return <code>true</code> if the extension is able to convert the {@link BuildWrapper}
     */
    boolean canConvert( BuildWrapper wrapper );

}
