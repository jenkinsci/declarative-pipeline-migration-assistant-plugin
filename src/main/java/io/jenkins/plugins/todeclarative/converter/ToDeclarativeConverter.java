package io.jenkins.plugins.todeclarative.converter;

import hudson.model.Job;

public interface ToDeclarativeConverter
{
    void convert(ConverterRequest converterRequest, ConverterResult converterResult);

    boolean canConvert( Job job);
}
