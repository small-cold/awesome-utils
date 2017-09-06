package com.microcold.hosts.view.controller;

import com.microcold.hosts.operate.HostsOperator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/*
 * Created by MicroCold on 2017/9/5.
 */
public class HostsOperatorProperty {

    /**
     * 显示名称
     */
    private String showName;

    private HostsOperator hostsOperator;

    @Setter
    @Getter
    private boolean enable;

    public String getShowName() {
        return showName;
    }

    public HostsOperatorProperty setShowName(String showName) {
        this.showName = showName;
        this.enable = true;
        return this;
    }

    public HostsOperator getHostsOperator() {
        return hostsOperator;
    }

    public HostsOperatorProperty setHostsOperator(HostsOperator hostsOperator) {
        this.hostsOperator = hostsOperator;
        return this;
    }

    @Override
    public String toString() {
        String result = "--";
        if (StringUtils.isNotBlank(showName)){
            result = showName;
        }
        if (hostsOperator != null){
            result = hostsOperator.getName();
        }
        if (!enable){
            result += "[无效]";
        }
        return result;
    }
}
