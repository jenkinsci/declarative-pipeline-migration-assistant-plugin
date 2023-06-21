package io.jenkins.plugins.todeclarative.converter.publisher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.tasks.Mailer;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class MailPublisherConverterTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest() throws Exception {
        String projectName = Long.toString(System.currentTimeMillis());
        FreeStyleProject p = j.createFreeStyleProject(projectName);

        Mailer mailer = new Mailer(
                "foo@beer.com", //
                /* notifyEveryUnstableBuild */ true, //
                /* sendToIndividuals*/ true);
        p.getPublishersList().add(mailer);

        MailerPublisherConverter converter =
                j.jenkins.getExtensionList(MailerPublisherConverter.class).get(0);
        assertTrue(converter.canConvert(mailer));
        ConverterResult result = new ConverterResult();
        converter.convert(new ConverterRequest().job(p), result, mailer);
        ModelASTPostBuild postBuild = result.getModelASTPipelineDef().getPostBuild();
        assertEquals(1, postBuild.getConditions().size());
        String groovy = postBuild.getConditions().get(0).toGroovy();
        assertThat(groovy, containsString("always"));
        // mail(to: 'foo@beer.com', body: 'The body')
        assertThat(groovy, containsString("mail("));
        assertThat(groovy, containsString("to: 'foo@beer.com'"));
        assertThat(groovy, containsString("body: 'The body'"));
    }
}
