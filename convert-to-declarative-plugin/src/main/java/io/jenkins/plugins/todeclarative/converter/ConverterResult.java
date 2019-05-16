package io.jenkins.plugins.todeclarative.converter;

import hudson.model.Job;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;

public class ConverterResult
{
    private ModelASTPipelineDef modelASTPipelineDef = new ModelASTPipelineDef(this);

    private Job job;

    public ConverterResult()
    {
        // no op
    }

    public ModelASTPipelineDef getModelASTPipelineDef()
    {
        return modelASTPipelineDef;
    }

    public void setModelASTPipelineDef( ModelASTPipelineDef modelASTPipelineDef )
    {
        this.modelASTPipelineDef = modelASTPipelineDef;
    }

    public ConverterResult modelASTPipelineDef( ModelASTPipelineDef modelASTPipelineDef )
    {
        this.modelASTPipelineDef = modelASTPipelineDef;
        return this;
    }

    public Job getJob()
    {
        return job;
    }

    public void setJob( Job job )
    {
        this.job = job;
    }
}
