package com.codebarrel.iconselect.customfield;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.codebarrel.iconselect.api.IconOptionBean;
import com.codebarrel.iconselect.api.IconOptionsService;
import com.codebarrel.iconselect.service.IconOptionUtil;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Scanned
@Named
public class IconMultiSelectCF extends MultiSelectCFType {
    private static final Logger log = LoggerFactory.getLogger(IconMultiSelectCF.class);

    private final OptionsManager optionsManager;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PageBuilderService pageBuilderService;
    private final IconOptionsService iconOptionsService;
    private final JiraBaseUrls jiraBaseUrls;

    @Inject
    public IconMultiSelectCF(OptionsManager optionsManager,
                             CustomFieldValuePersister valuePersister,
                             GenericConfigManager genericConfigManager,
                             JiraBaseUrls jiraBaseUrls,
                             SearchService searchService,
                             FeatureManager featureManager,
                             SoyTemplateRenderer soyTemplateRenderer,
                             PageBuilderService pageBuilderService,
                             JiraAuthenticationContext jiraAuthenticationContext,
                             IconOptionsService iconOptionsService) {
        super(optionsManager, valuePersister, genericConfigManager, jiraBaseUrls, searchService, featureManager, jiraAuthenticationContext);
        this.optionsManager = optionsManager;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.pageBuilderService = pageBuilderService;
        this.iconOptionsService = iconOptionsService;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        return Lists.newArrayList(new DefaultValueConfigItem(), new IconOptionsConfigItem(this, this.optionsManager, this.soyTemplateRenderer, this.iconOptionsService, this.jiraBaseUrls));
    }

    @Nonnull
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> velocityParams = super.getVelocityParameters(issue, field, fieldLayoutItem);
        velocityParams.put("iconOptionUtil", new IconOptionUtil(this.iconOptionsService));
        this.pageBuilderService.assembler().resources().requireWebResource("com.codebarrel.jira.iconselectlist:iconselectlist-resources");
        return velocityParams;
    }

    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext) {
        FieldConfig config = ((CustomField) fieldTypeInfoContext.getOderableField()).getRelevantConfig(fieldTypeInfoContext.getIssueContext());
        Options options = this.optionsManager.getOptions(config);
        List<IconOptionBean> optionBeans = options.stream()
                .map(option -> IconOptionBean
                        .fromOption(option, this.iconOptionsService.getAvatarIdForOption(option), this.jiraBaseUrls))
                .collect(Collectors.toList());
        return new FieldTypeInfo(optionBeans, this.jiraBaseUrls.baseUrl() + "/rest/iconselectoptions/1.0/option/context/" + config.getId());
    }

    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem) {
        Collection<Option> options = getValueFromIssue(field, issue);
        if (options == null)
            return new FieldJsonRepresentation(new JsonData(null));
        List<IconOptionBean> optionBeans = options.stream().map(option -> IconOptionBean.fromOption(option, this.iconOptionsService.getAvatarIdForOption(option), this.jiraBaseUrls)).collect(Collectors.toList());
        return new FieldJsonRepresentation(new JsonData(optionBeans));
    }

    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field) {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        Collection<Option> options = getDefaultValue(config);
        if (options == null)
            return null;
        List<IconOptionBean> optionBeans = (List<IconOptionBean>) options.stream().map(option -> IconOptionBean.fromOption(option, this.iconOptionsService.getAvatarIdForOption(option), this.jiraBaseUrls)).collect(Collectors.toList());
        return new JsonData(optionBeans);
    }
}

