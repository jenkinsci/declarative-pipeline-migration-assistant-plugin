package io.jenkins.plugins.todeclarative.converter.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Functions;


public abstract class SingleTypedConverter<T> extends BaseConverter {
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    protected SingleTypedConverter() {
        type = Functions.getTypeParameter(getClass(), SingleTypedConverter.class, 0);
    }

    @Override
    public boolean canConvert(@NonNull Object object) {
        return type.isInstance(object);
    }
}
