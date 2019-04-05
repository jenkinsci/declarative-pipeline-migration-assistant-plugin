package io.jenkins.plugins.todeclarative.converter;

import hudson.model.Job;

public class ConverterRequest {
    private Job job;

    private boolean useWithMvn;

    public ConverterRequest()
    {
        //no op
    }

    public Job getJob()
    {
        return job;
    }

    public void setJob( Job job )
    {
        this.job = job;
    }

    public ConverterRequest job( Job job )
    {
        this.job = job;
        return this;
    }

    public boolean isUseWithMvn()
    {
        return useWithMvn;
    }

    public void setUseWithMvn( boolean useWithMvn )
    {
        this.useWithMvn = useWithMvn;
    }

    public ConverterRequest useWithMvn( boolean useWithMvn )
    {
        this.useWithMvn = useWithMvn;
        return this;
    }
}
