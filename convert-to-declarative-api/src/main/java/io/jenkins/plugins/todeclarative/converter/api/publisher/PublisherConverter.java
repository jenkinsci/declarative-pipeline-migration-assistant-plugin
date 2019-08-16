package io.jenkins.plugins.todeclarative.converter.api.publisher;

import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

/**
 * All implementation of this extension will be used to convert {@link Publisher}.
 * Your extension can either return a {@link ModelASTStage} or modify the model from the {@link ConverterResult} instance
 */
public interface PublisherConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher );

    /**
     * @param publisher the publisher to convert
     * @return <code>true</code> if the extension is able to convert the {@link Publisher}
     */
    boolean canConvert( Publisher publisher );

}
