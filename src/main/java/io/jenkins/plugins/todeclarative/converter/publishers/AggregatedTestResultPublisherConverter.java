package io.jenkins.plugins.todeclarative.converter.publishers;

import hudson.Extension;
import hudson.tasks.Publisher;
import hudson.tasks.test.AggregatedTestResultPublisher;
import io.jenkins.plugins.todeclarative.converter.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;

@Extension
public class AggregatedTestResultPublisherConverter implements PublisherConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher )
    {
        // FIXME logs and warnings
        System.out.println( "Cannot convert publisher:" + publisher.getClass() );
        return null;
    }

    @Override
    public boolean canConvert( Publisher publisher )
    {
        return publisher instanceof AggregatedTestResultPublisher;
    }
}
