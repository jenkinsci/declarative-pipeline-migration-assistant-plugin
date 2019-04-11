package io.jenkins.plugins.todeclarative.converter;

import hudson.model.Job;

public interface ToDeclarativeConverter
{
    void convert(ConverterRequest converterRequest, ConverterResult converterResult) throws ConverterException;

    boolean canConvert( Job job);
}
