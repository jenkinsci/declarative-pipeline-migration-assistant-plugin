package io.jenkins.plugins.todeclarative.converter.trigger;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import jenkins.triggers.ReverseBuildTrigger;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class ReverseBuildTriggerConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        ReverseBuildTrigger trigger = new ReverseBuildTrigger( "foo,bar" );
        trigger.setThreshold( null );
        p.addTrigger( trigger );

        ReverseBuildTriggerConverter converter = j.jenkins
            .getExtensionList( ReverseBuildTriggerConverter.class ).get( 0 );

        assertTrue(converter.canConvert( trigger ) );

        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest().job( p ), result, trigger );

        List<ModelASTTrigger> triggers = result.getModelASTPipelineDef().getTriggers().getTriggers();
        assertEquals( 1, triggers.size() );

        String groovy = result.getModelASTPipelineDef().getTriggers().toGroovy();
        System.out.println( groovy );
        assertThat( groovy, containsString( "foo,bar" ) );
        assertThat( groovy, containsString( "upstreamProjects" ) );
    }

    @Test
    public void simpletestwiththreshold()
        throws Exception
    {
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        ReverseBuildTrigger trigger = new ReverseBuildTrigger( "foo,bar" );
        trigger.setThreshold( Result.FAILURE );
        p.addTrigger( trigger );

        ReverseBuildTriggerConverter converter = j.jenkins
            .getExtensionList( ReverseBuildTriggerConverter.class ).get( 0 );

        assertTrue(converter.canConvert( trigger ) );

        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest().job( p ), result, trigger );

        List<ModelASTTrigger> triggers = result.getModelASTPipelineDef().getTriggers().getTriggers();
        assertEquals( 1, triggers.size() );

        String groovy = result.getModelASTPipelineDef().getTriggers().toGroovy();
        System.out.println( groovy );
        assertThat( groovy, containsString( "foo,bar" ) );
        assertThat( groovy, containsString( "upstreamProjects" ) );
        assertThat( groovy, containsString( "threshold" ) );
        assertThat( groovy, containsString( "hudson.model.Result.FAILURE" ) );
    }
}
