package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.tasks.Publisher;
import hudson.tasks.junit.JUnitResultArchiver;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = { "junit" })
public class JUnitResultArchiverConverter
    implements PublisherConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher )
    {
        JUnitResultArchiver jUnitResultArchiver = (JUnitResultArchiver) publisher;
        ModelASTBuildCondition buildCondition =
            ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );

        ModelASTBranch branch = buildCondition.getBranch();
        if(branch==null){
            branch =new ModelASTBranch( this );
            buildCondition.setBranch( branch );
        }
        // junit(testResults: 'foof', allowEmptyResults: true, healthScaleFactor: 1.0,
        // keepLongStdio: true)
        ModelASTStep junit =  buildGenericStep( jUnitResultArchiver );
        branch.getSteps().add( junit );
        return null;
    }

    @Override
    public boolean canConvert( Publisher publisher )
    {
        return publisher instanceof JUnitResultArchiver;
    }
}
