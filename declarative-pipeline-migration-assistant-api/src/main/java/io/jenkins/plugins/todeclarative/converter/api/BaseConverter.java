package io.jenkins.plugins.todeclarative.converter.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

public abstract class BaseConverter implements ExtensionPoint {

    public abstract boolean canConvert(@NonNull Object object);

    public abstract boolean convert(ConverterRequest request, ConverterResult result, Object target) throws ConverterException;

    public static ExtensionList<BaseConverter> all() {
        return ExtensionList.lookup(BaseConverter.class);
    }
}
