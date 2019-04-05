package io.jenkins.plugins.sample.io.jenkins.plugins.todeclarative;

import com.coravy.hudson.plugins.github.GithubProjectProperty;
import hudson.maven.MavenModuleSet;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.freestyle.FreestyleToDeclarativeConverter;
import io.jenkins.plugins.todeclarative.converter.maven.MavenToDeclarativeConverter;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FreestyleTest
{

    @Rule
    public JenkinsRule j  = new JenkinsRule();

    @Test
    public void freestyle_conversion() throws Exception {

        Slave slave = j.createOnlineSlave();
        slave.setLabelString( "FOO_AGENT" );

        String projectName = Long.toString( System.currentTimeMillis() );
        FreeStyleProject p = j.createFreeStyleProject( projectName );
        p.addProperty( new GithubProjectProperty( "http://github.com/beer/paleale") );

        FreestyleToDeclarativeConverter converter = Jenkins.getInstance()
            .getExtensionList( FreestyleToDeclarativeConverter.class ).get( 0 );

        Assert.assertTrue( converter.canConvert( p ) );

        ConverterRequest request = new ConverterRequest().job( p );
        ConverterResult converterResult = new ConverterResult()
            .modelASTPipelineDef( new ModelASTPipelineDef(null));

        converter.convert( request, converterResult);
        String groovy = converterResult.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );


    }

}
