package io.jenkins.plugins.todeclarative.converter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.coravy.hudson.plugins.github.GithubProjectProperty;
import htmlpublisher.HtmlPublisher;
import htmlpublisher.HtmlPublisherTarget;
import hudson.FilePath;
import hudson.Functions;
import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.FileParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.JDK;
import hudson.model.Job;
import hudson.model.Label;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.Slave;
import hudson.model.StringParameterDefinition;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;
import hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy;
import hudson.plugins.build_timeout.impl.NoActivityTimeOutStrategy;
import hudson.plugins.build_timeout.operations.FailOperation;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.BatchFile;
import hudson.tasks.BuildTrigger;
import hudson.tasks.LogRotator;
import hudson.tasks.Mailer;
import hudson.tasks.Maven;
import hudson.tasks.Shell;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.test.AggregatedTestResultPublisher;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.freestyle.FreestyleToDeclarativeConverter;
import jenkins.model.BuildDiscarder;
import jenkins.model.BuildDiscarderProperty;
import jenkins.model.Jenkins;
import jenkins.triggers.ReverseBuildTrigger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkinsci.lib.configprovider.ConfigProvider;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.buildwrapper.ConfigFileBuildWrapper;
import org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile;
import org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig;
import org.jenkinsci.plugins.configfiles.maven.job.MvnGlobalSettingsProvider;
import org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordMultiBinding;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class FreestyleTest
{

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void freestyle_conversion()
        throws Exception
    {
        String nodeName = "FOO_AGENT";
        Slave slave = j.createOnlineSlave();
        slave.setLabelString( nodeName );

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );
        p.setAssignedLabel( Label.get( nodeName ) );
        p.addProperty( new GithubProjectProperty( "https://github.com/jenkinsci/foo" ) );
        File f = new File( "./target");
        p.setCustomWorkspace( f.getAbsolutePath() );

        { // git
            List<UserRemoteConfig> repoList = new ArrayList<>();
            repoList.add( new UserRemoteConfig( "https://github.com/jenkinsci/foo.git", null, "master", null ) );
            repoList.add( new UserRemoteConfig( "https://github.com/jenkinsci/bar.git", null, "patch-1", "credsId" ) );
            BranchSpec branchSpec = new BranchSpec( "master" );
            GitSCM gitSCM =
                new GitSCM( repoList, Arrays.asList( branchSpec ), false, Collections.emptyList(), null, null, Collections.emptyList() );
            p.setScm( gitSCM );
        }

        {
            //int daysToKeep, int numToKeep, int artifactDaysToKeep, int artifactNumToKeep
            LogRotator logRotator = new LogRotator( 1, 2, 3, 4 );
            BuildDiscarderProperty buildDiscarderProperty = new BuildDiscarderProperty( logRotator );
            p.addProperty( buildDiscarderProperty );
        }

        {
            RequiredResourcesProperty requiredResourcesProperty =
                new RequiredResourcesProperty( "beer", null, null, "labelName", null );
            p.addProperty( requiredResourcesProperty );
        }

        {
            BuildTimeoutWrapper buildTimeoutWrapper = new BuildTimeoutWrapper( new AbsoluteTimeOutStrategy( "180" ),
                                                                               Collections.singletonList(
                                                                                   new FailOperation() ), "FOO" );
            p.getBuildWrappersList().add( buildTimeoutWrapper );
        }

        {
            List<ParameterDefinition> parametersDefinitions = new ArrayList<>();
            parametersDefinitions.add(
                new StringParameterDefinition( "str", "defaultValue", "description str", true ) );
            parametersDefinitions.add( new ChoiceParameterDefinition( "choice", new String[]{"choice1","choice2"}, "description choice" ) );
            parametersDefinitions.add( new BooleanParameterDefinition( "nameboolean", true, "boolean description" ) );
            parametersDefinitions.add( new FileParameterDefinition( "foo.txt", "foo.txt file" ) );
            ParametersDefinitionProperty parametersDefinitionProperty =
                new ParametersDefinitionProperty( parametersDefinitions );
            p.addProperty( parametersDefinitionProperty );
        }
        {
            ReverseBuildTrigger reverseBuildTrigger = new ReverseBuildTrigger( "pipeline" );
            reverseBuildTrigger.setThreshold( Result.UNSTABLE );
            p.addTrigger( reverseBuildTrigger );

            p.addTrigger( new TimerTrigger( "45 9-16/2 * * 1-5" ) );
            SCMTrigger scmTrigger = new SCMTrigger( "45 9-16/2 * * 1-5" );
            scmTrigger.setIgnorePostCommitHooks( true );
            p.addTrigger( scmTrigger );
        }

        p.getBuildersList().add( new Shell( "pwd" ) );
        p.getBuildersList().add( new Shell( "ls -lrt" ) );

        // manage unstableReturn value
        p.getBuildersList().add( new BatchFile( "cmd" ) );

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

        // ConfigFileBuildWrapper
        {
            ManagedFile managedFile1 = new ManagedFile( "id1", "myfile1.txt", "MYFILE1" );
            ManagedFile managedFile2 = new ManagedFile( "id2", "myfile2.txt", "MYFILE2" );
            p.getBuildWrappersList().add( new ConfigFileBuildWrapper( Arrays.asList( managedFile1, managedFile2 ) ) );
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
            ArtifactArchiver artifactArchiver = new ArtifactArchiver( "**/target/**.jar" );
            artifactArchiver.setAllowEmptyArchive( true );
            artifactArchiver.setExcludes( "**pom**" );
            artifactArchiver.setCaseSensitive( true );
            artifactArchiver.setFingerprint( true );
            artifactArchiver.setOnlyIfSuccessful( false );
            p.getPublishersList().add( artifactArchiver );
        }

        {
            JUnitResultArchiver jUnitResultArchiver = new JUnitResultArchiver( "target/**.txt" );
            jUnitResultArchiver.setHealthScaleFactor( 2 );
            jUnitResultArchiver.setKeepLongStdio( true );
            p.getPublishersList().add( jUnitResultArchiver );
        }

        p.getPublishersList().add( new AggregatedTestResultPublisher( "foo", true ) );

        {
            p.getPublishersList().add( new BuildTrigger( "foo,bar", Result.SUCCESS ) );
            p.getPublishersList().add( new BuildTrigger( "beer", Result.SUCCESS ) );

            j.createFreeStyleProject( "foo" );
            j.createFreeStyleProject( "bar" );
            j.createFreeStyleProject( "beer" );
        }

        {
            HtmlPublisherTarget htmlPublisherTarget =
                new HtmlPublisherTarget( "reportName", "reportDir", "reportFiles", /*keepAll*/true,
                    /*alwaysLinkToLastBuild*/true, /*allowMissing*/true );
            p.getPublishersList().add( new HtmlPublisher( Arrays.asList( htmlPublisherTarget ) ) );
        }

        {
            p.getPublishersList().add( new Mailer( "foo@beer.com", //
                /* notifyEveryUnstableBuild */ true, //
                /* sendToIndividuals*/ true ) );
        }

        FreestyleToDeclarativeConverter converter =
            Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );

        Assert.assertTrue( converter.canConvert( p ) );

        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult converterResult = new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );

        converter.convert( request, converterResult, p );
        String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );

        assertThat( groovy, containsString( "branch: 'master'" ) );
        assertThat( groovy, containsString( "url: 'https://github.com/jenkinsci/foo.git'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credsId'" ) );
//        agent {
//            node {
//                label ""
//                customWorkspace "/blabla/blabla"
//            }
//        }
        assertThat( groovy, containsString( "label 'FOO_AGENT'" ) );
        assertThat( groovy, containsString( "customWorkspace \"" + f.getAbsolutePath() + "\"" ));

        assertThat( groovy, containsString( "lock" ) );
        assertThat( groovy, containsString( "resource: 'beer'" ) );
        assertThat( groovy, containsString( "label: 'labelName'" ) );

    }

    static class NoOpBuildDiscarder extends BuildDiscarder implements Serializable
    {

        public NoOpBuildDiscarder()
        {
            super();
        }

        @Override
        public void perform( Job<?, ?> job )
            throws IOException, InterruptedException
        {

        }
    }

    @Test
    public void freestyle_conversion_then_run()
        throws Exception
    {

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        p.addProperty( new GithubProjectProperty( "http://github.com/beer/paleale" ) );

        File f = new File( "target/ffoo/bar");
        if (Files.exists(f.toPath()))
        {
            FileUtils.deleteDirectory( f );
        }
        Files.createDirectories( f.toPath() );
        String workspace = Functions.isWindows() ? StringUtils.replace(f.getAbsolutePath(), "\\", "\\\\" ):f.getAbsolutePath();
        p.setCustomWorkspace( workspace );

        p.addProperty( new BuildDiscarderProperty(new NoOpBuildDiscarder() ));

        List<ParameterDefinition> parametersDefinitions = new ArrayList<>();
        parametersDefinitions.add( new StringParameterDefinition( "str", "defaultValue", "description str", true ) );
        parametersDefinitions.add( new BooleanParameterDefinition( "nameboolean", true, "boolean description" ) );
        ParametersDefinitionProperty parametersDefinitionProperty =
            new ParametersDefinitionProperty( parametersDefinitions );
        p.addProperty( parametersDefinitionProperty );

        { // triggers
            p.addTrigger( new TimerTrigger( "@daily" ) );
            p.addTrigger( new SCMTrigger( "H */4 * * 1-5" ) );
            ReverseBuildTrigger trigger = new ReverseBuildTrigger( "foo" );
            trigger.setThreshold( Result.FAILURE );
            p.addTrigger( trigger );
        }

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

        {
            BuildTimeoutWrapper buildTimeoutWrapper = new BuildTimeoutWrapper( new NoActivityTimeOutStrategy( "180" ),
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

        FreeStyleProject foo = j.createFreeStyleProject( "foo" );
        p.getPublishersList().add( new AggregatedTestResultPublisher( "foo", true ) );

        FreestyleToDeclarativeConverter converter =
            Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );

        Assert.assertTrue( converter.canConvert( p ) );

        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult converterResult = new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );

        converter.convert( request, converterResult, p );
        String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );

        assertThat( groovy, containsString( "agent" ) );
        assertThat( groovy, containsString( "customWorkspace \"" + workspace + "\""));

        { // triggers
            assertThat( groovy, containsString( "cron" ) );
            assertThat( groovy, containsString( "@daily" ) );
            assertThat( groovy, containsString( "pollSCM" ) );
            assertThat( groovy, containsString( "H */4 * * 1-5" ) );
            assertThat( groovy, containsString( "foo" ) );
            assertThat( groovy, containsString( "upstreamProjects" ) );
            assertThat( groovy, containsString( "threshold" ) );
            assertThat( groovy, containsString( "hudson.model.Result.FAILURE" ) );
        }

        WorkflowJob job = j.jenkins.createProject( WorkflowJob.class, "singleStep" );
        job.setDefinition( new CpsFlowDefinition( groovy, true ) );

//        FilePath ws = j.jenkins.getWorkspaceFor( job );
//        FilePath testFile = ws.child( "test-result.xml" );
//        testFile.copyFrom( Thread.currentThread().getContextClassLoader().getResourceAsStream( "junit-report.xml" ) );

        // copy the test file
        Path destination = new File(workspace,"test-result.xml").toPath();
        Files.copy( Paths.get("src", "test", "resources", "junit-report.xml"), destination );
        destination.toFile().setLastModified( System.currentTimeMillis() -1 );

        WorkflowRun run = job.scheduleBuild2( 0 ).get();

        j.waitForCompletion( run );
        j.assertBuildStatus( Result.SUCCESS, run );
        int size = job.getBuilds().size();
    }

    @Test
    public void freestyle_conversion_only_Jenkinsfile()
        throws Exception
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
        // List<String> toGroovy needs to be fixed
        //parametersDefinitions.add( new ChoiceParameterDefinition( "choice", new String[]{"choice1","choice2"}, "description choice" ) );
        parametersDefinitions.add( new BooleanParameterDefinition( "nameboolean", true, "boolean description" ) );
        ParametersDefinitionProperty parametersDefinitionProperty =
            new ParametersDefinitionProperty( parametersDefinitions );
        p.addProperty( parametersDefinitionProperty );

        p.getBuildersList().add( new Shell( "pwd" ) );

        FreestyleToDeclarativeConverter converter =
            Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );

        Assert.assertTrue( converter.canConvert( p ) );

        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult converterResult = new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );

        converter.convert( request, converterResult, p );
        String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );


    }

    @Test
    public void freestyle_conversion_maven_build_then_run_project()
        throws Exception
    {

        Slave slave = j.createOnlineSlave();
        slave.setLabelString( "FOO_AGENT" );

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );
        p.addProperty( new GithubProjectProperty( "https://github.com/jenkinsci/foo" ) );

        //int daysToKeep, int numToKeep, int artifactDaysToKeep, int artifactNumToKeep
        LogRotator logRotator = new LogRotator( 1, 2, 3, 4 );
        BuildDiscarderProperty buildDiscarderProperty = new BuildDiscarderProperty( logRotator );
        p.addProperty( buildDiscarderProperty );

        List<ParameterDefinition> parametersDefinitions = new ArrayList<>();
        parametersDefinitions.add( new StringParameterDefinition( "str", "defaultValue", "description str", true ) );
        // List<String> toGroovy needs to be fixed
        //parametersDefinitions.add( new ChoiceParameterDefinition( "choice", new String[]{"choice1","choice2"}, "description choice" ) );
        parametersDefinitions.add( new BooleanParameterDefinition( "nameboolean", true, "boolean description" ) );
        ParametersDefinitionProperty parametersDefinitionProperty =
            new ParametersDefinitionProperty( parametersDefinitions );
        p.addProperty( parametersDefinitionProperty );

        // should work everywhere.... at least I hope
        p.setJDK( new JDK( "thejdk", System.getenv( "JAVA_HOME" ) ) );

        p.setScm( new ExtractResourceSCM(
            Thread.currentThread().getContextClassLoader().getResource( "maven3-project.zip" ) ) );

        {
            GlobalConfigFiles store =
                j.getInstance().getExtensionList( GlobalConfigFiles.class ).get( GlobalConfigFiles.class );

            String content = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResource( "global-maven-settings.xml" ) );
            GlobalMavenSettingsConfig globalMavenSettingsConfig =
                new GlobalMavenSettingsConfig( "global-maven-settings-id", "global-maven-settings-name", "comment",
                                               content );
            store.save( globalMavenSettingsConfig );

            Maven.MavenInstallation mavenInstallation = ToolInstallations.configureMaven35();

            Maven maven = new Maven( "clean verify", mavenInstallation.getName() /*maven name*/, "pom.xml",
                                     "-DskipTests" /*properties*/, "-Djava.awt.headless=true" /*jvmOptions*/,
                                     false /*usePrivateRepository*/, null /*SettingsProvider settings*/,
                                     new MvnGlobalSettingsProvider(
                                         "global-maven-settings-id" ) /*GlobalSettingsProvider*/ );

            p.getBuildersList().add( maven );
        }

        p.getPublishersList().add( new BuildTrigger( "beer", Result.SUCCESS ) );

        FreestyleToDeclarativeConverter converter =
            Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );

        Assert.assertTrue( converter.canConvert( p ) );

        ConverterRequest request =
            new ConverterRequest().job( p );
        ConverterResult converterResult = new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );

        converter.convert( request, converterResult, p );
        String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );
        Assert.assertThat( groovy, CoreMatchers.containsString( "mvn" ) );
        WorkflowJob job = j.jenkins.createProject( WorkflowJob.class, "singleStep" );
        job.setDefinition( new CpsFlowDefinition( groovy, true ) );
        WorkflowRun run = job.scheduleBuild2( 0 ).get();
        j.waitForCompletion( run );
        j.assertBuildStatus( Result.SUCCESS, run );

    }

    @Test
    public void get_warning()
        throws Exception
    {
        Slave slave = j.createOnlineSlave();
        slave.setLabelString( "FOO_AGENT" );

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );
        p.getBuildersList().add( new FakeBuilder() );

        p.addProperty( new hudson.plugins.jira.JiraProjectProperty("foosite") );

        p.getBuildWrappersList().add( new FakeBuildWrapper() );
        p.getPublishersList().add( new FakeRecorder() );

        { // triggers
            p.addTrigger( new TimerTrigger( "@daily" ) );
            p.addTrigger( new SCMTrigger( "H */4 * * 1-5" ) );
            ReverseBuildTrigger trigger = new ReverseBuildTrigger( "foo" );
            trigger.setThreshold( Result.FAILURE );
            p.addTrigger( trigger );
        }

        { // general settings
            p.setConcurrentBuild( false );
            p.setDisabled( true );
        }
        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult converterResult = new ConverterResult().modelASTPipelineDef( new ModelASTPipelineDef( null ) );

        FreestyleToDeclarativeConverter converter =
            Jenkins.get().getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );
        converter.convert( request, converterResult, p );
        assertEquals( converterResult.getWarnings().toString(), 5, converterResult.getWarnings().size() );

        assertEquals( 1, converterResult.getWarnings().stream() //
            .filter( warning -> StringUtils.equals( warning.getTypeName(), FakeBuilder.class.getName() ) ) //
            .count());

        assertEquals( 1, converterResult.getWarnings().stream() //
            .filter( warning -> StringUtils.equals( warning.getTypeName(), FakeBuildWrapper.class.getName() ) ) //
            .count());

        assertEquals( 1, converterResult.getWarnings().stream() //
            .filter( warning -> StringUtils.equals( warning.getTypeName(), FakeRecorder.class.getName() ) ) //
            .count());

        String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );

        assertThat( groovy, containsString( "No converter for Builder: io.jenkins.plugins.todeclarative.converter.FakeBuilder" ) );
        assertThat( groovy, containsString( "No converter for Publisher: io.jenkins.plugins.todeclarative.converter.FakeRecorder" ));

        WorkflowJob job = j.jenkins.createProject( WorkflowJob.class, "get_warning" );
        job.setDefinition( new CpsFlowDefinition( groovy, true ) );
        WorkflowRun run = job.scheduleBuild2( 0 ).get();
        j.waitForCompletion( run );
        j.assertBuildStatus( Result.SUCCESS, run );
    }

}
