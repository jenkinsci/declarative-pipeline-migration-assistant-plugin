package io.jenkins.plugins.todeclarative.converter.api.builder;

import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface BuilderConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder );

    boolean canConvert( Builder builder );

}
