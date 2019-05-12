package io.jenkins.plugins.todeclarative.converter.publishers;

import hudson.Extension;
import hudson.tasks.Publisher;
import hudson.tasks.junit.JUnitResultArchiver;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.ModelASTUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.HashMap;
import java.util.Map;

@Extension
public class JUnitResultArchiverConverter
    implements PublisherConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher )
    {
        JUnitResultArchiver jUnitResultArchiver = (JUnitResultArchiver) publisher;
        // FIXME must depends on Threshold
        //buildTrigger.getThreshold()
        ModelASTBuildCondition buildCondition =
            ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );

        ModelASTBranch branch = buildCondition.getBranch();
        if(branch==null){
            branch =new ModelASTBranch( this );
            buildCondition.setBranch( branch );
        }
        // junit(testResults: 'foof', allowEmptyResults: true, healthScaleFactor: 1.0,
        // keepLongStdio: true)

        ModelASTStep junit = new ModelASTStep( this );
        junit.setName( "junit" );
        branch.getSteps().add( junit );

        Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
        { // testResults
            ModelASTKey testResults = new ModelASTKey( this );
            testResults.setKey( "testResults" );
            args.put( testResults, ModelASTValue.fromConstant( jUnitResultArchiver.getTestResults(), this ) );
        }

        { // healthScaleFactor
            if(jUnitResultArchiver.getHealthScaleFactor()>0)
            {
                ModelASTKey healthScaleFactor = new ModelASTKey( this );
                healthScaleFactor.setKey( "healthScaleFactor" );
                args.put( healthScaleFactor,
                          ModelASTValue.fromConstant( jUnitResultArchiver.getHealthScaleFactor(), this ) );
            }
        }

        { // keepLongStdio
            ModelASTKey keepLongStdio = new ModelASTKey( this );
            keepLongStdio.setKey( "keepLongStdio" );
            args.put( keepLongStdio, ModelASTValue.fromConstant( jUnitResultArchiver.isKeepLongStdio(), this ) );
        }

        ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList( null);
        stepArgs.setArguments( args );
        junit.setArgs( stepArgs );


        return null;
    }

    @Override
    public boolean canConvert( Publisher publisher )
    {
        return publisher instanceof JUnitResultArchiver;
    }
}
