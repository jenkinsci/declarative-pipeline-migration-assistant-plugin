package io.jenkins.plugins.todeclarative.converter.api;

import hudson.model.Job;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ConverterResult
{
    private ModelASTPipelineDef modelASTPipelineDef = new ModelASTPipelineDef(this);

    private List<Supplier<ModelASTTreeStep>> wrappingTreeSteps = new ArrayList<>( );

    private List<Warning> warnings = new ArrayList<>( );

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

    public void addWrappingTreeStep(Supplier<ModelASTTreeStep> treeStep)
    {
        this.wrappingTreeSteps.add( treeStep );
    }

    /**
     * @return List of {@link ModelASTTreeStep} can be a Tree of withCredential, configFileProvider etc..
     *          everything which need to wrap around builders
     */
    public List<Supplier<ModelASTTreeStep>> getWrappingTreeSteps()
    {
        return wrappingTreeSteps;
    }

    public List<Warning> getWarnings()
    {
        return warnings;
    }

    public void setWarnings( List<Warning> warnings )
    {
        this.warnings = warnings;
    }

    public void addWarning( Warning warning )
    {
        this.warnings.add( warning );
    }

    public static class Warning {
        private String message;
        private String pluginClassName;

        public Warning()
        {
            // nop
        }

        public Warning( String message, String pluginClassName )
        {
            this.message = message;
            this.pluginClassName = pluginClassName;
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage( String message )
        {
            this.message = message;
        }

        public String getPluginClassName()
        {
            return pluginClassName;
        }

        public void setPluginClassName( String pluginClassName )
        {
            this.pluginClassName = pluginClassName;
        }

        @Override
        public String toString()
        {
            return "Warning{" + "message='" + message + '\'' + ", pluginClassName='" + pluginClassName + '\'' + '}';
        }
    }
}
