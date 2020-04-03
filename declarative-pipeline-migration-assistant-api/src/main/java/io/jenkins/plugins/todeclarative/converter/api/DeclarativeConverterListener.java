package io.jenkins.plugins.todeclarative.converter.api;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.FreeStyleProject;

/**
 * Receive notifications about conversions from freestyle projects to declarative pipelines
 */
public abstract class DeclarativeConverterListener implements ExtensionPoint {

    /**
     * Called when a freestyle job has been converted to a declarative pipeline
     */
    public void onConversion(FreeStyleProject job, ConverterResult conversionResult) {
    }

    public static void fire(FreeStyleProject job, ConverterResult conversionResult) {
        ExtensionList.lookup(DeclarativeConverterListener.class).forEach(listener -> {listener.onConversion(job, conversionResult);});
    }
}
