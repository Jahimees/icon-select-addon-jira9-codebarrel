package com.codebarrel.iconselect.icontype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.plugin.icon.IconTypeDefinition;
import com.atlassian.jira.plugin.icon.IconTypeDefinitionFactory;
import com.atlassian.jira.plugin.icon.IconTypeDefinitionFactoryImpl;
import com.atlassian.jira.plugin.icon.SystemIconImageProvider;
import com.atlassian.plugin.PluginAccessor;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IconSelectIconTypeDataProvider implements SystemIconImageProvider {
    private final AvatarManager avatarManager;

    private final IconTypeDefinitionFactory iconTypeFactory;

    private static final Logger log = LoggerFactory.getLogger(IconSelectIconTypeDataProvider.class);

    public IconSelectIconTypeDataProvider(AvatarManager avatarManager, PluginAccessor pluginAccessor) {
        this.avatarManager = avatarManager;
        this.iconTypeFactory = new IconTypeDefinitionFactoryImpl(pluginAccessor);
    }

    @Nonnull
    @Override
    public InputStream getSystemIconInputStream(@Nonnull Avatar avatar, @Nonnull Avatar.Size size) throws IOException {
        String rawFileName = avatar.getFileName();
        String[] splitStrings = rawFileName.split("-", 2);
        String type = splitStrings[1];
        if (type.equals("iconselectlist")) {
            String fileName = splitStrings[0];
            return getPluginSystemIconInputStream(fileName);
        }
        Long avatarId = Long.valueOf(Long.parseLong(splitStrings[0]));
        Avatar delegate = this.avatarManager.getById(avatarId);
        IconTypeDefinition typeDefinition = getIconTypeDefinitionStrict(delegate.getIconType());
        return typeDefinition.getSystemIconImageProvider().getSystemIconInputStream(delegate, size);
    }

    @Nonnull
    public InputStream getPluginSystemIconInputStream(String fileName) throws IOException {
        String path = "/images/avatars/" + fileName;
        InputStream data = getClass().getResourceAsStream(path);
        if (data == null) {
            log.error("Plugin System Avatar not found at the following resource path: " + path);
            throw new IOException("File not found");
        }
        return data;
    }

    private IconTypeDefinition getIconTypeDefinitionStrict(IconType iconType) {
        IconTypeDefinition iconTypeDefinition = this.iconTypeFactory.getDefinition(iconType);
        if (iconTypeDefinition == null)
            throw new IllegalArgumentException("Unknown IconType '" + iconType + "'");
        return iconTypeDefinition;
    }
}

