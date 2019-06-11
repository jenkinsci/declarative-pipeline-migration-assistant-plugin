package io.jenkins.plugins.todeclarative.converter.api.publisher;

import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface PublisherConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher );

    boolean canConvert( Publisher publisher );

}
