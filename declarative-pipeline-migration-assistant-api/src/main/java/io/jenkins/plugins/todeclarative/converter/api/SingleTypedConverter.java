package io.jenkins.plugins.todeclarative.converter.api;

import hudson.Functions;

import javax.annotation.Nonnull;

public abstract class SingleTypedConverter<T> extends BaseConverter {
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    protected SingleTypedConverter() {
        type = Functions.getTypeParameter(getClass(), SingleTypedConverter.class, 0);
    }

    @Override
    public boolean canConvert(@Nonnull Object object) {
        return type.isInstance(object);
    }
}
