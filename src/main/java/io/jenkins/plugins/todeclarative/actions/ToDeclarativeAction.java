package io.jenkins.plugins.todeclarative.actions;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
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

    private FreeStyleProject job;

    public ToDeclarativeAction(FreeStyleProject job) {
        this.job = job;
    }

    public void doConvert( final StaplerRequest request, final StaplerResponse response) throws Exception
    {
        if(!StringUtils.equalsIgnoreCase(request.getParameter( "newName"),job.getFullName())) {

            FreestyleToDeclarativeConverter converter = Jenkins.get()
                .getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );
            ConverterRequest converterRequest = new ConverterRequest().job( job )
                .createdProjectName( request.getParameter( "newName" ));
            ConverterResult converterResult = new ConverterResult()
                .modelASTPipelineDef( new ModelASTPipelineDef( null));
            converter.convert( converterRequest, converterResult );

            Jenkins.get().reload();
            response.sendRedirect2(converterResult.getJob().getAbsoluteUrl());
        } else {
            response.forwardToPreviousPage(request);
        }
    }

    @CheckForNull
    @Override
    public String getIconFileName()
    {
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

    @Override
    public Descriptor<ToDeclarativeAction> getDescriptor()
    {
        return Jenkins.get().getDescriptorOrDie( getClass() );
    }

    @Extension
    public static class ActionInjector extends TransientActionFactory<FreeStyleProject>
    {
        @Override
        public Collection<ToDeclarativeAction> createFor( FreeStyleProject p) {
            return Arrays.asList(new ToDeclarativeAction(p));
        }
        @Override
        public Class type() {
            return FreeStyleProject.class;
        }
    }



    @Extension
    public static final class ToDeclarativeActionDescriptor extends Descriptor<ToDeclarativeAction> {

        /**
         * TODO Validate new name by checking if any existing job exists with same name at current level.
         *
         * @param newName Desired new name of new pipeline job.
         *
         * @return Form Validation response with error message if any.
         */
        public FormValidation doCheckNewName( @QueryParameter("newName") String newName, @AncestorInPath FreeStyleProject job ) {

            return FormValidation.ok();
        }


    }

}
