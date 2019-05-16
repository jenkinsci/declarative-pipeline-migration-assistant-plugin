package io.jenkins.plugins.todeclarative.converter.api.jobproperty;

import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;

public interface JobPropertyConverter
{
    /**
     *
     * @param request
     * @param converterResult
     * @param jobPropertyDescriptor
     * @param jobProperty
     * @return <code>true</code> if the method has converted the property
     */
    boolean doConvert( ConverterRequest request, ConverterResult converterResult,
                           JobPropertyDescriptor jobPropertyDescriptor,
                           JobProperty jobProperty );

    boolean canConvert( JobPropertyDescriptor jobPropertyDescriptor,
                        JobProperty jobProperty );

}
