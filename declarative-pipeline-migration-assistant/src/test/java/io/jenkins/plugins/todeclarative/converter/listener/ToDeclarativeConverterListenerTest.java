package io.jenkins.plugins.todeclarative.converter.listener;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.BooleanParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Slave;
import hudson.tasks.Shell;
import io.jenkins.plugins.todeclarative.actions.ToDeclarativeAction;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.freestyle.FreestyleToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.api.ToDeclarativeConverterListener;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ToDeclarativeConverterListenerTest {
    @Rule public JenkinsRule r = new JenkinsRule();

    @Test public void onConversion() throws Exception {
        String nodeName = "FOO_AGENT";
        Slave slave = r.createOnlineSlave();
        slave.setLabelString( nodeName );
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = r.createFreeStyleProject( projectName );
        p.setAssignedLabel( Label.get( nodeName ) );
        List<ParameterDefinition> parametersDefinitions = new ArrayList<>();
        parametersDefinitions.add( new BooleanParameterDefinition( "nameboolean", true, "boolean description" ) );
        ParametersDefinitionProperty parametersDefinitionProperty = new ParametersDefinitionProperty( parametersDefinitions );
        p.addProperty( parametersDefinitionProperty );
        p.getBuildersList().add( new Shell( "pwd" ) );

        FreestyleToDeclarativeConverter converter = Jenkins.get().getExtensionList(FreestyleToDeclarativeConverter.class).get(0);
        assertTrue( converter.canConvert( p ) );
        ToDeclarativeAction action = new ToDeclarativeAction(p);
        String jenkinsfile = action.doConvert();
        TestListenerTo instance = ExtensionList.lookupSingleton(TestListenerTo.class);
        assertTrue(instance.fired);
        assertThat(instance.job, equalTo(p));
        String resultGroovy = instance.result.getModelASTPipelineDef().toPrettyGroovy();
        assertThat(resultGroovy, equalTo(jenkinsfile));
    }

    @Extension
    public static class TestListenerTo extends ToDeclarativeConverterListener {
        boolean fired = false;
        FreeStyleProject job = null;
        ConverterResult result = null;

        @Override
        public void onConversion(FreeStyleProject job, ConverterResult conversionResult) {
            fired = true;
            this.job = job;
            result = conversionResult;
        }
    }
}
