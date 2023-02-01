package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import hudson.plugins.ansicolor.AnsiColorBuildWrapper;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AnsiColorWrapperConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        AnsiColorWrapperConverter converter =
            j.jenkins.getExtensionList( AnsiColorWrapperConverter.class ).get( 0 );

        AnsiColorBuildWrapper wrapper = new AnsiColorBuildWrapper( "beer" );
        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, wrapper );
        ModelASTOptions options = result.getModelASTPipelineDef().getOptions();
        assertEquals( 1, options.getOptions().size() );
        ModelASTOption option = options.getOptions().get( 0 );
        assertEquals( "ansiColor", option.getName() );
        String groovy = option.toGroovy();
        assertThat( groovy, containsString( "'beer'" ) );
    }
}
