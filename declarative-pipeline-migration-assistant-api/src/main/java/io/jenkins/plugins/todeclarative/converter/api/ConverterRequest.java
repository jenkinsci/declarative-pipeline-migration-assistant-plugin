package io.jenkins.plugins.todeclarative.converter.api;

import hudson.model.Job;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains data needed for converting a {@link Job} to a declarative pipeline
 */
public class ConverterRequest
{
    private Job job;

    private boolean createNewProject;

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

    public boolean isCreateNewProject() {
        return createNewProject;
    }

    public void setCreateNewProject(boolean createNewProject) {
        this.createNewProject = createNewProject;
    }

    public ConverterRequest createNewProject(boolean createNewProject) {
        this.createNewProject = createNewProject;
        return this;
    }

    /**
     * convenient method to have a counter associated with a key.
     * @param key the counter key
     * @return retrieve the counter value and add 1
     */
    public int getAndIncrement( String key )
    {
        if ( !context.containsKey( key ) )
        {
            context.put( key, 0 );
        }
        int value = (int) context.get( key );
        context.put( key, value + 1 );
        return value;
    }

}
