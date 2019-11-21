package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.tasks.Shell;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ShellConverterTest
{

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {

        ShellConverter converter = j.jenkins.getExtensionList( ShellConverter.class ).get( 0 );
        Shell shell = new Shell( "ls -lrt" );
        ConverterResult result = new ConverterResult();
        ModelASTStage stage = converter.convert( new ConverterRequest(), result, shell );
        assertNotNull( stage );
        String stageGroovy = stage.toGroovy();
        assertThat( stageGroovy, CoreMatchers.containsString( "sh 'ls -lrt'" ) );
    }
}
