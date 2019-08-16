package io.jenkins.plugins.todeclarative.converter.api.scm;

import hudson.scm.SCM;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;

/**
 * All implementations of this extension will be used to convert {@link SCM}.
 * Your must modify the model from the {@link ConverterResult} instance.
 */
public interface ScmConverter
{
    void convert( ConverterRequest request, ConverterResult converterResult, SCM scm );

    /**
     * @param scm the SCM to convert
     * @return <code>true</code> if the extension is able to convert the {@link SCM}
     */
    boolean canConvert( SCM scm );

}
