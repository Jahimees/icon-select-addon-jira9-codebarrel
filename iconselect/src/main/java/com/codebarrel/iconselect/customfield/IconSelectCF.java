package com.codebarrel.iconselect.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
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
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.collect.Lists;
import com.codebarrel.iconselect.api.IconOptionBean;
import com.codebarrel.iconselect.api.IconOptionsService;
import com.codebarrel.iconselect.service.IconOptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Scanned
@Named
public class IconSelectCF extends SelectCFType {
    private static final Logger log = LoggerFactory.getLogger(IconSelectCF.class);

    private final OptionsManager optionsManager;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PageBuilderService pageBuilderService;
    private final IconOptionsService iconOptionsService;
    private final JiraBaseUrls jiraBaseUrls;

    @Inject
    public IconSelectCF(OptionsManager optionsManager,
                        CustomFieldValuePersister valuePersister,
                        GenericConfigManager genericConfigManager,
                        JiraBaseUrls jiraBaseUrls,
                        SoyTemplateRenderer soyTemplateRenderer,
                        PageBuilderService pageBuilderService,
                        IconOptionsService iconOptionsService) {
        super(valuePersister, optionsManager, genericConfigManager, jiraBaseUrls);
        this.jiraBaseUrls = jiraBaseUrls;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.optionsManager = optionsManager;
        this.pageBuilderService = pageBuilderService;
        this.iconOptionsService = iconOptionsService;
    }

    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        return Lists.newArrayList(new DefaultValueConfigItem(),
                new IconOptionsConfigItem(this, this.optionsManager, this.soyTemplateRenderer,
                        this.iconOptionsService, this.jiraBaseUrls));
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
        List<IconOptionBean> optionBeans = options.stream().map(option -> IconOptionBean.fromOption(option, this.iconOptionsService.getAvatarIdForOption(option), this.jiraBaseUrls)).collect(Collectors.toList());
        return new FieldTypeInfo(optionBeans, this.jiraBaseUrls.baseUrl() + "/rest/iconselectoptions/1.0/option/context/" + config.getId());
    }

    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem) {
        Option option = getValueFromIssue(field, issue);
        if (option == null)
            return new FieldJsonRepresentation(new JsonData(null));
        IconOptionBean optionBean = IconOptionBean.fromOption(option, this.iconOptionsService.getAvatarIdForOption(option), this.jiraBaseUrls);
        return new FieldJsonRepresentation(new JsonData(optionBean));
    }

    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field) {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        Option option = getDefaultValue(config);
        if (option == null)
            return null;
        IconOptionBean optionBean = IconOptionBean.fromOption(option, this.iconOptionsService.getAvatarIdForOption(option), this.jiraBaseUrls);
        return new JsonData(optionBean);
    }
}

