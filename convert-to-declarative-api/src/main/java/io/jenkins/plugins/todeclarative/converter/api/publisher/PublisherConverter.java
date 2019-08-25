package io.jenkins.plugins.todeclarative.converter.api.publisher;

import hudson.tasks.Publisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.structs.describable.DescribableModel;

import java.util.HashMap;
import java.util.Map;

/**
 * All implementations of this extension will be used to convert {@link Publisher}.
 * Your extension can either return a {@link ModelASTStage} or modify the model from the {@link ConverterResult} instance.
 */
public interface PublisherConverter
{
    ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher );

    /**
     * @param publisher the publisher to convert
     * @return <code>true</code> if the extension is able to convert the {@link Publisher}
     */
    boolean canConvert( Publisher publisher );

    default ModelASTStep buildGenericStep(Publisher publisher){

        Class<Publisher> actualPublisherClass = (Class<Publisher>)publisher.getClass();
        DescribableModel<Publisher> model = DescribableModel.of( actualPublisherClass);
        Map<String, Object> uninstantiated = model.uninstantiate2( publisher).toMap();

        ModelASTStep genericStep = new ModelASTStep( this );
        genericStep.setName( "step" );

        Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
        ModelASTKey key = new ModelASTKey( this );
        key.setKey( "$class" );
        ModelASTValue value = ModelASTValue.fromConstant( model.getType().getSimpleName(), this );
        args.put( key, value );

        for (Map.Entry<String, Object> arg: uninstantiated.entrySet()) {
            key = new ModelASTKey( this );
            key.setKey( arg.getKey() );
            value = ModelASTValue.fromConstant( arg.getValue(), this );
            args.put( key, value );
        }

        ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList( null);
        stepArgs.setArguments( args );
        genericStep.setArgs( stepArgs );

        return genericStep;
    }

}
