package io.jenkins.plugins.todeclarative.converter.buildwrapper;

import hudson.plugins.ansicolor.AnsiColorBuildWrapper;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTMethodArg;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTOptions;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@OptionalExtension(requirePlugins = { "build-timeout" })
public class AnsiColorWrapperConverter extends SingleTypedConverter<AnsiColorBuildWrapper>
{
    private Logger LOGGER = LoggerFactory.getLogger( AnsiColorWrapperConverter.class );

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        AnsiColorBuildWrapper wrapper = (AnsiColorBuildWrapper)target;
        String colorMapName = wrapper.getColorMapName();

        if(StringUtils.isEmpty(colorMapName)) {
            return false;
        }

        ModelASTOption ansiColor = new ModelASTOption( this );
        ansiColor.setName( "ansiColor" );

        List<ModelASTMethodArg> ansiColorArgs = new ArrayList<>();
        ansiColorArgs.add(ModelASTValue.fromGString("'"+colorMapName+"'", this));

        ModelASTPipelineDef modelASTPipelineDef = result.getModelASTPipelineDef();
        if(modelASTPipelineDef.getOptions() == null)
        {
            modelASTPipelineDef.setOptions( new ModelASTOptions( this ) );
        }

        ansiColor.setArgs(ansiColorArgs);

        ModelASTUtils.addOption(modelASTPipelineDef, ansiColor);
        return true;
    }
}
