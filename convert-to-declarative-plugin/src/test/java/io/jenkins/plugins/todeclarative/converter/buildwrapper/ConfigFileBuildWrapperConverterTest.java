package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.configfiles.buildwrapper.ConfigFileBuildWrapper;
import org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ConfigFileBuildWrapperConverterTest
{

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        ConfigFileBuildWrapperConverter converter =
            j.jenkins.getExtensionList( ConfigFileBuildWrapperConverter.class ).get( 0 );

        ManagedFile managedFile1 = new ManagedFile( "id1", "myfile1.txt", "MYFILE1" );
        ManagedFile managedFile2 = new ManagedFile( "id2", "myfile2.txt", "MYFILE2" );
        ConfigFileBuildWrapper wrapper = new ConfigFileBuildWrapper( Arrays.asList( managedFile1, managedFile2 ) );
        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, wrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        String groovy = result.getWrappingTreeSteps().get( 0 ).get().toGroovy();
        assertThat( groovy, containsString( "configFileProvider" ) );
        assertThat( groovy, containsString( "[configFile(" ) );
        assertThat( groovy, containsString( "fileId:'id1'" ) );
        assertThat( groovy, containsString( "targetLocation: 'myfile1.txt'" ) );
    }
}
