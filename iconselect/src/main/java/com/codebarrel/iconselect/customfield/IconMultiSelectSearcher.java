package com.codebarrel.iconselect.customfield;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.MultiSelectCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.searchers.MultiSelectSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.JiraComponentFactory;
import com.atlassian.jira.util.JiraComponentLocator;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.codebarrel.iconselect.api.IconOptionsService;

import javax.inject.Inject;

@Scanned
public class IconMultiSelectSearcher extends MultiSelectSearcher {
    private volatile SearchRenderer searchRenderer;

    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final IconOptionsService iconOptionsService;

    @Inject
    public IconMultiSelectSearcher(FieldVisibilityManager fieldVisibilityManager,
                                   OptionsManager optionsManager,
                                   FieldConfigSchemeManager fieldConfigSchemeManager,
                                   IssueTypeSchemeManager issueTypeSchemeManager,
                                   ConstantsManager constantsManager,
                                   PermissionManager permissionManager,
                                   ProjectFactory projectFactory,
                                   IconOptionsService iconOptionsService) {
        super(new JiraComponentLocator(), JiraComponentFactory.getInstance());
        this.fieldVisibilityManager = fieldVisibilityManager;
        FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);
        this.jqlSelectOptionsUtil = new JqlSelectOptionsUtil(optionsManager, fieldConfigSchemeManager, util);
        this.iconOptionsService = iconOptionsService;
    }

    public void init(CustomField field) {
        super.init(field);
        this.searchRenderer = new IconMultiSelectSearchRender(field.getClauseNames(), getDescriptor(), field, (CustomFieldValueProvider) new MultiSelectCustomFieldValueProvider(), this.fieldVisibilityManager, this.jqlSelectOptionsUtil, this.iconOptionsService);
    }

    public SearchRenderer getSearchRenderer() {
        return this.searchRenderer;
    }
}
