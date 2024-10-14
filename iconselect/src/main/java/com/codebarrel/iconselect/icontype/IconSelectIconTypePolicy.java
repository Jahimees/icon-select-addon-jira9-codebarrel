package com.codebarrel.iconselect.icontype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.icon.IconOwningObjectId;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.plugin.icon.IconTypePolicy;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.codebarrel.iconselect.customfield.IconOptionsConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
@ExportAsService
public class IconSelectIconTypePolicy implements IconTypePolicy {

    private final GlobalPermissionManager globalPermissionManager;
    private final CustomFieldManager customFieldManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final PermissionManager permissionManager;

    private static final Logger log = LoggerFactory.getLogger(IconSelectIconTypePolicy.class);

    @Autowired
    public IconSelectIconTypePolicy(GlobalPermissionManager globalPermissionManager,
                                    CustomFieldManager customFieldManager,
                                    FieldConfigSchemeManager fieldConfigSchemeManager,
                                    PermissionManager permissionManager) {
        this.globalPermissionManager = globalPermissionManager;
        this.customFieldManager = customFieldManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean userCanView(@Nullable ApplicationUser applicationUser, @Nonnull Avatar avatar) {
        String owningObjectID = avatar.getOwner();
        boolean result = false;
        if (!avatar.getIconType().equals(IconSelectIconTypeDefinition.ICON_TYPE)) {
            result = false;
        } else if (avatar.isSystemAvatar()) {
            result = true;
        } else if (owningObjectID == null) {
            result = false;
        } else if (isUserAdmin(applicationUser)) {
            result = true;
        } else {
            try {
                long owningFieldId = Long.parseLong(owningObjectID);
            } catch (NumberFormatException var9) {
                log.error("Could not parse customfield ID " + owningObjectID);
                return false;
            }
            return true;
        }
        return result;
    }

    @Override
    public boolean userCanDelete(@Nullable ApplicationUser applicationUser, @Nonnull Avatar avatar) {
        return (avatar.getOwner() != null && isUserAdmin(applicationUser));
    }

    @Override
    public boolean userCanCreateFor(@Nullable ApplicationUser applicationUser, @Nonnull IconOwningObjectId iconOwningObjectId) {
        long owningFieldId;
        if (!isUserAdmin(applicationUser))
            return false;
        try {
            owningFieldId = Long.parseLong(iconOwningObjectId.getId());
        } catch (Exception var6) {
            log.error("Could not map " + iconOwningObjectId.getId() + " to a customfield id.");
            return false;
        }
        CustomField customField = this.customFieldManager.getCustomFieldObject(Long.valueOf(owningFieldId));
        if (customField == null)
            return false;
        List<FieldConfigItemType> configurationItemTypes = customField.getConfigurationItemTypes();
        return configurationItemTypes.stream().anyMatch(configItem -> configItem instanceof IconOptionsConfigItem);
    }

    private boolean isUserAdmin(ApplicationUser user) {
        return (user != null && (this.globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user) || this.globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user)));
    }
}
