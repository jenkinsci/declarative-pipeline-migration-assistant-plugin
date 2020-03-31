package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.model.FreeStyleProject;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class RequiredResourcesPropertyConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        RequiredResourcesProperty requiredResourcesProperty =
            new RequiredResourcesProperty( "beer", null, null, "labelName", null );
        p.addProperty( requiredResourcesProperty );
        p.addProperty( requiredResourcesProperty );

        RequiredResourcesPropertyConverter converter =
            j.jenkins.getExtensionList( RequiredResourcesPropertyConverter.class ).get( 0 );

        assertTrue( converter.canConvert( requiredResourcesProperty ) );
        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, requiredResourcesProperty );
        ModelASTOptions options = result.getModelASTPipelineDef().getOptions();
        assertEquals( 1, options.getOptions().size() );
        String groovy = options.toGroovy();
        assertThat( groovy, containsString( "lock" ) );
        assertThat( groovy, containsString( "resource: 'beer'" ) );
        assertThat( groovy, containsString( "label: 'labelName'" ) );
    }

    @Test
    public void simpletest_empty_values()
        throws Exception
    {
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        RequiredResourcesProperty requiredResourcesProperty =
            new RequiredResourcesProperty( "", null, null, "", null );
        p.addProperty( requiredResourcesProperty );
        p.addProperty( requiredResourcesProperty );

        RequiredResourcesPropertyConverter converter =
            j.jenkins.getExtensionList( RequiredResourcesPropertyConverter.class ).get( 0 );

        assertTrue( converter.canConvert( requiredResourcesProperty ) );
        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, requiredResourcesProperty );
        ModelASTOptions options = result.getModelASTPipelineDef().getOptions();
        assertEquals( 1, options.getOptions().size() );
        String groovy = options.toGroovy();
        assertThat( groovy, containsString( "lock" ) );
    }
}
