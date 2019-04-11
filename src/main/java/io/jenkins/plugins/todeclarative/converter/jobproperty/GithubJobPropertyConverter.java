package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;

import java.io.IOException;

@Extension
public class GithubJobPropertyConverter implements JobPropertyConverter
{
    @Override
    public boolean doConvert( ConverterRequest request, ConverterResult converterResult,
                                  JobPropertyDescriptor jobPropertyDescriptor,
                                  JobProperty jobProperty )
    {
        try
        {
            converterResult.getJob().addProperty( jobProperty );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor,
                               JobProperty jobProperty )
    {
        return jobProperty instanceof com.coravy.hudson.plugins.github.GithubProjectProperty;
    }
}
