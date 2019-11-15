package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.tasks.Builder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.builder.BuilderConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;

import java.util.Arrays;


@Extension
public class NoConverterBuilder
    implements BuilderConverter
{
    public static final String NO_BUILDER_NUMBER_KEY = NoConverterBuilder.class.getName() + ".step.number";

    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder )
    {
        ModelASTStage stage = new ModelASTStage( this );
        int stageNumber = request.getAndIncrement( NO_BUILDER_NUMBER_KEY );
        stage.setName( "No Converter-" + stageNumber );
        ModelASTBranch branch = new ModelASTBranch( this );
        stage.setBranches( Arrays.asList( branch ) );
        ModelASTStep step = new ModelASTStep( this ){
            // need to override as per default () added at the end which doesn't work for echo..
            @Override
            public String toGroovy()
            {
                return this.getName();
            }
        };
        step.setName( "echo 'No converter for Builder: " + builder.getClass().getName() + "'" );
        step.setArgs( null );
        wrapBranch(converterResult, step, branch);

        return stage;
    }

    @Override
    public boolean canConvert( Builder builder )
    {
        return false;
    }
}
