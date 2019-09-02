package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.hamcrest.CoreMatchers;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildParameters;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;

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

        assertTrue( converter.canConvert( null, requiredResourcesProperty ) );
        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, null, requiredResourcesProperty );
        ModelASTOptions options = result.getModelASTPipelineDef().getOptions();
        assertEquals( 1, options.getOptions().size() );
        String groovy = options.toGroovy();
        assertThat( groovy, CoreMatchers.containsString( "lock" ) );
        assertThat( groovy, CoreMatchers.containsString( "resource: 'beer'" ) );
        assertThat( groovy, CoreMatchers.containsString( "label: 'labelName'" ) );
    }
}
