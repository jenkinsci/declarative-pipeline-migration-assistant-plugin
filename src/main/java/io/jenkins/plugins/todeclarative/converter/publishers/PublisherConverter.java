package io.jenkins.plugins.todeclarative.converter.publishers;

import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface PublisherConverter
{
    ModelASTStage convert( ConverterRequest request, Publisher publisher );

    boolean canConvert( Publisher publisher );

}
