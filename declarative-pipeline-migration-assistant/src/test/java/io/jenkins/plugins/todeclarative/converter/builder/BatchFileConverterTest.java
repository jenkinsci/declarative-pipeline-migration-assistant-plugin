package io.jenkins.plugins.todeclarative.converter.builder;

import static org.junit.Assert.*;

import hudson.tasks.BatchFile;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class BatchFileConverterTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest() throws Exception {
        BatchFileConverter converter =
                j.jenkins.getExtensionList(BatchFileConverter.class).get(0);
        BatchFile batchFile = new BatchFile("cmd");
        ConverterResult result = new ConverterResult();
        assertTrue(converter.canConvert(batchFile));
        assertTrue(converter.convert(new ConverterRequest(), result, batchFile));
        List<ModelASTStage> stages = result.getModelASTPipelineDef().getStages().getStages();
        assertNotNull(stages);
        String stageGroovy = stages.get(0).toGroovy();
        assertThat(stageGroovy, CoreMatchers.containsString("bat 'cmd'"));
    }
}
