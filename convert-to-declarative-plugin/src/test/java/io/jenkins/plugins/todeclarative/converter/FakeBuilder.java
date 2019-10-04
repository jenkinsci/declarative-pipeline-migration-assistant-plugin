package io.jenkins.plugins.todeclarative.converter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

import java.io.IOException;

@Extension
public class FakeBuilder extends Builder
{
    @Override
    public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener )
        throws InterruptedException, IOException
    {
        return super.perform( build, launcher, listener );
    }
}
