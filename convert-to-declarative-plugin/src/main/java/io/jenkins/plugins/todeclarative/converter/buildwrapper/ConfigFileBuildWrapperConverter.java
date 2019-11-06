package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import hudson.tasks.BuildWrapper;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.buildwrapper.BuildWrapperConverter;
import org.jenkinsci.plugins.configfiles.buildwrapper.ConfigFileBuildWrapper;
import org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTreeStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OptionalExtension(requirePlugins = { "config-file-provider" })
public class ConfigFileBuildWrapperConverter
    implements BuildWrapperConverter
{
    private Logger LOGGER = LoggerFactory.getLogger( ConfigFileBuildWrapperConverter.class );

    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, BuildWrapper wrapper )
    {
        ConfigFileBuildWrapper configFileBuildWrapper = (ConfigFileBuildWrapper)wrapper;
        if(configFileBuildWrapper.getManagedFiles() == null || configFileBuildWrapper.getManagedFiles().isEmpty() )
        {
            return null;
        }
        converterResult.addWrappingTreeStep( () -> build( request, configFileBuildWrapper ) );
        return null;
    }

    private ModelASTTreeStep build(ConverterRequest request, ConfigFileBuildWrapper configFileBuildWrapper) {
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

    @Override
    public boolean canConvert( BuildWrapper wrapper )
    {
        return wrapper instanceof ConfigFileBuildWrapper;
    }
}
