package io.jenkins.plugins.todeclarative.converter.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.tasks.Publisher;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;

@Extension
public class NoPublisherConverter extends SingleTypedConverter<Publisher> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {

        ModelASTBuildCondition buildCondition =
                ModelASTUtils.buildOrFindBuildCondition(result.getModelASTPipelineDef(), "always");
        ModelASTStep step = new ModelASTStep(this) {
            // need to override as per default () added at the end which doesn't work for echo..
            @Override
            public String toGroovy() {
                return this.getName();
            }
        };
        step.setName("echo 'No converter for Publisher: " + target.getClass().getName() + "'");
        step.setArgs(null);
        ModelASTUtils.addStep(buildCondition, step);
        // not a real conversion, so return false
        return false;
    }

    @Override
    public boolean canConvert(@NonNull Object object) {
        return false;
    }
}
