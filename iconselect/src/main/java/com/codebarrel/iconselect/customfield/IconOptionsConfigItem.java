package com.codebarrel.iconselect.customfield;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.codebarrel.iconselect.api.IconOptionBean;
import com.codebarrel.iconselect.api.IconOptionsService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IconOptionsConfigItem extends SettableOptionsConfigItem {

    private final OptionsManager optionsManager;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final IconOptionsService iconOptionsService;
    private final JiraBaseUrls jiraBaseUrls;

    public IconOptionsConfigItem(CustomFieldType customFieldType, OptionsManager optionsManager,
                                 SoyTemplateRenderer soyTemplateRenderer, IconOptionsService iconOptionsService,
                                 JiraBaseUrls jiraBaseUrls) {
        super(customFieldType, optionsManager);
        this.optionsManager = optionsManager;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.iconOptionsService = iconOptionsService;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem) {
        Options options = this.optionsManager.getOptions(fieldConfig);
        Map<String, Object> data = new HashMap<>();
        data.put("options", getBeans(options));
        data.put("contextPath", ExecutingHttpRequest.get().getContextPath());
        return this.soyTemplateRenderer.render("com.codebarrel.jira.iconselectlist:iconselectlist-config-page-templates", "com.codebarrel.iconselect.config.page.viewOptions", data);
    }

    private List<IconOptionBean> getBeans(List<Option> options) {
        return options.stream().map(option -> IconOptionBean.fromOption(option, this.iconOptionsService.getAvatarIdForOption(option), this.jiraBaseUrls)).collect(Collectors.toList());
    }

    public String getBaseEditUrl() {
        return "ConfigureIconSelectListOptions.jspa";
    }
}
