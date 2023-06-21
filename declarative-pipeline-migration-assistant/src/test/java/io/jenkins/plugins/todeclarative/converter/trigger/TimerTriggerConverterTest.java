package io.jenkins.plugins.todeclarative.converter.trigger;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.triggers.TimerTrigger;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import java.util.List;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TimerTriggerConverterTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest() throws Exception {
        String projectName = Long.toString(System.currentTimeMillis());
        FreeStyleProject p = j.createFreeStyleProject(projectName);

        TimerTrigger trigger = new TimerTrigger("@daily");
        p.addTrigger(trigger);

        TimerTriggerConverter converter =
                j.jenkins.getExtensionList(TimerTriggerConverter.class).get(0);

        assertTrue(converter.canConvert(trigger));

        ConverterResult result = new ConverterResult();
        converter.convert(new ConverterRequest().job(p), result, trigger);

        List<ModelASTTrigger> triggers =
                result.getModelASTPipelineDef().getTriggers().getTriggers();
        assertEquals(1, triggers.size());

        String groovy = result.getModelASTPipelineDef().getTriggers().toGroovy();
        System.out.println(groovy);
        assertThat(groovy, containsString("cron"));
        assertThat(groovy, containsString("@daily"));
    }
}
