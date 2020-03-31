package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.tasks.test.AggregatedTestResultPublisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import io.jenkins.plugins.todeclarative.converter.api.Warning;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OptionalExtension(requirePlugins = { "junit" })
public class AggregatedTestResultPublisherConverter extends SingleTypedConverter<AggregatedTestResultPublisher>
{

    private static final Logger LOGGER = LoggerFactory.getLogger( AggregatedTestResultPublisherConverter.class.getName());

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        result.addWarning( new Warning( "Cannot convert publisher:" + target.getClass(),
                                        AggregatedTestResultPublisher.class.getName() ));
        return false;


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
}
