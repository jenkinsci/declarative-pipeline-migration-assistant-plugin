package io.jenkins.plugins.sample.io.jenkins.plugins.todeclarative;

import com.coravy.hudson.plugins.github.GithubProjectProperty;
import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.Slave;
import hudson.model.StringParameterDefinition;
import hudson.tasks.LogRotator;
import hudson.tasks.Shell;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.freestyle.FreestyleToDeclarativeConverter;
import jenkins.model.BuildDiscarderProperty;
import jenkins.model.Jenkins;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;

public class FreestyleTest
{

    @Rule
    public JenkinsRule j  = new JenkinsRule();

    @Test
    public void freestyle_conversion() throws Exception {

        Slave slave = j.createOnlineSlave();
        slave.setLabelString( "FOO_AGENT" );

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );
        p.addProperty( new GithubProjectProperty( "http://github.com/beer/paleale") );

        //int daysToKeep, int numToKeep, int artifactDaysToKeep, int artifactNumToKeep
        LogRotator logRotator = new LogRotator(1, 2,3, 4);
        BuildDiscarderProperty buildDiscarderProperty = new BuildDiscarderProperty( logRotator );
        p.addProperty( buildDiscarderProperty );

        RequiredResourcesProperty requiredResourcesProperty =
            new RequiredResourcesProperty( "beer", null, null, "labelName", null);
        p.addProperty( requiredResourcesProperty );

        List<ParameterDefinition> parametersDefinitions = new ArrayList<>();
        parametersDefinitions.add( new StringParameterDefinition( "str", "defaultValue", "description str", true ) );
        // List<String> toGroovy needs to be fixed
        //parametersDefinitions.add( new ChoiceParameterDefinition( "choice", new String[]{"choice1","choice2"}, "description choice" ) );
        parametersDefinitions.add( new BooleanParameterDefinition("nameboolean", true, "boolean description") );
        ParametersDefinitionProperty parametersDefinitionProperty = new ParametersDefinitionProperty(parametersDefinitions);
        p.addProperty( parametersDefinitionProperty );


        p.getBuildersList().add( new Shell( "pwd" ) );

        FreestyleToDeclarativeConverter converter = Jenkins.get()
            .getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );

        Assert.assertTrue( converter.canConvert( p ) );

        ConverterRequest request = new ConverterRequest().job( p ).createdProjectName( "foo-beer" );
        ConverterResult converterResult = new ConverterResult()
            .modelASTPipelineDef( new ModelASTPipelineDef(null));

        converter.convert( request, converterResult);
        String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );

        System.out.println( converterResult.getJob().getProperties() );

        WorkflowRun run =( (WorkflowJob)converterResult.getJob()).scheduleBuild2( 0).get();
        j.waitForCompletion( run );
        j.assertBuildStatus( Result.SUCCESS, run);

    }

}
