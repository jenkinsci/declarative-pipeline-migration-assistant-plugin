package io.jenkins.plugins.todeclarative.converter.api.buildwrapper;

import hudson.tasks.BuildWrapper;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface BuildWrapperConverter
{
    ModelASTStage convert( ConverterRequest request, BuildWrapper wrapper );

    boolean canConvert( BuildWrapper wrapper );

}
