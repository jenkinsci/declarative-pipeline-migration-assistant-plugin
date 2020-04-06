package io.jenkins.plugins.todeclarative.converter.api;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;

import javax.annotation.Nonnull;

public abstract class BaseConverter implements ExtensionPoint {

    public abstract boolean canConvert(@Nonnull Object object);

    public abstract boolean convert(ConverterRequest request, ConverterResult result, Object target) throws ConverterException;

    public static ExtensionList<BaseConverter> all() {
        return ExtensionList.lookup(BaseConverter.class);
    }
}
