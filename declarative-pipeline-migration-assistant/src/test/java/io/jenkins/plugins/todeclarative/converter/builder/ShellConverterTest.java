package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.ExtensionList;
import hudson.tasks.Shell;
import io.jenkins.plugins.todeclarative.converter.api.BaseConverter;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ShellConverterTest
{

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {

        ExtensionList<BaseConverter> allConverters = SingleTypedConverter.all();
        ShellConverter converter = j.jenkins.getExtensionList( ShellConverter.class ).get( 0 );
        Shell shell = new Shell( "ls -lrt" );
        ConverterResult result = new ConverterResult();
        assertTrue(converter.convert( new ConverterRequest(), result, shell ));
        List<ModelASTStage> stages = result.getModelASTPipelineDef().getStages().getStages();
        assertNotNull( stages );
        String stageGroovy = stages.get(0).toGroovy();
        assertThat( stageGroovy, CoreMatchers.containsString( "sh 'ls -lrt'" ) );
    }
}
