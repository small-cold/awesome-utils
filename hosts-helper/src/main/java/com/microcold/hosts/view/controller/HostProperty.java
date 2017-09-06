package com.microcold.hosts.view.controller;

import com.microcold.hosts.operate.HostBean;
import com.microcold.hosts.utils.IPDomainUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Created by MicroCold on 2017/9/4.
 */
public class HostProperty {

    private final IntegerProperty id;
    private final BooleanProperty enable;
    private final StringProperty ip;
    private final StringProperty domain;
    private final StringProperty content;

    public HostProperty() {
        this.id = new SimpleIntegerProperty(-1);
        this.enable = new SimpleBooleanProperty(false);
        this.ip = new SimpleStringProperty("");
        this.domain = new SimpleStringProperty("");
        this.content = new SimpleStringProperty("");
    }
    public HostProperty(HostBean hostBean) {
        this.id = new SimpleIntegerProperty(hostBean.getId());
        this.enable = new SimpleBooleanProperty(hostBean.isEnable());
        this.ip = new SimpleStringProperty(IPDomainUtil.longToIP(hostBean.getIp()));
        this.domain = new SimpleStringProperty(hostBean.getDomain());
        this.content = new SimpleStringProperty(hostBean.getComment());
    }

    public BooleanProperty enableProperty() { return enable; }

    public IntegerProperty idProperty() { return id; }

    public StringProperty ipProperty() { return ip; }

    public StringProperty domainProperty() { return domain; }

    public StringProperty contentProperty() { return content; }
}
