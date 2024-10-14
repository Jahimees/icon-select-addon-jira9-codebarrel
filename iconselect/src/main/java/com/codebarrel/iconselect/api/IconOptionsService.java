package com.codebarrel.iconselect.api;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.user.ApplicationUser;
import java.util.List;

public interface IconOptionsService {
    ServiceOutcome<IconOptionBean> getIconOptionById(ApplicationUser paramApplicationUser, Long paramLong);

    ServiceOutcome<IconOptionBean> getIconOptionByFieldConfigAndId(ApplicationUser paramApplicationUser, Long paramLong1, Long paramLong2);

    ServiceOutcome<List<IconOptionBean>> getAllIconOptionForFieldConfig(ApplicationUser paramApplicationUser, Long paramLong);

    ServiceOutcome<IconOptionBean> createIconOption(ApplicationUser paramApplicationUser, Long paramLong, IconOptionBean paramIconOptionBean);

    ServiceOutcome<IconOptionBean> updateIconOption(ApplicationUser paramApplicationUser, Long paramLong, IconOptionBean paramIconOptionBean);

    ServiceResult deleteIconOption(ApplicationUser paramApplicationUser, Long paramLong1, Long paramLong2);

    ServiceResult disableIconOption(ApplicationUser paramApplicationUser, Long paramLong1, Long paramLong2);

    ServiceResult enableIconOption(ApplicationUser paramApplicationUser, Long paramLong1, Long paramLong2);

    ServiceOutcome<IconOptionBean> moveToPosition(ApplicationUser paramApplicationUser, Long paramLong1, Long paramLong2, int paramInt);

    ServiceOutcome<IconOptionBean> moveToAfter(ApplicationUser paramApplicationUser, Long paramLong1, Long paramLong2, Long paramLong3);

    Long getAvatarIdForOption(Option paramOption);
}
