package io.jenkins.plugins.todeclarative.converter.publisher;

import hudson.Extension;
import hudson.tasks.Mailer;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.ModelASTUtils;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import java.util.HashMap;
import java.util.Map;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBranch;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTBuildCondition;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTKey;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTNamedArgumentList;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStep;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTValue;

@Extension
public class MailerPublisherConverter extends SingleTypedConverter<Mailer> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        Mailer mailer = (Mailer) target;
        // FIXME must depends on Threshold
        // mailer.isNotifyEveryUnstableBuild()

        ModelASTBuildCondition buildCondition =
                ModelASTUtils.buildOrFindBuildCondition(result.getModelASTPipelineDef(), "always");

        ModelASTBranch branch = buildCondition.getBranch();
        if (branch == null) {
            branch = new ModelASTBranch(this);
            buildCondition.setBranch(branch);
        }
        // TODO maybe using Email Extension Plugin
        // mail bcc: 'bcc@foo.com', body: 'The body', cc: 'cc@foo.com', charset: 'Body Character Set',
        // from: 'from@foo.com', mimeType: 'Body MIME Type', replyTo: 'reply-to@foo.com', subject: 'The Subject', to:
        // 'foo@ffo.com'

        ModelASTStep mail = new ModelASTStep(this);
        mail.setName("mail");
        ModelASTUtils.addStep(buildCondition, mail);

        Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
        { // to
            ModelASTKey to = new ModelASTKey(this);
            to.setKey("to");
            args.put(to, ModelASTValue.fromConstant(mailer.recipients, this));
        }

        // TODO we need to define a body similar to the body from mailer plugin
        { // body
            ModelASTKey body = new ModelASTKey(this);
            body.setKey("body");
            //
            args.put(body, ModelASTValue.fromConstant("The body", this));
        }

        ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList(null);
        stepArgs.setArguments(args);
        mail.setArgs(stepArgs);

        return true;
    }
}
