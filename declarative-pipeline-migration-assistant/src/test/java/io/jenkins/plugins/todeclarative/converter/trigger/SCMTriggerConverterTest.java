package io.jenkins.plugins.todeclarative.converter.trigger;

import hudson.model.FreeStyleProject;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class SCMTriggerConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        SCMTrigger trigger = new SCMTrigger( "H */4 * * 1-5" );
        p.addTrigger( trigger );

        SCMTriggerConverter converter = j.jenkins
            .getExtensionList( SCMTriggerConverter.class ).get( 0 );

        assertTrue(converter.canConvert( null, trigger ) );

        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest().job( p ), result, null, trigger );

        List<ModelASTTrigger> triggers = result.getModelASTPipelineDef().getTriggers().getTriggers();
        assertEquals( 1, triggers.size() );

        String groovy = result.getModelASTPipelineDef().getTriggers().toGroovy();
        System.out.println( groovy );
        assertThat( groovy, containsString( "pollSCM" ) );
        assertThat( groovy, containsString( "H */4 * * 1-5" ) );
    }

}
