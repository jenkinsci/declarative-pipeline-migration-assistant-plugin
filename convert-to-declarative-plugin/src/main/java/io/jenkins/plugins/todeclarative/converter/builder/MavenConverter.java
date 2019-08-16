package io.jenkins.plugins.todeclarative.converter.builder;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.JDK;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.builder.BuilderConverter;
import jenkins.mvn.SettingsProvider;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTTools;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.Arrays;

@Extension
public class MavenConverter implements BuilderConverter
{
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder )
    {
        ModelASTTools tools = converterResult.getModelASTPipelineDef().getTools();
        if(tools==null){
            tools = new ModelASTTools( this );
            converterResult.getModelASTPipelineDef().setTools(tools);
        }
        Maven maven = (Maven) builder;
        Maven.MavenInstallation mavenInstallation = maven.getMaven();
        if(mavenInstallation!=null){
            ModelASTKey key = new ModelASTKey( this );
            key.setKey( "maven" );
            tools.getTools().put( key, ModelASTValue.fromConstant( mavenInstallation.getName(), this ) );
        }

        FreeStyleProject freeStyleProject = (FreeStyleProject) request.getJob();
        JDK jdk = freeStyleProject.getJDK();
        if(jdk!=null){
            ModelASTKey key = new ModelASTKey( this );
            key.setKey( "jdk" );
            tools.getTools().put( key, ModelASTValue.fromConstant( jdk.getName(), this ) );
        }


        ModelASTStage stage = new ModelASTStage( this );
        stage.setName( "Maven Build ");
        ModelASTBranch branch = new ModelASTBranch( this );
        stage.setBranches( Arrays.asList( branch ) );
        ModelASTStep step = new ModelASTStep( this );

        branch.setSteps( Arrays.asList( step ) );

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add( "mvn" );
        Arrays.asList(StringUtils.split( maven.getTargets() )).stream().forEach( s -> args.add( s ) );
        

        //String settingsPath = SettingsProvider.getSettingsRemotePath( maven.getSettings(), build, listener);
        SettingsProvider settingsProvider = maven.getSettings();
//        if(settingsProvider!=null)
//        if( StringUtils.isNotBlank( settingsPath)){
//            args.add("-s", settingsPath);
//        }


        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( this );
        singleArgument.setValue( ModelASTValue.fromConstant( args.toString(), this ) );

        step.setArgs( singleArgument );
        step.setName( "sh" );
        return stage;

    }

    @Override
    public boolean canConvert( Builder builder )
    {
        return builder instanceof Maven;
    }
}
