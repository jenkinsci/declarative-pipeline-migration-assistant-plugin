package io.jenkins.plugins.todeclarative.converter;

import hudson.model.Job;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConverterRequest
{
    private Job job;

    private boolean useWithMvn;

    private boolean createProject;

    private String createdProjectName;

    private Supplier<ModelASTTreeStep> withCredentials;

    /**
     * Map to store some values related to the current conversion
     * if you use that please add the key name as a constant
     * or key name must contains a class name
     */
    private Map<String, Object> context = new HashMap<>();

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

    public int getInt( String key )
    {
        if ( !context.containsKey( key ) )
        {
            context.put( key, 0 );
        }
        return (int) context.get( key );
    }

    public void setInt( String key, int value )
    {
        context.put( key, value );
    }

    public Supplier<ModelASTTreeStep> getWithCredentials()
    {
        return withCredentials == null ? ()-> null : withCredentials;
    }

    public void setWithCredentials( Supplier<ModelASTTreeStep> withCredentials )
    {
        this.withCredentials = withCredentials;
    }
}
