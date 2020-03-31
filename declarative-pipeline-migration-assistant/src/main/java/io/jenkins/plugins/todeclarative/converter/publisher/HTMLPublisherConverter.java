package io.jenkins.plugins.todeclarative.converter.publisher;

import htmlpublisher.HtmlPublisher;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

import java.util.HashMap;
import java.util.Map;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = { "htmlpublisher" })
public class HTMLPublisherConverter extends SingleTypedConverter<HtmlPublisher>
{
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        HtmlPublisher htmlPublisher = (HtmlPublisher) target;
        // FIXME must depends on Threshold
        //buildTrigger.getThreshold()
        ModelASTBuildCondition buildCondition =
            ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
        htmlPublisher.getReportTargets().stream().forEach( reportTarget -> {
            // publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true,
            // reportDir: '/JTReport/html', reportFiles: 'report.html',
            // reportName: ' Report', reportTitles: ''])

            ModelASTStep publishHTML = new ModelASTStep( this );
            publishHTML.setName( "publishHTML" );

            ModelASTUtils.addStep(buildCondition, publishHTML);

            Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
            { // allowMissing
                ModelASTKey allowMissing = new ModelASTKey( this );
                allowMissing.setKey( "allowMissing" );
                args.put( allowMissing, ModelASTValue.fromConstant( reportTarget.getAllowMissing(), this ) );
            }

            { // alwaysLinkToLastBuild
                ModelASTKey alwaysLinkToLastBuild = new ModelASTKey( this );
                alwaysLinkToLastBuild.setKey( "alwaysLinkToLastBuild" );
                args.put( alwaysLinkToLastBuild, ModelASTValue.fromConstant( reportTarget.getAlwaysLinkToLastBuild(), this ) );
            }

            { // keepAll
                ModelASTKey keepAll = new ModelASTKey( this );
                keepAll.setKey( "keepAll" );
                args.put( keepAll, ModelASTValue.fromConstant( reportTarget.getKeepAll(), this ) );
            }

            { // reportDir
                ModelASTKey reportDir = new ModelASTKey( this );
                reportDir.setKey( "reportDir" );
                args.put( reportDir, ModelASTValue.fromConstant( reportTarget.getReportDir(), this ) );
            }

            { // reportFiles
                ModelASTKey reportFiles = new ModelASTKey( this );
                reportFiles.setKey( "reportFiles" );
                args.put( reportFiles, ModelASTValue.fromConstant( reportTarget.getReportFiles(), this ) );
            }

            { // reportName
                ModelASTKey reportName = new ModelASTKey( this );
                reportName.setKey( "reportName" );
                args.put( reportName, ModelASTValue.fromConstant( reportTarget.getReportName(), this ) );
            }

            { // reportTitles
                ModelASTKey reportTitles = new ModelASTKey( this );
                reportTitles.setKey( "reportTitles" );
                args.put( reportTitles, ModelASTValue.fromConstant( reportTarget.getReportTitles(), this ) );
            }

            ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList( null);
            stepArgs.setArguments( args );
            publishHTML.setArgs( stepArgs );


        } );
        return true;
    }
}
