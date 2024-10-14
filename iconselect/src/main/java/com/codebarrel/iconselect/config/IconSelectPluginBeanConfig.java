package com.codebarrel.iconselect.config;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
        ModuleFactoryBean.class,
        PluginAccessorBean.class
})
public class IconSelectPluginBeanConfig {
    @Bean
    public ActiveObjects activeObjects() {
        return importOsgiService(ActiveObjects.class);
    }

    @Bean
    public OptionsManager optionsManager() {
        return importOsgiService(OptionsManager.class);
    }

    @Bean
    public PageBuilderService pageBuilderService() {
        return importOsgiService(PageBuilderService.class);
    }

    @Bean
    public JiraBaseUrls jiraBaseUrls() {
        return importOsgiService(JiraBaseUrls.class);
    }

    @Bean
    public SearchService searchService() {
        return importOsgiService(SearchService.class);
    }

    @Bean
    public ProjectFactory projectFactory() {
        return importOsgiService(ProjectFactory.class);
    }

    @Bean
    public SoyTemplateRenderer soyTemplateRenderer() {
        return importOsgiService(SoyTemplateRenderer.class);
    }

    @Bean
    public AvatarManager avatarManager() {
        return importOsgiService(AvatarManager.class);
    }

    @Bean
    public JiraPropertySetFactory jiraPropertySetFactory() {
        return importOsgiService(JiraPropertySetFactory.class);
    }

    @Bean
    public I18nHelper.BeanFactory beanFactory() {
        return importOsgiService(I18nHelper.BeanFactory.class);
    }

    @Bean
    public ConstantsManager constantsManager() {
        return importOsgiService(ConstantsManager.class);
    }

    @Bean
    public CustomFieldManager customFieldManager() {
        return importOsgiService(CustomFieldManager.class);
    }

    @Bean
    public CustomFieldValuePersister customFieldValuePersister() {
        return importOsgiService(CustomFieldValuePersister.class);
    }

    @Bean
    public FieldConfigSchemeManager fieldConfigSchemeManager() {
        return importOsgiService(FieldConfigSchemeManager.class);
    }

    @Bean
    public FieldConfigManager fieldConfigManager() {
        return importOsgiService(FieldConfigManager.class);
    }

    @Bean
    public FieldManager fieldManager() {
        return importOsgiService(FieldManager.class);
    }

    @Bean
    public FieldVisibilityManager fieldVisibilityManager() {
        return importOsgiService(FieldVisibilityManager.class);
    }

    @Bean
    public FeatureManager featureManager() {
        return importOsgiService(FeatureManager.class);
    }

    @Bean
    public GenericConfigManager genericConfigManager() {
        return importOsgiService(GenericConfigManager.class);
    }

    @Bean
    public GlobalPermissionManager globalPermissionManager() {
        return importOsgiService(GlobalPermissionManager.class);
    }

    @Bean
    public IssueTypeSchemeManager issueTypeSchemeManager() {
        return importOsgiService(IssueTypeSchemeManager.class);
    }

    @Bean
    public JiraAuthenticationContext jiraAuthenticationContext() {
        return importOsgiService(JiraAuthenticationContext.class);
    }

    @Bean
    public PermissionManager permissionManager() {
        return importOsgiService(PermissionManager.class);
    }
}

