package io.jenkins.plugins.todeclarative.converter;

import hudson.model.Job;

public class ConverterRequest {
    private Job job;

    private boolean useWithMvn;

    private boolean createProject;

    private String createdProjectName;

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

    public boolean isCreateProject()
    {
        return createProject;
    }

    public void setCreateProject( boolean createProject )
    {
        this.createProject = createProject;
    }

    public ConverterRequest createProject( boolean createProject )
    {
        this.createProject = createProject;
        return this;
    }

    public String getCreatedProjectName()
    {
        return createdProjectName;
    }

    public void setCreatedProjectName( String createdProjectName )
    {
        this.createProject = true;
        this.createdProjectName = createdProjectName;
    }

    public ConverterRequest createdProjectName( String createdProjectName )
    {
        this.createProject = true;
        this.createdProjectName = createdProjectName;
        return this;
    }
}
