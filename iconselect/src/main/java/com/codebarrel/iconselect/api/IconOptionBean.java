package com.codebarrel.iconselect.api;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.util.JiraUrlCodec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "option")
@XmlAccessorType(XmlAccessType.FIELD)
public class IconOptionBean {
    @XmlElement(name = "id")
    private Long id;

    @XmlElement(name = "self")
    private String self;

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "iconUrl")
    private String iconUrl;

    @XmlElement(name = "avatarId")
    private Long avatarId;

    @XmlElement(name = "sequence")
    private Long sequence;

    @XmlElement(name = "disabled")
    private boolean disabled;

    public IconOptionBean() {}

    public IconOptionBean(Long id, String label, Long avatarId, Long sequence, boolean disabled) {
        this(id, label, avatarId, sequence, disabled, "");
    }

    public IconOptionBean(Long id, String label, Long avatarId, Long sequence, boolean disabled, String baseUrl) {
        this.id = id;
        this.label = label;
        this.avatarId = avatarId;
        this.sequence = sequence;
        this.disabled = disabled;
        if (baseUrl == null)
            baseUrl = "";
        this.self = baseUrl + "/rest/iconselectoptions/1.0/option/" + JiraUrlCodec.encode(id.toString());
        this.iconUrl = baseUrl + "/secure/viewavatar?size=xsmall&avatarId=" + avatarId + "&avatarType=iconselectlist";
    }

    public String getSelf() {
        return this.self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getAvatarId() {
        return this.avatarId;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }

    public Long getSequence() {
        return this.sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static IconOptionBean fromOption(Option option, Long avatarId, JiraBaseUrls urls) {
        if (option == null)
            return null;
        IconOptionBean bean = new IconOptionBean(option.getOptionId(), option.getValue(), avatarId, option.getSequence(), option.getDisabled().booleanValue(), urls.baseUrl());
        return bean;
    }
}
