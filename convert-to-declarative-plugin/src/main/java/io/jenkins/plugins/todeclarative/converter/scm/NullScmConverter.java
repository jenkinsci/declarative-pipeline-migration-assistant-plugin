package io.jenkins.plugins.todeclarative.converter.scm;

import hudson.Extension;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.scm.ScmConverter;

@Extension
public class NullScmConverter
    implements ScmConverter
{
    @Override
    public void convert( ConverterRequest request, ConverterResult converterResult, SCM scm )
    {
        // nothing here
        // just no more warning
    }

    @Override
    public boolean canConvert( SCM scm )
    {
        return scm instanceof NullSCM;
    }
}
