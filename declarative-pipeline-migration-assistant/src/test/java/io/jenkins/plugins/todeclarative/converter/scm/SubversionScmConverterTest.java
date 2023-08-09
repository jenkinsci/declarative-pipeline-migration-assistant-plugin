package io.jenkins.plugins.todeclarative.converter.scm;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import hudson.ExtensionList;
import hudson.scm.SubversionSCM;
import hudson.scm.subversion.UpdateUpdater;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubversionScmConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubversionScmConverterTest.class);

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpleconversiontest() throws Exception {

        SubversionSCM.ModuleLocation moduleLocation =
                new SubversionSCM.ModuleLocation("https://foo.com/svn", "credsid", "local-dir", "3", true, true);
        ;

        SubversionSCM subversionSCM = new SubversionSCM(
                Arrays.asList(moduleLocation),
                new UpdateUpdater(),
                null,
                "excludedRegionsValue",
                "excludedUsersValue",
                "excludedRevpropValue",
                "excludedCommitMessagesValue",
                "includedRegionsValue",
                true,
                true,
                Arrays.asList(new SubversionSCM.AdditionalCredentials("realmValue", "credentialsIdValue")),
                true);

        SubversionScmConverter subversionScmConverter = ExtensionList.lookupSingleton(SubversionScmConverter.class);
        ConverterRequest converterRequest = new ConverterRequest();
        ConverterResult converterResult = new ConverterResult();

        assertThat(subversionScmConverter.canConvert(subversionSCM), is(true));

        subversionScmConverter.convert(converterRequest, converterResult, subversionSCM);
        LOGGER.info("groovy: {}", converterResult.getModelASTPipelineDef().toGroovy());

        String groovy = converterResult.getModelASTPipelineDef().toGroovy();
        assertThat(groovy, containsString("checkout([$class: 'SubversionSCM'"));
        assertThat(groovy, containsString("excludedCommitMessages:'excludedCommitMessagesValue'"));
        assertThat(groovy, containsString("excludedRegions:'excludedRegionsValue'"));
        assertThat(groovy, containsString("excludedRevprop:'excludedRevpropValue'"));
        assertThat(groovy, containsString("excludedUsers:'excludedUsersValue'"));
        assertThat(groovy, containsString("filterChangelog:true"));
        assertThat(groovy, containsString("excludedCommitMessages:'excludedCommitMessagesValue'"));
        assertThat(groovy, containsString("remote: 'https://foo.com/svn'"));
    }
}
