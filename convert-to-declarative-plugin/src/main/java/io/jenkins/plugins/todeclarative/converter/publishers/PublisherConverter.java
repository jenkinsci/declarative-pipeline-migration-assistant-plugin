package io.jenkins.plugins.todeclarative.converter.publishers;

import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface PublisherConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher );

    boolean canConvert( Publisher publisher );

}
