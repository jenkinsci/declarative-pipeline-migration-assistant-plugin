package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.model.FreeStyleProject;
import hudson.tasks.junit.JUnitResultArchiver;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPostBuild;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class JUnitResultArchiverConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );

        JUnitResultArchiver publisher = new JUnitResultArchiver( "**/**.xml");
        publisher.setHealthScaleFactor( 2 );
        publisher.setKeepLongStdio( true );
        p.getPublishersList().add( publisher );

        JUnitResultArchiverConverter converter = j.jenkins.getExtensionList( JUnitResultArchiverConverter.class ).get( 0 );
        assertTrue( converter.canConvert( publisher ) );
        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest().job( p ), result, publisher );
        ModelASTPostBuild postBuild = result.getModelASTPipelineDef().getPostBuild();
        assertEquals( 1, postBuild.getConditions().size() );
        String groovy = postBuild.getConditions().get( 0 ).toGroovy();
        assertThat( groovy, containsString( "always" ) );
        //step($class: 'JUnitResultArchiver', healthScaleFactor: 2.0, keepLongStdio: true, testResults: '**/**.xml')
        assertThat( groovy, containsString( "$class: 'JUnitResultArchiver'" ) );
        assertThat( groovy, containsString( "healthScaleFactor: 2.0" ) );
        assertThat( groovy, containsString( "keepLongStdio: true" ) );
        assertThat( groovy, containsString( "testResults: '**/**.xml'" ) );
    }
}
