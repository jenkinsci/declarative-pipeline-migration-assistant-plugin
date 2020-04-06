package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.configfiles.buildwrapper.ConfigFileBuildWrapper;
import org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OptionalExtension(requirePlugins = { "config-file-provider" })
public class ConfigFileBuildWrapperConverter extends SingleTypedConverter<ConfigFileBuildWrapper>
{
    private Logger LOGGER = LoggerFactory.getLogger( ConfigFileBuildWrapperConverter.class );

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        ConfigFileBuildWrapper configFileBuildWrapper = (ConfigFileBuildWrapper) target;
        if(configFileBuildWrapper.getManagedFiles() == null || configFileBuildWrapper.getManagedFiles().isEmpty() )
        {
            return true;
        }
        result.addWrappingTreeStep( () -> build( configFileBuildWrapper ) );
        return true;
    }

    private ModelASTTreeStep build(ConfigFileBuildWrapper configFileBuildWrapper) {
        ModelASTTreeStep configFileProvider = new ModelASTTreeStep( this );

        configFileProvider.setName( "configFileProvider" );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( null);
        configFileProvider.setArgs( singleArgument );

        //for( ManagedFile managedFile : configFileBuildWrapper.getManagedFiles() ){
        // only the 1st one
        ManagedFile managedFile = configFileBuildWrapper.getManagedFiles().get( 0 );
        //configFileProvider([configFile(fileId: 'yup', targetLocation: 'myfile.txt')])
        // FIXME we need something with java code to generate this...
        StringBuilder gstring = new StringBuilder( "[configFile(fileId:'" );
        gstring.append( managedFile.getFileId());
        gstring.append( "', targetLocation: '" );
        gstring.append( managedFile.getTargetLocation() );
        gstring.append( "')]" );
        // TODO what about variable???
        singleArgument.setValue( ModelASTValue.fromGString( gstring.toString(), this ) );

        //}
        return configFileProvider;
    }
}
