package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

public class GithubJobPropertyConverter implements JobPropertyConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult,
                                  JobPropertyDescriptor jobPropertyDescriptor,
                                  JobProperty<? super FreeStyleProject> jobProperty )
    {
        return null;
    }

    @Override
    public boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor,
                               JobProperty<? super FreeStyleProject> jobProperty )
    {
        return false;
    }
}
