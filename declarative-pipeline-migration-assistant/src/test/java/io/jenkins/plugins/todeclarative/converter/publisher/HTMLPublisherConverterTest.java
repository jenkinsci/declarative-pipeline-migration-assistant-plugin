package io.jenkins.plugins.todeclarative.converter.publisher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import htmlpublisher.HtmlPublisher;
import htmlpublisher.HtmlPublisherTarget;
import hudson.model.FreeStyleProject;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import java.util.Arrays;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class HTMLPublisherConverterTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest() throws Exception {
        String projectName = Long.toString(System.currentTimeMillis());
        FreeStyleProject p = j.createFreeStyleProject(projectName);

        HtmlPublisherTarget htmlPublisherTarget = new HtmlPublisherTarget(
                "reportName",
                "reportDir",
                "reportFiles", /*keepAll*/
                true,
                /*alwaysLinkToLastBuild*/ true, /*allowMissing*/
                true);
        HtmlPublisher publisher = new HtmlPublisher(Arrays.asList(htmlPublisherTarget));
        p.getPublishersList().add(publisher);

        HTMLPublisherConverter converter =
                j.jenkins.getExtensionList(HTMLPublisherConverter.class).get(0);
        assertTrue(converter.canConvert(publisher));
        ConverterResult result = new ConverterResult();
        converter.convert(new ConverterRequest().job(p), result, publisher);
        ModelASTPostBuild postBuild = result.getModelASTPipelineDef().getPostBuild();
        assertEquals(1, postBuild.getConditions().size());
        String groovy = postBuild.getConditions().get(0).toGroovy();
        assertThat(groovy, containsString("always"));
        assertThat(groovy, containsString("publishHTML("));
        assertThat(groovy, containsString("keepAll: true"));
        assertThat(groovy, containsString("reportName: 'reportName'"));
        assertThat(groovy, containsString("alwaysLinkToLastBuild: true"));
        assertThat(groovy, containsString("allowMissing: true"));
        assertThat(groovy, containsString("reportFiles: 'reportFiles'"));
        assertThat(groovy, containsString("reportDir: 'reportDir'"));
    }
}
