package io.jenkins.plugins.todeclarative.converter.api;

import hudson.model.Job;

/**
 * Main interface which convert a Job type to a declarative pipeline
 */
public interface ToDeclarativeConverter
{
    void convert(ConverterRequest converterRequest, ConverterResult converterResult) throws ConverterException;

    boolean canConvert( Job job);
}
