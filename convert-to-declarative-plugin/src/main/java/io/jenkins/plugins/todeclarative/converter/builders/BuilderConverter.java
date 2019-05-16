package io.jenkins.plugins.todeclarative.converter.builders;

import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface BuilderConverter
{
    ModelASTStage convert( ConverterRequest request, Builder builder );

    boolean canConvert(Builder builder);

}
