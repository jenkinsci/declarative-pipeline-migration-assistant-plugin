package io.jenkins.plugins.todeclarative.converter.scm;

import hudson.scm.SubversionSCM;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTSingleArgument;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStage;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = {"subversion"})
public class SubversionScmConverter extends SingleTypedConverter<SubversionSCM> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {

        SubversionSCM subversionSCM = (SubversionSCM) target;
        SubversionSCM.ModuleLocation[] locations = subversionSCM.getLocations();
        if (locations == null || locations.length < 1) {
            return true;
        }
        ModelASTStage stage = new ModelASTStage(this);
        List<ModelASTStep> steps = new ArrayList<>();
        stage.setName("Checkout Scm");
        //        checkout([$class: 'SubversionSCM',
        //                additionalCredentials: [],
        //                excludedCommitMessages: '',
        //                excludedRegions: '',
        //                excludedRevprop: '',
        //                excludedUsers: '',
        //                filterChangelog: false,
        //                ignoreDirPropChanges: false,
        //                includedRegions: '',
        //                locations: [[credentialsId: 'someid',
        //                depthOption: 'infinity',
        //                ignoreExternalsOption:
        //                local: 'cable_branch',
        //                remote: "url"]],
        //        workspaceUpdater: [$class: 'UpdateUpdater']])

        ModelASTStep checkout = new ModelASTStep(null);
        checkout.setName("checkout");
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument(null);
        checkout.setArgs(singleArgument);
        // TODO this is not really comvenient but the subversion plugin doesn't have better option...
        StringBuilder gstring = new StringBuilder("[$class: 'SubversionSCM'");

        if (StringUtils.isNotEmpty(subversionSCM.getExcludedCommitMessages())) {
            gstring.append(",excludedCommitMessages:'")
                    .append(subversionSCM.getExcludedCommitMessages())
                    .append("'");
        }
        if (StringUtils.isNotEmpty(subversionSCM.getExcludedRegions())) {
            gstring.append(",excludedRegions:'")
                    .append(subversionSCM.getExcludedRegions())
                    .append("'");
        }
        if (StringUtils.isNotEmpty(subversionSCM.getExcludedRevprop())) {
            gstring.append(",excludedRevprop:'")
                    .append(subversionSCM.getExcludedRevprop())
                    .append("'");
        }
        if (StringUtils.isNotEmpty(subversionSCM.getExcludedUsers())) {
            gstring.append(",excludedUsers:'")
                    .append(subversionSCM.getExcludedUsers())
                    .append("'");
        }
        gstring.append(",filterChangelog:").append(subversionSCM.isFilterChangelog());
        gstring.append(",ignoreDirPropChanges:").append(subversionSCM.isIgnoreDirPropChanges());
        if (StringUtils.isNotEmpty(subversionSCM.getIncludedRegions())) {
            gstring.append(",includedRegions:'")
                    .append(subversionSCM.getIncludedRegions())
                    .append("'");
        }
        if (subversionSCM.getWorkspaceUpdater() != null) {
            // workspaceUpdater: [$class: 'UpdateUpdater']
            gstring.append(",[$class:'")
                    .append(subversionSCM.getWorkspaceUpdater().getClass().getSimpleName())
                    .append("']");
        }
        gstring.append(",locations:[");
        for (SubversionSCM.ModuleLocation moduleLocation : locations) {

            //            locations: [[credentialsId: '34761a89-1402-47d7-96e2-aec22ffdc50b',
            //                    depthOption: 'infinity',
            //                    ignoreExternalsOption: true,
            //                    local: 'cable_branch',
            //                    remote: "https://trac.nci.org.au/svn/cable/branches/$SVN_BRANCH"]]
            gstring.append("[");
            boolean empty = true;
            if (StringUtils.isNotEmpty(moduleLocation.credentialsId)) {
                gstring.append("credentialsId:'")
                        .append(moduleLocation.credentialsId)
                        .append("'");
                empty = false;
            }
            if (StringUtils.isNotEmpty(moduleLocation.depthOption)) {
                if (!empty) {
                    gstring.append(',');
                }
                gstring.append("depthOption: '")
                        .append(moduleLocation.depthOption)
                        .append("'");

                empty = false;
            }
            if (StringUtils.isNotEmpty(moduleLocation.getLocalDir())) {
                if (!empty) {
                    gstring.append(',');
                }
                gstring.append("local: '").append(moduleLocation.getLocalDir()).append("'");
            }
            if (StringUtils.isNotEmpty(moduleLocation.getURL())) {
                if (!empty) {
                    gstring.append(',');
                }
                gstring.append("remote: '").append(moduleLocation.getURL()).append("'");
            }
            gstring.append("]");
        }
        gstring.append("]");

        gstring.append("]");
        singleArgument.setValue(ModelASTValue.fromGString(gstring.toString(), this));
        steps.add(checkout);
        ModelASTBranch branch = new ModelASTBranch(this);
        branch.setSteps(steps);
        stage.setBranches(Arrays.asList(branch));
        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);
        return true;
    }
}
