package io.jenkins.plugins.todeclarative.actions;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.freestyle.FreestyleToDeclarativeConverter;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import java.util.Arrays;
import java.util.Collection;

public class ToDeclarativeAction
    implements Action, Describable<ToDeclarativeAction>
{

    public static final String JELLY_RESOURCES_PATH = "/io/jenkins/plugins/todeclarative/actions/ToDeclarativeAction/";
    //TODO make it dynamic
    // ToDeclarativeAction.class.getPackage().getName();

    private FreeStyleProject job;

    private String jenkinsFile;

    public ToDeclarativeAction( FreeStyleProject job )
    {
        this.job = job;
    }

    public void doConvert( final StaplerRequest request, final StaplerResponse response )
        throws Exception
    {
        // FIXME use boolean
        if (StringUtils.equalsIgnoreCase( request.getParameter( "jenkinsFileOnly" ), "on" ))
        {
            FreestyleToDeclarativeConverter converter =
                Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );
            ConverterRequest converterRequest = new ConverterRequest().job( job ).createProject( false );
            ConverterResult converterResult =
                new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );
            converter.convert( converterRequest, converterResult );
            this.jenkinsFile = converterResult.getModelASTPipelineDef().toPrettyGroovy();
            request.setAttribute( "Jenkinsfile", this.jenkinsFile);
            request.getView( this, JELLY_RESOURCES_PATH + "jenkinsfile.jelly" ).forward( request, response );
            return;
        }

        String newName = request.getParameter( "newName" );

        if (StringUtils.isNotBlank( newName ) && !StringUtils.equalsIgnoreCase( newName, job.getFullName() ) )
        {

            FreestyleToDeclarativeConverter converter =
                Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );
            ConverterRequest converterRequest =
                new ConverterRequest().job( job ).createdProjectName( request.getParameter( "newName" ) );
            ConverterResult converterResult =
                new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );
            converter.convert( converterRequest, converterResult );

            Jenkins.get().reload();
            response.sendRedirect2( converterResult.getJob().getAbsoluteUrl() );
            return;
        }
        response.forwardToPreviousPage( request );
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
