package com.codebarrel.iconselect.service;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.opensymphony.module.propertyset.PropertySet;
import com.codebarrel.iconselect.api.IconOptionBean;
import com.codebarrel.iconselect.api.IconOptionsService;
import com.codebarrel.iconselect.customfield.IconOptionsConfigItem;
import com.codebarrel.iconselect.icontype.IconSelectIconTypeDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ExportAsService({IconOptionsService.class})
public class IconOptionsServiceImpl implements IconOptionsService {
    public static final String AVATAR_MAP = "com.code.barrel.iconselect.avatar.map";

    private final OptionsManager optionsManager;
    private final FieldConfigManager fieldConfigManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final AvatarManager avatarManager;
    private final PermissionManager permissionManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraBaseUrls jiraBaseUrls;
    private final PropertySet avatarMap;

    @Autowired
    public IconOptionsServiceImpl(OptionsManager optionsManager,
                                  FieldConfigManager fieldConfigManager,
                                  FieldConfigSchemeManager fieldConfigSchemeManager,
                                  AvatarManager avatarManager,
                                  PermissionManager permissionManager,
                                  GlobalPermissionManager globalPermissionManager,
                                  I18nHelper.BeanFactory i18nFactory,
                                  JiraPropertySetFactory jiraPropertySetFactory,
                                  JiraBaseUrls jiraBaseUrls) {
        this.optionsManager = optionsManager;
        this.fieldConfigManager = fieldConfigManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.avatarManager = avatarManager;
        this.permissionManager = permissionManager;
        this.globalPermissionManager = globalPermissionManager;
        this.i18nFactory = i18nFactory;
        this.jiraBaseUrls = jiraBaseUrls;
        this.avatarMap = jiraPropertySetFactory.buildCachingDefaultPropertySet(AVATAR_MAP);
    }

    public ServiceOutcome<IconOptionBean> getIconOptionById(ApplicationUser user, Long id) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        Option option = this.optionsManager.findByOptionId(id);
        if (option == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found", id.toString()), ErrorCollection.Reason.NOT_FOUND);
        return getIconOptionBeanServiceOutcome(user, i18n, option);
    }

    public ServiceOutcome<IconOptionBean> getIconOptionByFieldConfigAndId(ApplicationUser user, Long fieldConfigId, Long id) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        Options options = this.optionsManager.getOptions(fieldConfig);
        Optional<Option> matchedOption = options.stream().filter(option -> option.getOptionId().equals(id)).findFirst();
        if (!matchedOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", id.toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        Option option = matchedOption.get();
        return getIconOptionBeanServiceOutcome(user, i18n, option);
    }

    public ServiceOutcome<List<IconOptionBean>> getAllIconOptionForFieldConfig(ApplicationUser user, Long fieldConfigId) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user) && !canSeeFieldConfig(user, fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        Options options = this.optionsManager.getOptions(fieldConfig);
        return ServiceOutcomeImpl.ok(getBeans(options));
    }

    public ServiceOutcome<IconOptionBean> createIconOption(ApplicationUser user, Long fieldConfigId, IconOptionBean bean) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        if (StringUtils.isBlank(bean.getLabel()))
            simpleErrorCollection.addError("label", i18n.getText("icon.option.label.not.null"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (bean.getAvatarId() == null)
            simpleErrorCollection.addError("avatarId", i18n.getText("icon.option.avatar.not.null"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (simpleErrorCollection.hasAnyErrors())
            return ServiceOutcomeImpl.from(simpleErrorCollection);
        Options options = this.optionsManager.getOptions(fieldConfig);
        if (options.stream().anyMatch(option -> option.getValue().equals(bean.getLabel())))
            simpleErrorCollection.addError("label", i18n.getText("icon.option.label.already.exists", bean.getLabel()), ErrorCollection.Reason.VALIDATION_FAILED);
        Avatar avatar = this.avatarManager.getById(bean.getAvatarId());
        if (avatar == null) {
            simpleErrorCollection.addError("avatarId", i18n.getText("icon.option.avatar.not.exists", bean.getAvatarId().toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        } else if (!IconSelectIconTypeDefinition.ICON_TYPE.equals(avatar.getIconType())) {
            simpleErrorCollection.addError("avatarId", i18n.getText("icon.option.wrong.icon.type"), ErrorCollection.Reason.VALIDATION_FAILED);
        }
        if (simpleErrorCollection.hasAnyErrors())
            return ServiceOutcomeImpl.from(simpleErrorCollection);
        Option option = this.optionsManager.createOption(fieldConfig, null, Long.valueOf(options.size()), bean.getLabel());
        setAvatarIdForOption(option, avatar);
        return ServiceOutcomeImpl.ok(IconOptionBean.fromOption(option, avatar.getId(), this.jiraBaseUrls));
    }

    public ServiceOutcome<IconOptionBean> updateIconOption(ApplicationUser user, Long fieldConfigId, IconOptionBean bean) {
        Long avatarId;
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        Options options = this.optionsManager.getOptions(fieldConfig);
        Optional<Option> matchedOption = options.stream().filter(option -> option.getOptionId().equals(bean.getId())).findFirst();
        if (!matchedOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", bean.getId().toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        if (simpleErrorCollection.hasAnyErrors())
            return ServiceOutcomeImpl.from((ErrorCollection) simpleErrorCollection);
        if (options.stream().anyMatch(option -> option.getValue().equals(bean.getLabel())))
            simpleErrorCollection.addError("label", i18n.getText("icon.option.label.already.exists", bean.getLabel()), ErrorCollection.Reason.VALIDATION_FAILED);
        Avatar avatar = null;
        if (bean.getAvatarId() != null && bean.getAvatarId().intValue() != 0) {
            avatar = this.avatarManager.getById(bean.getAvatarId());
            if (avatar == null) {
                simpleErrorCollection.addError("avatarId", i18n.getText("icon.option.avatar.not.exists", bean.getAvatarId().toString()), ErrorCollection.Reason.VALIDATION_FAILED);
            } else if (!IconSelectIconTypeDefinition.ICON_TYPE.equals(avatar.getIconType())) {
                simpleErrorCollection.addError("avatarId", i18n.getText("icon.option.wrong.icon.type"), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        if (simpleErrorCollection.hasAnyErrors())
            return ServiceOutcomeImpl.from(simpleErrorCollection);
        Option option = matchedOption.get();
        if (!StringUtils.isBlank(bean.getLabel())) {
            option.setValue(bean.getLabel());
            this.optionsManager.updateOptions(Collections.singletonList(option));
        }
        if (bean.getAvatarId() != null && bean.getAvatarId().intValue() != 0) {
            setAvatarIdForOption(option, avatar);
            avatarId = avatar.getId();
        } else {
            avatarId = getAvatarIdForOption(option);
        }
        return ServiceOutcomeImpl.ok(IconOptionBean.fromOption(option, avatarId, this.jiraBaseUrls));
    }

    public ServiceResult deleteIconOption(ApplicationUser user, Long fieldConfigId, Long id) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        Options options = this.optionsManager.getOptions(fieldConfig);
        Optional<Option> matchedOption = options.stream().filter(option -> option.getOptionId().equals(id)).findFirst();
        if (!matchedOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", id.toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        this.optionsManager.deleteOptionAndChildren(matchedOption.get());
        return new ServiceResultImpl(simpleErrorCollection);
    }

    public ServiceResult disableIconOption(ApplicationUser user, Long fieldConfigId, Long id) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        Options options = this.optionsManager.getOptions(fieldConfig);
        Optional<Option> matchedOption = options.stream().filter(option -> option.getOptionId().equals(id)).findFirst();
        if (!matchedOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", id.toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        this.optionsManager.disableOption(matchedOption.get());
        return new ServiceResultImpl((ErrorCollection) simpleErrorCollection);
    }

    public ServiceResult enableIconOption(ApplicationUser user, Long fieldConfigId, Long id) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        Options options = this.optionsManager.getOptions(fieldConfig);
        Optional<Option> matchedOption = options.stream().filter(option -> option.getOptionId().equals(id)).findFirst();
        if (!matchedOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", id.toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        this.optionsManager.enableOption(matchedOption.get());
        return new ServiceResultImpl(simpleErrorCollection);
    }

    public ServiceOutcome<IconOptionBean> moveToPosition(ApplicationUser user, Long fieldConfigId, Long id, int position) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        Options options = this.optionsManager.getOptions(fieldConfig);
        Optional<Option> matchedOption = options.stream().filter(option -> option.getOptionId().equals(id)).findFirst();
        if (!matchedOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", id.toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        if (position != 0)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.only.move.to.first"), ErrorCollection.Reason.VALIDATION_FAILED);
        Option option = matchedOption.get();
        options.moveToStartSequence(option);
        return ServiceOutcomeImpl.ok(IconOptionBean.fromOption(option, getAvatarIdForOption(option), this.jiraBaseUrls));
    }

    public ServiceOutcome<IconOptionBean> moveToAfter(ApplicationUser user, Long fieldConfigId, Long id, Long afterId) {
        I18nHelper i18n = this.i18nFactory.getInstance(user);
        FieldConfig fieldConfig = getFieldConfig(fieldConfigId);
        if (fieldConfig == null)
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.field.config.not.found", fieldConfigId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
        if (isConfigForIconSelect(fieldConfig))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        Options options = this.optionsManager.getOptions(fieldConfig);
        Optional<Option> matchedOption = options.stream().filter(option -> option.getOptionId().equals(id)).findFirst();
        if (!matchedOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", id.toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        Optional<Option> relativeOption = options.stream().filter(option -> option.getOptionId().equals(afterId)).findFirst();
        if (!relativeOption.isPresent())
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.not.found.in.field.config", afterId.toString(), fieldConfigId.toString()), ErrorCollection.Reason.NOT_FOUND);
        Option option = matchedOption.get();
        CollectionReorderer.moveToPositionAfter((List) options, option, relativeOption.get());
        renumberOptions((List<Option>) options);
        this.optionsManager.updateOptions((Collection) options);
        return ServiceOutcomeImpl.ok(IconOptionBean.fromOption(option, getAvatarIdForOption(option), this.jiraBaseUrls));
    }

    public Long getAvatarIdForOption(Option option) {
        return Long.valueOf(this.avatarMap.getLong(option.getOptionId().toString()));
    }

    private void setAvatarIdForOption(Option option, Avatar avatar) {
        this.avatarMap.setLong(option.getOptionId().toString(), avatar.getId().longValue());
    }

    private void renumberOptions(List<Option> options) {
        if (options != null) {
            long pos = 0L;
            for (Iterator<Option> it = options.iterator(); it.hasNext(); pos++) {
                Option option = it.next();
                option.setSequence(Long.valueOf(pos));
            }
        }
    }

    private FieldConfig getFieldConfig(Long id) {
        if (id == null)
            return null;
        try {
            return this.fieldConfigManager.getFieldConfig(id);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private boolean isOptionForIconSelect(Option option) {
        FieldConfig fieldConfig = option.getRelatedCustomField();
        return isConfigForIconSelect(fieldConfig);
    }

    private boolean isConfigForIconSelect(FieldConfig fieldConfig) {
        return (fieldConfig != null && fieldConfig.getConfigItems().stream().anyMatch(configItem -> configItem instanceof IconOptionsConfigItem));
    }

    private boolean isUserAdmin(ApplicationUser user) {
        return (user != null && (this.globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user) || this.globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user)));
    }

    private boolean canSeeOption(ApplicationUser user, Option option) {
        FieldConfig fieldConfig = option.getRelatedCustomField();
        return (fieldConfig != null && canSeeFieldConfig(user, fieldConfig));
    }

    private List<IconOptionBean> getBeans(List<Option> options) {
        return options.stream().map(option -> IconOptionBean.fromOption(option, getAvatarIdForOption(option), this.jiraBaseUrls)).collect(Collectors.toList());
    }

    private boolean canSeeFieldConfig(ApplicationUser user, FieldConfig config) {
        FieldConfigScheme fieldConfigScheme = this.fieldConfigSchemeManager.getConfigSchemeForFieldConfig(config);
        if (fieldConfigScheme == null)
            return false;
        List<Project> projects = fieldConfigScheme.getAssociatedProjectObjects();
        return projects.stream().anyMatch(project -> (this.permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, project, user) || this.permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, user)));
    }

    private ServiceOutcome<IconOptionBean> getIconOptionBeanServiceOutcome(ApplicationUser user, I18nHelper i18n, Option option) {
        if (isOptionForIconSelect(option))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.customfield.not.recognized"), ErrorCollection.Reason.VALIDATION_FAILED);
        if (!isUserAdmin(user) && !canSeeOption(user, option))
            return ServiceOutcomeImpl.error(i18n.getText("icon.option.cant.see.permission"), ErrorCollection.Reason.FORBIDDEN);
        return ServiceOutcomeImpl.ok(IconOptionBean.fromOption(option, getAvatarIdForOption(option), this.jiraBaseUrls));
    }
}
