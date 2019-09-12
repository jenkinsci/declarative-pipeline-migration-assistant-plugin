package io.jenkins.plugins.todeclarative.actions;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import io.jenkins.plugins.todeclarative.converter.freestyle.FreestyleToDeclarativeConverter;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ToDeclarativeAction
    implements Action, Describable<ToDeclarativeAction>
{

    private FreeStyleProject job;

    private String jenkinsFile;

    private List<Warning> warnings;

    private Exception error;

    public ToDeclarativeAction( FreeStyleProject job )
    {
        this.job = job;
    }

    public String doConvert()
        throws Exception
    {
        try
        {
            FreestyleToDeclarativeConverter converter = Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );
            ConverterRequest converterRequest = new ConverterRequest().job( job ).createProject( false );
            ConverterResult converterResult = new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );
            converter.convert( converterRequest, converterResult );
//            if(true)
//            {
//                throw new Exception( "Something really bad happened with this bloody conversion" );
//            }
            this.jenkinsFile = converterResult.getModelASTPipelineDef().toPrettyGroovy();
            this.warnings = converterResult.getWarnings();
            //this.warnings.add( new Warning( "Not good", "Awesome plugin class" ) );
            return jenkinsFile;
        } catch ( Exception e )
        {
            this.error = e;
        }
        return null;
    }

    public Exception getError()
    {
        return error;
    }

    public List<Warning> getWarnings()
    {
        return warnings;
    }

    @CheckForNull
    @Override
    public String getIconFileName()
    {
        // TODO: Use real icon.
        return "foo";
    }

    @CheckForNull
    @Override
    public String getDisplayName()
    {
        return "To Declarative";
    }

    @CheckForNull
    @Override
    public String getUrlName()
    {
        return "todeclarative";
    }

    public String getJenkinsFile()
    {
        return jenkinsFile;
    }

    public void setJenkinsFile( String jenkinsFile )
    {
        this.jenkinsFile = jenkinsFile;
    }

    @Override
    public Descriptor<ToDeclarativeAction> getDescriptor()
    {
        return Jenkins.get().getDescriptorOrDie( getClass() );
    }

    @Extension
    public static class ActionInjector
        extends TransientActionFactory<FreeStyleProject>
    {
        @Override
        public Collection<ToDeclarativeAction> createFor( FreeStyleProject p )
        {
            return Arrays.asList( new ToDeclarativeAction( p ) );
        }

        @Override
        public Class type()
        {
            return FreeStyleProject.class;
        }
    }


    @Extension
    public static final class ToDeclarativeActionDescriptor
        extends Descriptor<ToDeclarativeAction>
    {

        /**
         * TODO Validate new name by checking if any existing job exists with same name at current level.
         *
         * @param newName Desired new name of new pipeline job.
         * @return Form Validation response with error message if any.
         */
        public FormValidation doCheckNewName( @QueryParameter( "newName" ) String newName,
                                              @AncestorInPath FreeStyleProject job )
        {

            return FormValidation.ok();
        }


    }

}
