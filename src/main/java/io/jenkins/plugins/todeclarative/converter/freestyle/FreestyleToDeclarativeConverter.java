package io.jenkins.plugins.todeclarative.converter.freestyle;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.ToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.jobproperty.JobPropertyConverter;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Extension
public class FreestyleToDeclarativeConverter implements ToDeclarativeConverter
{
    @Override
    public void convert( ConverterRequest converterRequest, ConverterResult converterResult )
    {
        FreeStyleProject freeStyleProject = (FreeStyleProject) converterRequest.getJob();

        convertJobProperties(converterRequest, converterResult, freeStyleProject.getProperties());

    }

    protected void convertJobProperties( ConverterRequest converterRequest, ConverterResult converterResult,
                                         Map<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> map ) {

        for( Map.Entry<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> entry : map.entrySet() ) {
            List<JobPropertyConverter> converters = findJobPropertyConverters( entry.getKey(), entry.getValue() );
            converters.stream().forEach( jobPropertyConverter ->
                                             jobPropertyConverter.convert( converterRequest,
                                                                           converterResult,
                                                                           entry.getKey(),
                                                                           entry.getValue() ) );
        }

    }

    protected List<JobPropertyConverter> findJobPropertyConverters( JobPropertyDescriptor jobPropertyDescriptor,
                                                                    JobProperty<? super FreeStyleProject> jobProperty) {

        List<JobPropertyConverter> converters = Jenkins.getInstance().getExtensionList( JobPropertyConverter.class );
        return converters.stream()
            .filter( converter -> converter.canConvert( jobPropertyDescriptor, jobProperty ) )
            .collect(Collectors.toList());
    }

    @Override
    public boolean canConvert( Job job )
    {
        return job instanceof FreeStyleProject;
    }
}
