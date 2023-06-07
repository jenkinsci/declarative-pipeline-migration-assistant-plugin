package io.jenkins.plugins.todeclarative.converter.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import java.util.Collections;

@Extension
public class NoBuilderConverter extends SingleTypedConverter<Builder>
{
    public static final String NO_BUILDER_NUMBER_KEY = NoBuilderConverter.class.getName() + ".step.number";

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        Builder builder = (Builder) target;
        ModelASTStage stage = new ModelASTStage( this );
        int stageNumber = request.getAndIncrement( NO_BUILDER_NUMBER_KEY );
        stage.setName( "No Converter-" + stageNumber );
        ModelASTBranch branch = new ModelASTBranch( this );
        stage.setBranches(Collections.singletonList(branch));
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
        ModelASTUtils.wrapBranch(result, step, branch);
        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);

        // this is false because it's not really converting anything
        return false;
    }

    @Override
    public boolean canConvert(@NonNull Object object) {
        return false;
    }
}
