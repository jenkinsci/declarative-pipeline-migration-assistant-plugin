package io.jenkins.plugins.todeclarative.converter.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.Functions;
import hudson.model.Describable;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Publisher;
import java.util.Collection;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTStages;

public abstract class SingleTypedConverter<T> extends BaseConverter {
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    protected SingleTypedConverter() {
        type = Functions.getTypeParameter(getClass(), SingleTypedConverter.class, 0);
    }

    @Override
    public boolean canConvert(@NonNull Object object) {
        return type.isInstance(object);
    }

    protected <T> void convertTypeList(
            ConverterRequest request,
            ConverterResult result,
            ExtensionList<BaseConverter> converterList,
            Collection<T> typeList)
            throws ConverterException {
        ModelASTStages stages = result.getModelASTPipelineDef().getStages();
        if (stages == null) {
            stages = new ModelASTStages(this);
            result.getModelASTPipelineDef().setStages(stages);
        }
        for (T type : typeList) {
            boolean matched = false;
            for (BaseConverter converter : converterList) {
                if (converter.canConvert(type)) {
                    matched = true;
                    if (converter.convert(request, result, type)) {
                        result.addConvertedType(type.getClass());
                    } else {
                        // TODO: log addition information for failed conversion?
                    }
                    if (type instanceof Maven) {
                        // Maven is a special one and we can apply only one converter so we pick the first one
                        break;
                    }
                }
            }
            if (!matched) {
                result.addWarning(new Warning(
                        "Converter not found '" + getDisplayName((Describable) type) + "'", type.getClass()));
                SingleTypedConverter noConverter = null;
                if (type instanceof Builder) {
                    noConverter = Jenkins.get()
                            .getExtensionList(NoBuilderConverter.class)
                            .get(0);
                } else if (type instanceof Publisher) {
                    noConverter = Jenkins.get()
                            .getExtensionList(NoPublisherConverter.class)
                            .get(0);
                }
                if (noConverter != null) {
                    noConverter.convert(request, result, type);
                }
            }
        }
    }

    /**
     *
     * @param describable the {@link Describable} instance to find displayName
     * @return the displayName or empty String
     */
    protected String getDisplayName(Describable describable) {
        try {
            return describable.getDescriptor() == null
                    ? ""
                    : describable.getDescriptor().getDisplayName();
        } catch (Throwable e) {
            return "";
        }
    }
}
