package io.jenkins.plugins.todeclarative.converter.api.builder;

import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface BuilderConverter
{
    ModelASTStage convert( ConverterRequest request, Builder builder );

    boolean canConvert(Builder builder);

}
