package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import hudson.tasks.BuildWrapper;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public interface BuildWrapperConverter
{
    ModelASTStage convert( ConverterRequest request, BuildWrapper wrapper );

    boolean canConvert( BuildWrapper wrapper );

}
