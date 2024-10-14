package com.codebarrel.iconselect.customfield;

import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.MultiSelectCustomFieldSearchRenderer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.codebarrel.iconselect.api.IconOptionsService;
import com.codebarrel.iconselect.service.IconOptionUtil;
import java.util.Map;
import webwork.action.Action;

public class IconMultiSelectSearchRender extends MultiSelectCustomFieldSearchRenderer {
    private final IconOptionsService iconOptionsService;

    public IconMultiSelectSearchRender(ClauseNames clauseNames,
                                       CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor,
                                       CustomField customField, CustomFieldValueProvider customFieldValueProvider,
                                       FieldVisibilityManager fieldVisibilityManager,
                                       JqlSelectOptionsUtil jqlSelectOptionsUtil, IconOptionsService iconOptionsService) {
        super(clauseNames, customFieldSearcherModuleDescriptor, customField, customFieldValueProvider,
                fieldVisibilityManager, jqlSelectOptionsUtil);
        this.iconOptionsService = iconOptionsService;
    }

    public String getEditHtml(SearchContext searchContext, FieldValuesHolder fieldValuesHolder,
                              Map<?, ?> displayParameters, Action action, Map<String, Object> velocityParams) {
        velocityParams.put("iconOptionUtil", new IconOptionUtil(this.iconOptionsService));
        return super.getEditHtml(searchContext, fieldValuesHolder, displayParameters, action, velocityParams);
    }

    public String getViewHtml(SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action, Map<String, Object> velocityParams) {
        velocityParams.put("iconOptionUtil", new IconOptionUtil(this.iconOptionsService));
        return super.getViewHtml(searchContext, fieldValuesHolder, displayParameters, action, velocityParams);
    }
}
