package com.codebarrel.iconselect.service;

import com.atlassian.jira.issue.customfields.option.Option;
import com.codebarrel.iconselect.api.IconOptionsService;

public class IconOptionUtil {
    private IconOptionsService iconOptionsService;

    public IconOptionUtil(IconOptionsService iconOptionsService) {
        this.iconOptionsService = iconOptionsService;
    }

    public Long getAvatarId(Option option) {
        return Long.valueOf((option == null) ? 0L : this.iconOptionsService.getAvatarIdForOption(option).longValue());
    }
}
