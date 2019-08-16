package io.jenkins.plugins.todeclarative.converter;

import hudson.Extension;
import hudson.scm.SCM;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.scm.ScmConverter;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jvnet.hudson.test.ExtractResourceSCM;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Extension
public class TestScmConverter
    implements ScmConverter
{
    @Override
    public void convert( ConverterRequest request, ConverterResult converterResult, SCM scm )
    {
        try
        {
            ExtractResourceSCM extractResourceSCM = (ExtractResourceSCM) scm;
            URL urlZip = getZipUrl( extractResourceSCM );
            String filePath = urlZip.getFile();

            ModelASTStage stage = new ModelASTStage( this );
            stage.setName( "Checkout Scm" );
            //git url: "", branch: '',changelog: '', credentialsId: '', pool: ''
            List<ModelASTStep> steps = new ArrayList<>();

            ModelASTStep step = new ModelASTStep( this );
            ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( this );
            // TODO olamy escape shell command?? not sure as might be done when running it
            singleArgument.setValue( ModelASTValue.fromConstant( "unzip " + filePath, this ) );
            step.setArgs( singleArgument );
            step.setName( "sh" );
            steps.add( step );

            ModelASTBranch branch = new ModelASTBranch( this );
            branch.setSteps(steps);
            stage.setBranches( Arrays.asList( branch ) );
            converterResult.getModelASTPipelineDef().getStages().getStages().add( stage );

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( e.getMessage(), e );
        }
    }


    private URL getZipUrl(ExtractResourceSCM extractResourceSCM) throws Exception
    {
        Field[] fields = extractResourceSCM.getClass().getDeclaredFields();
        for(Field field : fields)
        {
            if( StringUtils.equals( "zip", field.getName() ) )
            {
                field.setAccessible( true );
                return (URL) field.get( extractResourceSCM );
            }
        }
        return null;
    }

    @Override
    public boolean canConvert( SCM scm )
    {
        return scm instanceof ExtractResourceSCM;
    }
}
