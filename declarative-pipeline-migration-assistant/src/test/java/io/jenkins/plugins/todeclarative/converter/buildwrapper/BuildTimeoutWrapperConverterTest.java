package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import hudson.ExtensionList;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;
import hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy;
import hudson.plugins.build_timeout.operations.FailOperation;
import io.jenkins.plugins.todeclarative.converter.api.BaseConverter;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BuildTimeoutWrapperConverterTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpletest()
        throws Exception
    {
        ExtensionList<BaseConverter> allConverters = SingleTypedConverter.all();
        ExtensionList<BuildTimeoutWrapperConverter> btwList = j.jenkins.getExtensionList(BuildTimeoutWrapperConverter.class);
        BuildTimeoutWrapperConverter converter =
            j.jenkins.getExtensionList( BuildTimeoutWrapperConverter.class ).get( 0 );

        BuildTimeoutWrapper wrapper = new BuildTimeoutWrapper( new AbsoluteTimeOutStrategy( "180" ),
                                                               Collections.singletonList( new FailOperation() ),
                                                               "FOO" );
        ConverterResult result = new ConverterResult();
        converter.convert( new ConverterRequest(), result, wrapper );
        ModelASTOptions options = result.getModelASTPipelineDef().getOptions();
        assertEquals( 1, options.getOptions().size() );
        ModelASTOption option = options.getOptions().get( 0 );
        assertEquals( "timeout", option.getName() );
        String groovy = option.toGroovy();
        assertThat( groovy, containsString( "time: 180" ) );
        assertThat( groovy, containsString( "unit: 'MINUTES'" ) );
    }
}
