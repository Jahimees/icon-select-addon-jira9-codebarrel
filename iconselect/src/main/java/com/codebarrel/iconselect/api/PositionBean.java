package com.codebarrel.iconselect.api;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "position")
@XmlAccessorType(XmlAccessType.FIELD)
public class PositionBean {
    @XmlElement(name = "after")
    private String after = null;

    @XmlElement(name = "position")
    private String position = null;

    public PositionBean() {}

    public PositionBean(String after, String position) {
        this.after = after;
        this.position = position;
    }

    public String getAfter() {
        return this.after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getPosition() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
