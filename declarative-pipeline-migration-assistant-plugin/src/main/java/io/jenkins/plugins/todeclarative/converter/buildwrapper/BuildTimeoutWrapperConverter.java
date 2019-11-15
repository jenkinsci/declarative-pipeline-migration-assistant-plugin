package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import hudson.Extension;
import hudson.plugins.build_timeout.BuildTimeOutStrategy;
import hudson.plugins.build_timeout.BuildTimeoutWrapper;
import hudson.plugins.build_timeout.impl.AbsoluteTimeOutStrategy;
import hudson.tasks.BuildWrapper;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import io.jenkins.plugins.todeclarative.converter.api.buildwrapper.BuildWrapperConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.variant.OptionalExtension;

import static io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils.buildKeyPairArg;

@OptionalExtension(requirePlugins = { "build-timeout" })
public class BuildTimeoutWrapperConverter
    implements BuildWrapperConverter
{
    private Logger LOGGER = LoggerFactory.getLogger( BuildTimeoutWrapperConverter.class );

    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, BuildWrapper wrapper )
    {
        BuildTimeoutWrapper timeoutWrapper = (BuildTimeoutWrapper)wrapper;
        BuildTimeOutStrategy strategy = timeoutWrapper.getStrategy();

        if(!(strategy instanceof AbsoluteTimeOutStrategy)){
            converterResult.addWarning( new Warning( "we can only convert Absolute timeout and not "
                                                                         + strategy.getDescriptor().getDisplayName(),
                                                     BuildTimeOutStrategy.class.getName() ));
            return null;
        }

        ModelASTOption timeout = new ModelASTOption( this );
        timeout.setName( "timeout" );

        List<ModelASTMethodArg> timeoutArgs = new ArrayList<>();

        AbsoluteTimeOutStrategy absoluteTimeOutStrategy = (( AbsoluteTimeOutStrategy)strategy);

        timeoutArgs.add( buildKeyPairArg("time", Long.parseLong( absoluteTimeOutStrategy.getTimeoutMinutes())));
        timeoutArgs.add( buildKeyPairArg("unit", "MINUTES"));

        timeout.setArgs( timeoutArgs );

        ModelASTPipelineDef modelASTPipelineDef = converterResult.getModelASTPipelineDef();
        if(modelASTPipelineDef.getOptions()==null){
            modelASTPipelineDef.setOptions( new ModelASTOptions( this ) );
        }
        modelASTPipelineDef.getOptions().getOptions().add( timeout );
        return null;
    }

    @Override
    public boolean canConvert( BuildWrapper wrapper )
    {
        return wrapper instanceof BuildTimeoutWrapper;
    }
}
