package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.model.FreeStyleProject;
import hudson.tasks.LogRotator;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import jenkins.model.BuildDiscarderProperty;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BuildDiscarderPropertyConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        //int daysToKeep, int numToKeep, int artifactDaysToKeep, int artifactNumToKeep
        LogRotator logRotator = new LogRotator( 1, 2, 3, 4);
        BuildDiscarderProperty buildDiscarderProperty = new BuildDiscarderProperty( logRotator );
        p.addProperty( buildDiscarderProperty );

        BuildDiscarderPropertyConverter converter =
            j.jenkins.getExtensionList( BuildDiscarderPropertyConverter.class ).get( 0 );

        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, null, buildDiscarderProperty );
        ModelASTOptions options = result.getModelASTPipelineDef().getOptions();
        assertEquals( 1, options.getOptions().size() );
        ModelASTOption option = options.getOptions().get( 0 );
        assertEquals( "buildDiscarder", option.getName() );
        String groovy = option.toGroovy();
        assertThat( groovy, CoreMatchers.containsString( "daysToKeepStr: '1'" ) );
        assertThat( groovy, CoreMatchers.containsString( "numToKeepStr: '2'" ) );
        assertThat( groovy, CoreMatchers.containsString( "artifactDaysToKeepStr: '3'" ) );
        assertThat( groovy, CoreMatchers.containsString( "artifactNumToKeepStr: '4'" ) );
    }
}
