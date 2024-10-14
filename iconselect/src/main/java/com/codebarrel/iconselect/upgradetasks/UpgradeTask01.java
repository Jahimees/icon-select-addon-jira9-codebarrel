package com.codebarrel.iconselect.upgradetasks;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.icon.IconType;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.google.common.collect.Lists;
import com.codebarrel.iconselect.icontype.IconSelectIconTypeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
@ExportAsService({PluginUpgradeTask.class})
public class UpgradeTask01 implements PluginUpgradeTask {
    private final AvatarManager avatarManager;

    @Autowired
    public UpgradeTask01(AvatarManager avatarManager) {
        this.avatarManager = avatarManager;
    }

    public int getBuildNumber() {
        return 1;
    }

    public String getShortDescription() {
        return "Initialise all avatars";
    }

    public String getPluginKey() {
        return "com.codebarrel.jira.iconselectlist";
    }

    public Collection<Message> doUpgrade() throws Exception {
        List<Avatar> allSystemAvatars = this.avatarManager.getAllSystemAvatars(IconSelectIconTypeDefinition.ICON_TYPE);
        if (allSystemAvatars.isEmpty())
            createAvatars(this.avatarManager);
        return null;
    }

    private void createAvatars(AvatarManager avatarManager) {
        avatarManager.create(new TempAvatar("defaulticon.svg-iconselectlist", "image/svg+xml"));
        loadLocalAvatars(avatarManager);
        copyAvatarsForType(IconType.PROJECT_ICON_TYPE);
        copyAvatarsForType(IconType.ISSUE_TYPE_ICON_TYPE);
        copyAvatarsForType(IconType.USER_ICON_TYPE);
    }

    private void copyAvatarsForType(IconType type) {
        List<Avatar> systemAvatars = this.avatarManager.getAllSystemAvatars(type);
        systemAvatars.forEach(avatar -> this.avatarManager.create(fromAvatar(avatar)));
    }

    private void loadLocalAvatars(AvatarManager avatarManager) {
        List<String> priorities = Lists.newArrayList("blocker.svg", "critical.svg", "major.svg", "highest.svg", "high.svg", "medium.svg", "low.svg", "lowest.svg", "minor.svg", "trivial.svg");
        priorities.forEach(name -> avatarManager.create(new TempAvatar(name + "-" + "iconselectlist", "image/svg+xml")));
        for (int i = 1; i < 88; i++)
            avatarManager.create(new TempAvatar((1000 + i) + ".svg-" + "iconselectlist", "image/svg+xml"));
    }

    private TempAvatar fromAvatar(Avatar avatar) {
        return new TempAvatar(avatar.getId() + "-" + avatar.getIconType().getKey(), avatar.getContentType());
    }

    private class TempAvatar implements Avatar {
        private String fileName;

        private String contentType;

        TempAvatar(String fileName, String contentType) {
            this.fileName = fileName;
            this.contentType = contentType;
        }

        @Nonnull
        public Avatar.Type getAvatarType() {
            return null;
        }

        @Nonnull
        public IconType getIconType() {
            return IconSelectIconTypeDefinition.ICON_TYPE;
        }

        @Nonnull
        public String getFileName() {
            return this.fileName;
        }

        @Nonnull
        public String getContentType() {
            return this.contentType;
        }

        public Long getId() {
            return null;
        }

        public String getOwner() {
            return null;
        }

        public boolean isSystemAvatar() {
            return true;
        }
    }
}

