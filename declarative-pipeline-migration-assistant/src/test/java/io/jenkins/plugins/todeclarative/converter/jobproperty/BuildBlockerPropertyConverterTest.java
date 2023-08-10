package io.jenkins.plugins.todeclarative.converter.jobproperty;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import hudson.ExtensionList;
import hudson.plugins.buildblocker.BuildBlockerProperty;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class BuildBlockerPropertyConverterTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest() throws Exception {

        BuildBlockerProperty buildBlockerProperty = new BuildBlockerProperty(true, "NODE", "ALL", "foo");
        BuildBlockerPropertyConverter converter = ExtensionList.lookupSingleton(BuildBlockerPropertyConverter.class);

        assertTrue(converter.canConvert(buildBlockerProperty));
        ConverterResult result = new ConverterResult();
        converter.convert(new ConverterRequest(), result, buildBlockerProperty);
        ModelASTOptions options = result.getModelASTPipelineDef().getOptions();
        assertEquals(1, options.getOptions().size());
        ModelASTOption option = options.getOptions().get(0);
        assertEquals("buildBlocker", option.getName());
        String groovy = option.toGroovy();
        assertThat(groovy, containsString("useBuildBlocker: true"));
        assertThat(groovy, containsString("blockLevel: 'NODE'"));
        assertThat(groovy, containsString("scanQueueFor: 'ALL'"));
        assertThat(groovy, containsString("blockingJobs: 'foo'"));
    }
}
