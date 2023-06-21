package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.tasks.junit.JUnitResultArchiver;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = {"junit"})
public class JUnitResultArchiverConverter extends SingleTypedConverter<JUnitResultArchiver> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        JUnitResultArchiver jUnitResultArchiver = (JUnitResultArchiver) target;
        ModelASTBuildCondition buildCondition =
                ModelASTUtils.buildOrFindBuildCondition(result.getModelASTPipelineDef(), "always");

        // junit(testResults: 'foof', allowEmptyResults: true, healthScaleFactor: 1.0,
        // keepLongStdio: true)
        ModelASTStep junit = ModelASTUtils.buildGenericStep(jUnitResultArchiver, this);
        ModelASTUtils.addStep(buildCondition, junit);
        return true;
    }
}
