package io.jenkins.plugins.todeclarative.converter.jobproperty;

import com.coravy.hudson.plugins.github.GithubProjectProperty;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.jobproperty.JobPropertyConverter;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OptionalExtension(requirePlugins = { "github" })
public class GithubJobPropertyConverter implements JobPropertyConverter
{

    private static final Logger LOGGER = LoggerFactory.getLogger( GithubJobPropertyConverter.class.getName());

    @Override
    public void convert( ConverterRequest request, ConverterResult converterResult,
                         JobPropertyDescriptor jobPropertyDescriptor,
                         JobProperty jobProperty )
    {
//            if(request.isCreateProject()) {
//                converterResult.getJob().addProperty( jobProperty );
//            }
        // nothing as we do not create project
    }

    @Override
    public boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor,
                               JobProperty jobProperty )
    {
        return jobProperty instanceof GithubProjectProperty;
    }
}
