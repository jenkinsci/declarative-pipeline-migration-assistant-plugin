package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.model.FreeStyleProject;
import hudson.model.JDK;
import hudson.tasks.Maven;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import jenkins.model.Jenkins;
import jenkins.mvn.FilePathGlobalSettingsProvider;
import jenkins.mvn.FilePathSettingsProvider;
import jenkins.mvn.GlobalSettingsProvider;
import jenkins.mvn.SettingsProvider;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;

import java.util.List;

import static org.junit.Assert.*;

public class MavenConverterTest
{

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpleMavenConversion()
        throws Exception
    {

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );
        JDK jdk = new JDK( "thejdk", System.getenv( "JAVA_HOME" ) );
        Jenkins.get().getJDKs().add( jdk );
        p.setJDK( jdk );

        GlobalConfigFiles store =
            j.getInstance().getExtensionList( GlobalConfigFiles.class ).get( GlobalConfigFiles.class );

        String content = IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResource( "global-maven-settings.xml" ) );
        GlobalMavenSettingsConfig globalMavenSettingsConfig =
            new GlobalMavenSettingsConfig( "global-maven-settings-id", "global-maven-settings-name", "comment",
                                           content );
        store.save( globalMavenSettingsConfig );

        Maven.MavenInstallation mavenInstallation = ToolInstallations.configureMaven35();

        GlobalSettingsProvider globalSettingsProvider = new FilePathGlobalSettingsProvider( "global.xml" );
        SettingsProvider settingsProvider = new FilePathSettingsProvider( "settings.xml" );

        Maven maven = new Maven( "clean verify", mavenInstallation.getName() /*maven name*/, "thepom.xml",
                                 "-DskipTests -Dfoo=bar" /*properties*/,
                                 "-Djava.awt.headless=true -Xmx768m -Xms768m -client -XX:+HeapDumpOnOutOfMemoryError" /*jvmOptions*/,
                                 true /*usePrivateRepository*/, settingsProvider /*SettingsProvider settings*/,
                                 globalSettingsProvider /*GlobalSettingsProvider*/ );

        MavenConverter mavenConverter = j.jenkins.getExtensionList( MavenConverter.class ).get( 0 );

        Assert.assertTrue( mavenConverter.canConvert( maven ) );
        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult result = new ConverterResult();
        assertTrue(mavenConverter.convert( request, result, maven ));
        List<ModelASTStage> stages = result.getModelASTPipelineDef().getStages().getStages();
        assertNotNull( stages );

        String toolsGroovy = result.getModelASTPipelineDef().getTools().toGroovy();
        System.out.println( "tools: " + toolsGroovy );

        String stageGroovy = stages.get(0).toGroovy();
        System.out.println( "groovy: " + stageGroovy );

        assertThat( toolsGroovy, CoreMatchers.containsString( "tools" ) );
        assertThat( toolsGroovy, CoreMatchers.containsString( "maven 'apache-maven-3.5.0'" ) );
        assertThat( toolsGroovy, CoreMatchers.containsString( "jdk 'thejdk'" ) );

        assertThat( stageGroovy, CoreMatchers.containsString( "mvn" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "clean verify" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-Dmaven.repo.local=.repository" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-f thepom.xml" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-DskipTests -Dfoo=bar" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-s settings.xml" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-gs global.xml" ) );
        assertThat( stageGroovy, CoreMatchers.containsString(
            "MAVEN_OPTS = '-Djava.awt.headless=true -Xmx768m -Xms768m -client -XX:+HeapDumpOnOutOfMemoryError'" ) );
    }

    @Test
    public void simpleMavenConversionNoJDKNoMaven()
        throws Exception
    {

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        GlobalConfigFiles store =
            j.getInstance().getExtensionList( GlobalConfigFiles.class ).get( GlobalConfigFiles.class );

        String content = IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResource( "global-maven-settings.xml" ) );
        GlobalMavenSettingsConfig globalMavenSettingsConfig =
            new GlobalMavenSettingsConfig( "global-maven-settings-id", "global-maven-settings-name", "comment",
                                           content );
        store.save( globalMavenSettingsConfig );


        GlobalSettingsProvider globalSettingsProvider = new FilePathGlobalSettingsProvider( "global.xml" );
        SettingsProvider settingsProvider = new FilePathSettingsProvider( "settings.xml" );

        Maven maven = new Maven( "clean verify", null /*maven name*/, "thepom.xml",
                                 "-DskipTests -Dfoo=bar" /*properties*/,
                                 "-Djava.awt.headless=true -Xmx768m -Xms768m -client -XX:+HeapDumpOnOutOfMemoryError" /*jvmOptions*/,
                                 true /*usePrivateRepository*/, settingsProvider /*SettingsProvider settings*/,
                                 globalSettingsProvider /*GlobalSettingsProvider*/ );

        MavenConverter mavenConverter = j.jenkins.getExtensionList( MavenConverter.class ).get( 0 );

        assertTrue( mavenConverter.canConvert( maven ) );
        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult result = new ConverterResult();
        assertTrue(mavenConverter.convert( request, result, maven ));
        List<ModelASTStage> stages = result.getModelASTPipelineDef().getStages().getStages();
        assertNotNull( stages );

        assertNull( result.getModelASTPipelineDef().getTools() );

        String stageGroovy = stages.get(0).toGroovy();
        System.out.println( "groovy: " + stageGroovy );

        assertThat( stageGroovy, CoreMatchers.containsString( "mvn" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "clean verify" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-Dmaven.repo.local=.repository" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-f thepom.xml" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-DskipTests -Dfoo=bar" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-s settings.xml" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-gs global.xml" ) );
        assertThat( stageGroovy, CoreMatchers.containsString(
            "MAVEN_OPTS = '-Djava.awt.headless=true -Xmx768m -Xms768m -client -XX:+HeapDumpOnOutOfMemoryError'" ) );
    }

    @Test
    public void simpleMavenConversionNoValues()
        throws Exception
    {

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        GlobalConfigFiles store =
            j.getInstance().getExtensionList( GlobalConfigFiles.class ).get( GlobalConfigFiles.class );

        String content = IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResource( "global-maven-settings.xml" ) );
        GlobalMavenSettingsConfig globalMavenSettingsConfig =
            new GlobalMavenSettingsConfig( "global-maven-settings-id", "global-maven-settings-name", "comment",
                                           content );
        store.save( globalMavenSettingsConfig );


        GlobalSettingsProvider globalSettingsProvider = new FilePathGlobalSettingsProvider( "global.xml" );
        SettingsProvider settingsProvider = new FilePathSettingsProvider( "settings.xml" );

        Maven maven = new Maven( "clean verify", null /*maven name*/, null,
                                 null /*properties*/,
                                 null /*jvmOptions*/,
                                 true /*usePrivateRepository*/, null /*SettingsProvider settings*/,
                                 null /*GlobalSettingsProvider*/ );

        MavenConverter mavenConverter = j.jenkins.getExtensionList( MavenConverter.class ).get( 0 );

        assertTrue( mavenConverter.canConvert( maven ) );
        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult result = new ConverterResult();
        assertTrue(mavenConverter.convert( request, result, maven ));
        List<ModelASTStage> stages = result.getModelASTPipelineDef().getStages().getStages();
        assertNotNull( stages );

        assertNull( result.getModelASTPipelineDef().getTools() );

        String stageGroovy = stages.get(0).toGroovy();
        System.out.println( "groovy: " + stageGroovy );

        assertThat( stageGroovy, CoreMatchers.containsString( "mvn" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "clean verify" ) );
        assertThat( stageGroovy, CoreMatchers.containsString( "-Dmaven.repo.local=.repository" ) );
    }

}
