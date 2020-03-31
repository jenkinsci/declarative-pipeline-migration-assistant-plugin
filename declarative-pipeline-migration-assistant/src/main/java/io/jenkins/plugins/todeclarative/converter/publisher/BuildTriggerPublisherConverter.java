package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.model.AbstractProject;
import hudson.tasks.BuildTrigger;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = { "pipeline-build-step" })
public class BuildTriggerPublisherConverter extends SingleTypedConverter<BuildTrigger>
{
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        BuildTrigger buildTrigger = (BuildTrigger) target;
        // FIXME must depends on Threshold
        //buildTrigger.getThreshold()
        if (buildTrigger.getChildJobs((AbstractProject<?, ?>) request.getJob()) == null ||
            buildTrigger.getChildJobs((AbstractProject<?, ?>) request.getJob()).isEmpty()) {
            return true;
        }
        ModelASTBuildCondition buildCondition =
            ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
        buildTrigger.getChildJobs((AbstractProject<?, ?>) request.getJob()).forEach( abstractProject -> {
            ModelASTBranch branch = buildCondition.getBranch();
            if(branch==null){
                branch =new ModelASTBranch( this );
                buildCondition.setBranch( branch );
            }
            // build job: 'PayloadJob'
            // TODO parameters from the main project??
            ModelASTStep build = new ModelASTStep( this ){
                @Override
                public String toGroovy()
                {
                    // we need very simple build(job: 'foo')
                    return "build(job: '"+abstractProject.getName()+"')";
                }
            };
            build.setName( "build" );
            branch.getSteps().add( build );
//
//            Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
//
//            ModelASTKey job = new ModelASTKey( this );
//            job.setKey( "job" );
//            ModelASTValue jobName = ModelASTValue.fromConstant( abstractProject.getName(), this );
//            args.put( job, jobName );
//
//            ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList( null);
//            stepArgs.setArguments( args );
//            build.setArgs( stepArgs );
//            String groovy = build.toGroovy();
//            System.out.println( groovy );
        } );
        return true;
    }
}
