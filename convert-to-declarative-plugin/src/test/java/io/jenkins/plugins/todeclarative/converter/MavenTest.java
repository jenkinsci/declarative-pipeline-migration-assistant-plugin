package io.jenkins.plugins.todeclarative.converter;

import hudson.maven.MavenModuleSet;
import hudson.model.Slave;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.maven.MavenToDeclarativeConverter;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class MavenTest
{

    @Rule
    public JenkinsRule j  = new JenkinsRule();

    @Test
    public void foo() throws Exception {

        Slave slave = j.createOnlineSlave();
        slave.setLabelString( "FOO_AGENT" );

        MavenModuleSet mavenModuleSet = new MavenModuleSet( j.jenkins, "Foo"  );
        mavenModuleSet.setGoals( "clean deploy" );
        mavenModuleSet.setAssignedNode( slave );

        MavenToDeclarativeConverter converter = Jenkins.getInstance()
            .getExtensionList( MavenToDeclarativeConverter.class ).get( 0 );

        Assert.assertTrue( converter.canConvert( mavenModuleSet ) );

        ConverterRequest request = new ConverterRequest().job( mavenModuleSet ).useWithMvn( true );

        ModelASTStage stage = converter.toWithMvn( request );
        String groovy = ModelASTPipelineDef.toIndentedGroovy( stage.toGroovy());

        System.out.println( groovy );

        ConverterResult result = new ConverterResult()
            .modelASTPipelineDef( new ModelASTPipelineDef(null));

        converter.convert( request, result);

        groovy = result.getModelASTPipelineDef().toPrettyGroovy();

        System.out.println( groovy );




        /*
        ModelASTStage stage = new ModelASTStage( null );
        stage.setName( "Stage 1" );
        ModelASTBranch branch = new ModelASTBranch(null);
        branch.setName( "Branch 1" );

        ModelASTTreeStep withMvn = new ModelASTTreeStep( null);
        withMvn.setName( "withMvn" );

        ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList( null);
        Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
        ModelASTKey key = new ModelASTKey(this);
        key.setKey( "withMvnKey" );
        ModelASTValue value = ModelASTValue.fromConstant("withMvnArgs", this);
        args.put( key, value);

        stepArgs.setArguments( args );
        withMvn.setArgs( stepArgs );


        ModelASTStep sh = new ModelASTStep(null);
        sh.setName( "sh" );
        withMvn.getChildren().add( sh );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument(this);
        singleArgument.setValue( ModelASTValue.fromConstant( "clean deploy", null ));
        sh.setArgs( singleArgument );

        branch.getSteps().add( withMvn );

        stage.getBranches().add( branch );

        ModelASTAgent agent = new ModelASTAgent( this );
        ModelASTKey agentKey = new ModelASTKey(this);
        agentKey.setKey( "\'linux\'" );

        agent.setAgentType( agentKey );
        stage.setAgent( agent );


        ModelASTPipelineDef modelASTPipelineDef = new ModelASTPipelineDef(null);

        ModelASTStages modelASTStages = new ModelASTStages( null );
        modelASTStages.setStages( Collections.singletonList( stage ) );
        modelASTPipelineDef.setStages( modelASTStages );

        String groovy = modelASTPipelineDef.toPrettyGroovy();

        System.out.println( groovy );
        */

    }

}
