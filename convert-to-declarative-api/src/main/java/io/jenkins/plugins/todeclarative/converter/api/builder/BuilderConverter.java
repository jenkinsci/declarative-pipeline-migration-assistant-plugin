package io.jenkins.plugins.todeclarative.converter.api.builder;

import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

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

}
