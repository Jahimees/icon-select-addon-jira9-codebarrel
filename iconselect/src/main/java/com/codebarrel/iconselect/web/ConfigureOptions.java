package com.codebarrel.iconselect.web;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.request.RequestMethod;
import com.atlassian.jira.security.request.SupportedMethods;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.codebarrel.iconselect.icontype.IconSelectIconTypeDefinition;
import com.codebarrel.iconselect.upgradetasks.UpgradeTask01;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Scanned
@SupportedMethods(RequestMethod.GET)
public class ConfigureOptions extends JiraWebActionSupport {

    private final JiraAuthenticationContext authenticationContext;
    private final PageBuilderService pageBuilderService;
    private final AvatarManager avatarManager;
    private final CustomFieldManager customFieldManager;
    private final FieldConfigManager fieldConfigManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final UpgradeTask01 upgradeTask01;
    private static final Logger log = LoggerFactory.getLogger(ConfigureOptions.class);
    private Long fieldConfigId = null;
    private Long customFieldId = null;

    @Inject
    public ConfigureOptions(JiraAuthenticationContext authenticationContext,
                            PageBuilderService pageBuilderService,
                            AvatarManager avatarManager,
                            CustomFieldManager customFieldManager,
                            FieldConfigManager fieldConfigManager,
                            GlobalPermissionManager globalPermissionManager,
                            UpgradeTask01 upgradeTask01) throws Exception {
        this.authenticationContext = authenticationContext;
        this.pageBuilderService = pageBuilderService;
        this.avatarManager = avatarManager;
        this.customFieldManager = customFieldManager;
        this.fieldConfigManager = fieldConfigManager;
        this.globalPermissionManager = globalPermissionManager;
        this.upgradeTask01 = upgradeTask01;

        //FIXME. Workaround. Auto upgrade task doesn't work.
        this.upgradeTask01.doUpgrade();
    }

    public String execute() throws Exception {
        if (!isUserAdmin(this.authenticationContext.getLoggedInUser()))
            return "permissionviolation";
        Avatar defaultAvatar = this.avatarManager.getDefaultAvatar(IconSelectIconTypeDefinition.ICON_TYPE);
        this.pageBuilderService.assembler().resources()
            .requireWebResource("com.codebarrel.jira.iconselectlist:iconselectlist-config-resources")
            .requireWebResource("com.codebarrel.jira.iconselectlist:iconselectlist-restfulltable-templates");
        this.pageBuilderService.assembler().data()
            .requireData("com.codebarrel.iconselect:fieldConfig", this.fieldConfigId)
            .requireData("com.codebarrel.iconselect:defaultAvatar", defaultAvatar.getId())
            .requireData("com.codebarrel.iconselect:customField", this.customFieldId);
        return super.execute();
    }

    private boolean isUserAdmin(ApplicationUser user) {
        return (user != null && (this.globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user) || this.globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user)));
    }

    public Long getFieldConfigId() {
        return this.fieldConfigId;
    }

    public void setFieldConfigId(Long fieldConfigId) {
        this.fieldConfigId = fieldConfigId;
    }

    public Long getCustomFieldId() {
        return this.customFieldId;
    }

    public void setCustomFieldId(Long customFieldId) {
        this.customFieldId = customFieldId;
    }

    @ActionViewData
    public String getCustomFieldName() {
        return getCustomField().getFieldName();
    }

    @ActionViewData
    public String getFieldConfigName() {
        return this.fieldConfigManager.getFieldConfig(this.fieldConfigId).getName();
    }

    public CustomField getCustomField() {
        return this.customFieldManager.getCustomFieldObject(this.customFieldId);
    }

    @ActionViewData
    public String getReturnUrl() {
        return super.getReturnUrl();
    }
}
