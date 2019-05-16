package io.jenkins.plugins.todeclarative.converter.scm;

import hudson.scm.SCM;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;

public interface ScmConverter
{
    void convert( ConverterRequest request, ConverterResult converterResult, SCM scm );

    boolean canConvert( SCM scm );

}
