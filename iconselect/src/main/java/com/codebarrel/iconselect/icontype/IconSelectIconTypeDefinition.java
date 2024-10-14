package com.codebarrel.iconselect.icontype;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.plugin.icon.IconTypeDefinition;
import com.atlassian.jira.plugin.icon.IconTypePolicy;
import com.atlassian.jira.plugin.icon.SystemIconImageProvider;
import com.atlassian.plugin.PluginAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class IconSelectIconTypeDefinition implements IconTypeDefinition {

    private final IconSelectIconTypePolicy iconSelectIconTypePolicy;
    private final IconSelectIconTypeDataProvider iconSelectIconTypeDataProvider;
    public static final String TYPE_NAME = "iconselectlist";
    public static final IconType ICON_TYPE = new IconType("iconselectlist");

    @Autowired
    public IconSelectIconTypeDefinition(IconSelectIconTypePolicy iconSelectIconTypePolicy,
                                        AvatarManager avatarManager,
                                        PluginAccessor pluginAccessor) {
        this.iconSelectIconTypePolicy = iconSelectIconTypePolicy;
        this.iconSelectIconTypeDataProvider = new IconSelectIconTypeDataProvider(avatarManager, pluginAccessor);
    }

    @Nonnull
    @Override
    public String getKey() {
        return TYPE_NAME;
    }

    @Nonnull
    @Override
    public IconTypePolicy getPolicy() {
        return this.iconSelectIconTypePolicy;
    }

    @Nonnull
    @Override
    public SystemIconImageProvider getSystemIconImageProvider() {
        return this.iconSelectIconTypeDataProvider;
    }
}
