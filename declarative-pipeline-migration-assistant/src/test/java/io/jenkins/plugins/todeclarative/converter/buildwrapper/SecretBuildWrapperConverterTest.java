package io.jenkins.plugins.todeclarative.converter.buildwrapper;


import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.credentialsbinding.Binding;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.jenkinsci.plugins.credentialsbinding.impl.CertificateMultiBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.FileBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.Messages;
import org.jenkinsci.plugins.credentialsbinding.impl.SSHUserPrivateKeyBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper;
import org.jenkinsci.plugins.credentialsbinding.impl.StringBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordMultiBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.ZipFileBinding;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SecretBuildWrapperConverterTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SecretBuildWrapperConverterTest.class );

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void usernamePassword()
    {
        ConverterResult result = new ConverterResult();
        UsernamePasswordMultiBinding binding = //
            new UsernamePasswordMultiBinding( "unameVar", "pwdVar", "credId" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        String groovy = tree.toGroovy();
        assertThat( groovy, containsString( "usernamePassword(" ) );
        assertThat( groovy, containsString( "usernameVariable: 'unameVar'" ) );
        assertThat( groovy, containsString( "passwordVariable: 'pwdVar'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credId'" ) );
    }

    @Test
    public void usernameColonPassword()
    {
        ConverterResult result = new ConverterResult();
        UsernamePasswordBinding binding = //
            new UsernamePasswordBinding( "theVariable", "credId" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        String groovy = tree.toGroovy();
        assertThat( groovy, containsString( "usernameColonPassword(" ) );
        assertThat( groovy, containsString( "variable: 'theVariable'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credId'" ) );
    }

    @Test
    public void string()
    {
        ConverterResult result = new ConverterResult();
        StringBinding binding = //
            new StringBinding( "theVariable", "credId" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        String groovy = tree.toGroovy();
        assertThat( groovy, containsString( "string(" ) );
        assertThat( groovy, containsString( "variable: 'theVariable'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credId'" ) );
    }

    @Test
    public void file()
    {
        ConverterResult result = new ConverterResult();
        FileBinding binding = //
            new FileBinding( "theVariable", "credId" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        String groovy = tree.toGroovy();
        assertThat( groovy, containsString( "file(" ) );
        assertThat( groovy, containsString( "variable: 'theVariable'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credId'" ) );
    }

    @Test
    public void zip()
    {
        ConverterResult result = new ConverterResult();
        ZipFileBinding binding = //
            new ZipFileBinding( "theVariable", "credId" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        String groovy = tree.toGroovy();
        assertThat( groovy, containsString( "zip(" ) );
        assertThat( groovy, containsString( "variable: 'theVariable'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credId'" ) );
    }

    @Test
    public void certificate()
    {
        ConverterResult result = new ConverterResult();
        CertificateMultiBinding binding = //
            new CertificateMultiBinding( "theVariable", "credId" );
        binding.setAliasVariable( "thealias" );
        binding.setPasswordVariable( "pwdVar" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        String groovy = tree.toGroovy();
        assertThat( groovy, containsString( "certificate(" ) );
        assertThat( groovy, containsString( "keystoreVariable: 'theVariable'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credId'" ) );
        assertThat( groovy, containsString( "aliasVariable: 'thealias'" ) );
        assertThat( groovy, containsString( "passwordVariable: 'pwdVar'" ) );
    }

    @Test
    public void sshuserprivatekey()
    {
        ConverterResult result = new ConverterResult();
        SSHUserPrivateKeyBinding binding = //
            new SSHUserPrivateKeyBinding( "theVariable", "credId" );
        binding.setPassphraseVariable( "thepassphrase" );
        binding.setUsernameVariable( "uidVar" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        String groovy = tree.toGroovy();
        assertThat( groovy, containsString( "sshUserPrivateKey(" ) );
        assertThat( groovy, containsString( "keyFileVariable: 'theVariable'" ) );
        assertThat( groovy, containsString( "credentialsId: 'credId'" ) );
        assertThat( groovy, containsString( "usernameVariable: 'uidVar'" ) );
        assertThat( groovy, containsString( "passphraseVariable: 'thepassphrase'" ) );
    }

    @Test
    public void fakegeneratewarning()
    {
        ConverterResult result = new ConverterResult();
        FakeBinding binding = //
            new FakeBinding( "theVariable", "credId" );
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper( Arrays.asList( binding ) );
        SecretBuildWrapperConverter converter = j.jenkins.getExtensionList( SecretBuildWrapperConverter.class ).get(0);
        converter.convert( null, result, secretBuildWrapper );
        assertEquals( 1, result.getWrappingTreeSteps().size() );
        ModelASTTreeStep tree = result.getWrappingTreeSteps().get( 0 ).get();
        assertEquals( 1, result.getWarnings().size() );
    }


    public static class FakeBinding
        extends Binding<StandardUsernamePasswordCredentials>
    {
        @DataBoundConstructor
        public FakeBinding( String variable, String credentialsId) {
            super(variable, credentialsId);
        }

        protected Class<StandardUsernamePasswordCredentials> type() {
            return StandardUsernamePasswordCredentials.class;
        }

        public SingleEnvironment bindSingle( @Nonnull Run<?, ?> build, @Nullable FilePath workspace, @Nullable Launcher launcher, @Nonnull TaskListener listener) throws
            IOException, InterruptedException {
            return null;
        }

        @Symbol({"fakeBinding"})
        @Extension
        public static class DescriptorImpl extends BindingDescriptor<StandardUsernamePasswordCredentials>
        {
            public DescriptorImpl() {
            }

            protected Class<StandardUsernamePasswordCredentials> type() {
                return StandardUsernamePasswordCredentials.class;
            }

            public String getDisplayName() {
                return "foo";
            }

            public boolean requiresWorkspace() {
                return false;
            }
        }
    }
}
