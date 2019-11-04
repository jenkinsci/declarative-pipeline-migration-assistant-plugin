package io.jenkins.plugins.todeclarative.converter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.coravy.hudson.plugins.github.GithubProjectProperty;
import htmlpublisher.HtmlPublisher;
import htmlpublisher.HtmlPublisherTarget;
import hudson.Functions;
import hudson.model.BooleanParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Slave;
import hudson.model.StringParameterDefinition;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;
import hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy;
import hudson.plugins.build_timeout.operations.FailOperation;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.BatchFile;
import hudson.tasks.LogRotator;
import hudson.tasks.Shell;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.test.AggregatedTestResultPublisher;
import io.jenkins.plugins.todeclarative.actions.ToDeclarativeAction;
import jenkins.model.BuildDiscarderProperty;
import org.jenkinsci.lib.configprovider.ConfigProvider;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.buildwrapper.ConfigFileBuildWrapper;
import org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile;
import org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig;
import org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordMultiBinding;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ToDeclarativeActionTest
{

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpleActionRun() throws Exception
    {
        Slave slave = j.createOnlineSlave();
        slave.setLabelString( "FOO_AGENT" );

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );
        p.addProperty( new GithubProjectProperty( "http://github.com/beer/paleale" ) );

        //int daysToKeep, int numToKeep, int artifactDaysToKeep, int artifactNumToKeep
        LogRotator logRotator = new LogRotator( 1, 2, 3, 4 );
        BuildDiscarderProperty buildDiscarderProperty = new BuildDiscarderProperty( logRotator );
        p.addProperty( buildDiscarderProperty );

        List<ParameterDefinition> parametersDefinitions = new ArrayList<>();
        parametersDefinitions.add( new StringParameterDefinition( "str", "defaultValue", "description str", true ) );
        //parametersDefinitions.add( new ChoiceParameterDefinition( "choice", new String[]{"choice1","choice2"}, "description choice" ) );
        parametersDefinitions.add( new BooleanParameterDefinition( "nameboolean", true, "boolean description" ) );
        ParametersDefinitionProperty parametersDefinitionProperty =
            new ParametersDefinitionProperty( parametersDefinitions );
        p.addProperty( parametersDefinitionProperty );

        if ( Functions.isWindows() )
        {
            p.getBuildersList().add( new BatchFile( "cmd" ) );
        }
        else
        {
            p.getBuildersList().add( new Shell( "pwd" ) );
            p.getBuildersList().add( new Shell( "ls -lrt" ) );
            p.getBuildersList().add( new Shell( "echo $str" ) );
            p.getBuildersList().add( new Shell( "cat myfile1.txt" ) );
        }

        {
            String username = "bob";
            String password = "s3cr3t";
            UsernamePasswordCredentialsImpl c =
                new UsernamePasswordCredentialsImpl( CredentialsScope.GLOBAL, null, "sample", username, password );
            CredentialsProvider.lookupStores( j ).iterator().next().addCredentials( Domain.global(), c );

            UsernamePasswordMultiBinding usernamePasswordMultiBinding =
                new UsernamePasswordMultiBinding( "theuser", "thepassword", c.getId() );
            SecretBuildWrapper secretBuildWrapper =
                new SecretBuildWrapper( Arrays.asList( usernamePasswordMultiBinding ) );

            p.getBuildWrappersList().add( secretBuildWrapper );
        }

        {
            BuildTimeoutWrapper buildTimeoutWrapper = new BuildTimeoutWrapper( new AbsoluteTimeOutStrategy( "180" ),
                                                                               Collections.singletonList(
                                                                                   new FailOperation() ), "FOO" );
            p.getBuildWrappersList().add( buildTimeoutWrapper );
        }

        // ConfigFileBuildWrapper
        {
            GlobalConfigFiles globalConfigFiles = GlobalConfigFiles.get();
            ConfigProvider configProvider = ConfigProvider.getByIdOrNull( GlobalMavenSettingsConfig.class.getName() );
            globalConfigFiles.save( configProvider.newConfig( "id1", "the id1", "id1 comment", "foo=bar_id1" ) );
            globalConfigFiles.save( configProvider.newConfig( "id2", "the id2", "id2 comment", "foo=bar_id2" ) );
            ManagedFile managedFile1 = new ManagedFile( "id1", "myfile1.txt", "MYFILE1" );
            ManagedFile managedFile2 = new ManagedFile( "id2", "myfile2.txt", "MYFILE2" );
            p.getBuildWrappersList().add( new ConfigFileBuildWrapper( Arrays.asList( managedFile1, managedFile2 ) ) );
        }

        {
            HtmlPublisherTarget htmlPublisherTarget =
                new HtmlPublisherTarget( "reportName", "reportDir", "reportFiles", /*keepAll*/true,
                    /*alwaysLinkToLastBuild*/true, /*allowMissing*/true );
            p.getPublishersList().add( new HtmlPublisher( Arrays.asList( htmlPublisherTarget ) ) );
        }

        {
            ArtifactArchiver artifactArchiver = new ArtifactArchiver( "**/target/**.jar" );
            artifactArchiver.setAllowEmptyArchive( true );
            artifactArchiver.setExcludes( "**pom**" );
            artifactArchiver.setCaseSensitive( true );
            artifactArchiver.setFingerprint( true );
            artifactArchiver.setOnlyIfSuccessful( true );
            p.getPublishersList().add( artifactArchiver );
        }

        {
            JUnitResultArchiver jUnitResultArchiver = new JUnitResultArchiver( "**/**.xml" );
            jUnitResultArchiver.setHealthScaleFactor( 2 );
            jUnitResultArchiver.setKeepLongStdio( true );
            p.getPublishersList().add( jUnitResultArchiver );
        }

        p.getPublishersList().add( new AggregatedTestResultPublisher( "foo", true ) );

        ToDeclarativeAction action = new ToDeclarativeAction(p);

        String jenkinsFile = action.doConvert();

        assertTrue(action.getWarnings().toString(), !action.getWarnings().isEmpty());
        assertTrue(action.getError()==null);

        assertEquals( jenkinsFile, action.getJenkinsFile() );

        assertNotNull( action.getDisplayName() );
        assertNotNull( action.getIconFileName() );
        assertNotNull( action.getUrlName() );


        ToDeclarativeAction.ToDeclarativeActionDescriptor actionDescriptor =
            j.jenkins.getExtensionList( ToDeclarativeAction.ToDeclarativeActionDescriptor.class ).get( 0 );

        ToDeclarativeAction.ActionInjector actionInjector =
            j.jenkins.getExtensionList( ToDeclarativeAction.ActionInjector.class).get( 0 );

        assertEquals( 1, actionInjector.createFor( p ).size() );

    }

}
