package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.Extension;
import hudson.tasks.Publisher;
import hudson.tasks.test.AggregatedTestResultPublisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.publisher.PublisherConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

import java.util.logging.Logger;

@Extension
public class AggregatedTestResultPublisherConverter implements PublisherConverter
{

    private static final Logger LOGGER = Logger.getLogger( AggregatedTestResultPublisherConverter.class.getName());

    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher )
    {
        // FIXME warnings
        LOGGER.info( "Cannot convert publisher:" + publisher.getClass() );
        return null;


//        ModelASTBuildCondition buildCondition =
//            ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
//
//        ModelASTBranch branch = buildCondition.getBranch();
//        if(branch==null){
//            branch =new ModelASTBranch( this );
//            buildCondition.setBranch( branch );
//        }
//
//        //  step([$class: 'AggregatedTestResultPublisher', includeFailedBuilds: true])
//        ModelASTStep step = new ModelASTStep(this);
//        branch.getSteps().add( step );
//        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( null);
//        AggregatedTestResultPublisher aggregatedTestResultPublisher = (AggregatedTestResultPublisher) publisher;
//
//        StringBuilder gstring = new StringBuilder( "[$class: 'hudson.tasks.test.AggregatedTestResultPublisher'"
//                                                       + ", jobs:'" + aggregatedTestResultPublisher.jobs
//                                                       + "', includeFailedBuilds: true]" );
//        singleArgument.setValue( ModelASTValue.fromGString( gstring.toString(), this ) );
//        step.setArgs( singleArgument );
//        step.setName( "step" );
//
//        return null;
    }

    @Override
    public boolean canConvert( Publisher publisher )
    {
        return publisher instanceof AggregatedTestResultPublisher;
    }
}
