package io.jenkins.plugins.todeclarative.converter.jobproperty;

import com.coravy.hudson.plugins.github.GithubProjectProperty;
import io.jenkins.plugins.todeclarative.converter.api.ConverterRequest;
import io.jenkins.plugins.todeclarative.converter.api.ConverterResult;
import io.jenkins.plugins.todeclarative.converter.api.SingleTypedConverter;
import org.jenkinsci.plugins.variant.OptionalExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OptionalExtension(requirePlugins = {"github"})
public class GithubJobPropertyConverter extends SingleTypedConverter<GithubProjectProperty> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubJobPropertyConverter.class.getName());

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {
        //            if(request.isCreateProject()) {
        //                converterResult.getJob().addProperty( jobProperty );
        //            }
        // nothing as we do not create project
        return true;
    }
}
