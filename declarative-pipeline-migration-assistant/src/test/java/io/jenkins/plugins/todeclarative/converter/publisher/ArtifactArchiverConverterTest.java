package io.jenkins.plugins.todeclarative.converter.publisher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.tasks.ArtifactArchiver;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ArtifactArchiverConverterTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest() throws Exception {
        String projectName = Long.toString(System.currentTimeMillis());
        FreeStyleProject p = j.createFreeStyleProject(projectName);

        ArtifactArchiver artifactArchiver = new ArtifactArchiver("**/target/**.jar");
        artifactArchiver.setAllowEmptyArchive(true);
        artifactArchiver.setExcludes("**pom**");
        artifactArchiver.setCaseSensitive(true);
        artifactArchiver.setFingerprint(true);
        artifactArchiver.setOnlyIfSuccessful(true);
        p.getPublishersList().add(artifactArchiver);

        ArtifactArchiverConverter converter =
                j.jenkins.getExtensionList(ArtifactArchiverConverter.class).get(0);
        assertTrue(converter.canConvert(artifactArchiver));
        ConverterResult result = new ConverterResult();
        converter.convert(new ConverterRequest(), result, artifactArchiver);
        ModelASTPostBuild postBuild = result.getModelASTPipelineDef().getPostBuild();
        assertEquals(1, postBuild.getConditions().size());
        String groovy = postBuild.getConditions().get(0).toGroovy();
        assertThat(groovy, containsString("success"));
        assertThat(groovy, containsString("excludes: '**pom**'"));
        assertThat(groovy, containsString("onlyIfSuccessful: true"));
        assertThat(groovy, containsString("allowEmptyArchive: true"));
        assertThat(groovy, containsString("artifacts: '**/target/**.jar'"));
        assertThat(groovy, containsString("fingerprint: true"));
        assertThat(groovy, containsString("$class: 'ArtifactArchiver'"));
    }
}
