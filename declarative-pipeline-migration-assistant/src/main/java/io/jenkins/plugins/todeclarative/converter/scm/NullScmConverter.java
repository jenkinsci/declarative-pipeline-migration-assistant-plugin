package io.jenkins.plugins.todeclarative.converter.scm;

import hudson.Extension;
import hudson.scm.NullSCM;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.NoPublisherConverter;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;

/**
 * Unlike {@link NoPublisherConverter}, this converter is valid
 * and not used for reporting warnings.
 */
@Extension
public class NullScmConverter extends SingleTypedConverter<NullSCM> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        // nothing here
        // just no more warning
        return true;
    }
}
