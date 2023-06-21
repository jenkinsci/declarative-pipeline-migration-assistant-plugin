package io.jenkins.plugins.todeclarative.converter.publisher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BuildTrigger;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class BuildTriggerPublisherConverterTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest() throws Exception {
        String projectName = Long.toString(System.currentTimeMillis());
        FreeStyleProject p = j.createFreeStyleProject(projectName);

        j.createFreeStyleProject("foo");
        j.createFreeStyleProject("bar");
        j.createFreeStyleProject("beer");

        BuildTrigger buildTrigger = new BuildTrigger("foo,bar", Result.SUCCESS);
        p.getPublishersList().add(buildTrigger);

        BuildTriggerPublisherConverter converter =
                j.jenkins.getExtensionList(BuildTriggerPublisherConverter.class).get(0);
        assertTrue(converter.canConvert(buildTrigger));
        ConverterResult result = new ConverterResult();
        converter.convert(new ConverterRequest().job(p), result, buildTrigger);
        ModelASTPostBuild postBuild = result.getModelASTPipelineDef().getPostBuild();
        assertEquals(1, postBuild.getConditions().size());
        String groovy = postBuild.getConditions().get(0).toGroovy();
        assertThat(groovy, containsString("build(job: 'foo')"));
        assertThat(groovy, containsString("build(job: 'bar')"));
        assertThat(groovy, not(containsString("build(job: 'beer')")));
    }
}
