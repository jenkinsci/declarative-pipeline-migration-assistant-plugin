package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.tasks.Maven;
import io.jenkins.plugins.todeclarative.converter.api.ConverterException;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;

@Extension(ordinal = 10)
/**
 * we just want to make sure the ordinal ordering works and we pick the smaller one and not this one
 */
public class FakeMavenConverter extends SingleTypedConverter<Maven> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) throws ConverterException {
        return true;
    }
}
