package io.jenkins.plugins.todeclarative.converter.jobproperty;

import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.tasks.LogRotator;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import jenkins.model.BuildDiscarderProperty;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildParameter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildParameters;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ParameterPropertyConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        List<ParameterDefinition> parametersDefinitions = new ArrayList<>();
        parametersDefinitions.add( new StringParameterDefinition( "str", "defaultValue", "description str", true ) );
        parametersDefinitions.add( new ChoiceParameterDefinition( "choices", new String[]{"choice1","choice2"}, "description choice" ) );
        parametersDefinitions.add( new BooleanParameterDefinition( "nameboolean", true, "boolean description") );
        ParametersDefinitionProperty
            parametersDefinitionProperty = new ParametersDefinitionProperty( parametersDefinitions);
        p.addProperty( parametersDefinitionProperty );
        ParameterPropertyConverter converter =
            j.jenkins.getExtensionList( ParameterPropertyConverter.class ).get( 0 );
        assertTrue( converter.canConvert( null, parametersDefinitionProperty ) );

        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, null, parametersDefinitionProperty );
        ModelASTBuildParameters parameters = result.getModelASTPipelineDef().getParameters();
        assertEquals( 3, parameters.getParameters().size() );
        String groovy = parameters.toGroovy();
        assertThat( groovy, CoreMatchers.containsString( "string" ) );
        assertThat( groovy, CoreMatchers.containsString( "name: 'str'" ) );
        assertThat( groovy, CoreMatchers.containsString( "defaultValue: 'defaultValue'" ) );
        assertThat( groovy, CoreMatchers.containsString( "description: 'description str'" ) );

        assertThat( groovy, CoreMatchers.containsString( "choice" ) );
        assertThat( groovy, CoreMatchers.containsString( "name: 'choices'" ) );
        assertThat( groovy, CoreMatchers.containsString( "choices: [choice1, choice2]" ) );
        assertThat( groovy, CoreMatchers.containsString( "description: 'description choice'" ) );

        assertThat( groovy, CoreMatchers.containsString( "booleanParam" ) );
        assertThat( groovy, CoreMatchers.containsString( "name: 'nameboolean'" ) );
        assertThat( groovy, CoreMatchers.containsString( "defaultValue: true" ) );
        assertThat( groovy, CoreMatchers.containsString( "description: 'boolean description'" ) );
    }
}
