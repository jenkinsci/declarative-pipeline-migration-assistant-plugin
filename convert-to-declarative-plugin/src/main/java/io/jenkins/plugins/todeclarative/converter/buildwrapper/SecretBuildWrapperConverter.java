package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import hudson.Extension;
import hudson.tasks.BuildWrapper;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import io.jenkins.plugins.todeclarative.converter.api.buildwrapper.BuildWrapperConverter;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.credentialsbinding.MultiBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.CertificateMultiBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.FileBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.SSHUserPrivateKeyBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper;
import org.jenkinsci.plugins.credentialsbinding.impl.StringBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordMultiBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.ZipFileBinding;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Extension
public class SecretBuildWrapperConverter
    implements BuildWrapperConverter
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SecretBuildWrapperConverter.class );

    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, BuildWrapper wrapper )
    {
        SecretBuildWrapper secretBuildWrapper = SecretBuildWrapper.class.cast( wrapper );
        if ( secretBuildWrapper.getBindings() == null || secretBuildWrapper.getBindings().isEmpty() )
        {
            return null;
        }

        converterResult.addWrappingTreeStep( () -> build( request, secretBuildWrapper, converterResult) );
        return null;
    }

    // FIXME currently we manage only one binding
    private ModelASTTreeStep build( ConverterRequest request, SecretBuildWrapper secretBuildWrapper, ConverterResult converterResult )
    {

        ModelASTTreeStep withCredentials = new ModelASTTreeStep( this );

        withCredentials.setName( "withCredentials" );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( null);
        withCredentials.setArgs( singleArgument );

        for( MultiBinding binding : secretBuildWrapper.getBindings() ){
            String credentialId = binding.getCredentialsId();
            Symbol symbolAnn = binding.getDescriptor().getClass().getAnnotation( Symbol.class );
            List<String> symbols = Arrays.asList(symbolAnn.value());
            for(String symbol : symbols){

                switch ( symbol ) {
                    case "usernamePassword":
                        singleArgument.setValue(
                            ModelASTValue.fromGString( generateUsernamePassword(binding, credentialId).get(), this ) );
                        return withCredentials;
                    case "usernameColonPassword":
                        singleArgument.setValue(
                            ModelASTValue.fromGString( generateUsernameColonPassword(binding, credentialId).get(), this ) );
                        return withCredentials;
                    case "string":
                        singleArgument.setValue(
                            ModelASTValue.fromGString( generateString(binding, credentialId).get(), this ) );
                        return withCredentials;
                    case "file":
                        singleArgument.setValue( ModelASTValue.fromGString( generateFile( binding, credentialId).get(), this ) );
                        return withCredentials;
                    case "zip":
                        singleArgument.setValue( ModelASTValue.fromGString( generateZip( binding, credentialId ).get(), this ) );
                        return withCredentials;
                    case "certificate":
                        singleArgument.setValue( ModelASTValue.fromGString( generateCertificate( binding, credentialId ).get(), this ) );
                        return withCredentials;
                    case "sshUserPrivateKey":
                        singleArgument.setValue( ModelASTValue.fromGString( generateSshUser( binding, credentialId ).get(), this ) );
                        return withCredentials;
                    default:
                        LOGGER.warn( "credential binding, ignore symbol: {}",  symbol );
                        converterResult.addWarning( new Warning( "Cannot convert credential binding: '" + symbol + "'",
                                                                 secretBuildWrapper.getClass().getName()) );

                }
            }
        }

        return null;
    }


    private Supplier<String> generateUsernamePassword( MultiBinding binding, String credentialId) {
        //withCredentials([usernamePassword(usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD', credentialsId: 'creds')])
        UsernamePasswordMultiBinding usernamePasswordMultiBinding = (UsernamePasswordMultiBinding)binding;
        // FIXME we need something with java code to generate this...
        StringBuilder userPMultiString = new StringBuilder( "[usernamePassword(usernameVariable: '" );
        userPMultiString.append( usernamePasswordMultiBinding.getUsernameVariable());
        userPMultiString.append( "', passwordVariable: '" );
        userPMultiString.append( usernamePasswordMultiBinding.getPasswordVariable() );
        userPMultiString.append( "', credentialsId: '" );
        userPMultiString.append( credentialId );
        userPMultiString.append( "')]" );
        return () -> userPMultiString.toString();
    }

    private Supplier<String> generateUsernameColonPassword(MultiBinding binding, String credentialId){
        // withCredentials([usernameColonPassword(credentialsId: 'mylogin', variable: 'USERPASS')])
        UsernamePasswordBinding usernamePasswordBinding = (UsernamePasswordBinding)binding;
        // FIXME we need something with java code to generate this...
        StringBuilder userPwdString = new StringBuilder( "[usernameColonPassword(credentialsId: '" );
        userPwdString.append(credentialId);
        userPwdString.append( "', variable: '" );
        userPwdString.append( usernamePasswordBinding.getVariable());
        userPwdString.append( "')]" );
        return () -> userPwdString.toString();
    }

    private Supplier<String> generateString(MultiBinding binding, String credentialId){
        // withCredentials([string(credentialsId: 'mytoken', variable: 'TOKEN')])
        StringBinding stringBinding = (StringBinding)binding;
        // FIXME we need something with java code to generate this...
        StringBuilder stringBindingString = new StringBuilder( "[string(credentialsId: '" );
        stringBindingString.append(credentialId);
        stringBindingString.append( "', variable: '" );
        stringBindingString.append( stringBinding.getVariable());
        stringBindingString.append( "')]" );
        return () -> stringBindingString.toString();
    }

    private Supplier<String> generateFile(MultiBinding binding, String credentialId) {
        // withCredentials([file(credentialsId: 'secret', variable: 'FILE')])
        FileBinding fileBinding = (FileBinding)binding;
        // FIXME we need something with java code to generate this...
        StringBuilder fileBindingString = new StringBuilder( "[file(credentialsId: '" );
        fileBindingString.append(credentialId);
        fileBindingString.append( "', variable: '" );
        fileBindingString.append( fileBinding.getVariable());
        fileBindingString.append( "')]" );
        return () -> fileBindingString.toString();
    }

    private Supplier<String> generateZip(MultiBinding binding, String credentialId) {
        // withCredentials([zip(credentialsId: 'secret', variable: 'FILE')])
        ZipFileBinding zipFileBinding = (ZipFileBinding)binding;
        // FIXME we need something with java code to generate this...
        StringBuilder zipFileBindingString = new StringBuilder( "[zip(credentialsId: '" );
        zipFileBindingString.append(credentialId);
        zipFileBindingString.append( "', variable: '" );
        zipFileBindingString.append( zipFileBinding.getVariable());
        zipFileBindingString.append( "')]" );
        return () -> zipFileBindingString.toString();
    }

    private Supplier<String> generateCertificate(MultiBinding binding, String credentialId) {
        // withCredentials([certificate(keystoreVariable: 'MY_KEYSTORE',aliasVariable: 'KEYSTORE_ALIAS',
        //                    passwordVariable: 'KEYSTORE_PASSWORD',credentialsId: 'my-certificate')])
        CertificateMultiBinding certificateMultiBinding = (CertificateMultiBinding)binding;
        // FIXME we need something with java code to generate this...
        StringBuilder generateString = new StringBuilder( "[certificate(keystoreVariable: '" );
        generateString.append(  certificateMultiBinding.getKeystoreVariable());
        generateString.append( "', aliasVariable: '" );
        generateString.append( certificateMultiBinding.getAliasVariable() );
        generateString.append( "', passwordVariable: '" );
        generateString.append( certificateMultiBinding.getPasswordVariable() );
        generateString.append( "', credentialsId: '" );
        generateString.append( credentialId );
        generateString.append( "')]" );
        return () -> generateString.toString();
    }

    private Supplier<String> generateSshUser(MultiBinding binding, String credentialId) {
        // [sshUserPrivateKey(credentialsId: 'jenkins-ssh-key-for-abc',keyFileVariable: 'SSH_KEY_FOR_ABC',
        //                    passphraseVariable: '', usernameVariable: '')]
        SSHUserPrivateKeyBinding sshUserPrivateKeyBinding = (SSHUserPrivateKeyBinding)binding;
        // FIXME we need something with java code to generate this...
        StringBuilder generateString = new StringBuilder( "[sshUserPrivateKey(keyFileVariable: '" );
        generateString.append(  sshUserPrivateKeyBinding.keyFileVariable);
        generateString.append( "', passphraseVariable: '" );
        generateString.append( sshUserPrivateKeyBinding.getPassphraseVariable() );
        generateString.append( "', usernameVariable: '" );
        generateString.append( sshUserPrivateKeyBinding.getUsernameVariable() );
        generateString.append( "', credentialsId: '" );
        generateString.append( credentialId );
        generateString.append( "')]" );
        return () -> generateString.toString();
    }

    @Override
    public boolean canConvert( BuildWrapper wrapper )
    {
        return wrapper instanceof SecretBuildWrapper;
    }
}
