package io.jenkins.plugins.todeclarative.converter.api.scm;

import hudson.scm.SCM;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;

public interface ScmConverter
{
    void convert( ConverterRequest request, ConverterResult converterResult, SCM scm );

    boolean canConvert( SCM scm );

}
