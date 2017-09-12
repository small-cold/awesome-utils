package com.microcold.hosts.view.controller;

import com.microcold.hosts.operate.HostBean;
import com.microcold.hosts.operate.HostsOperator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * Created by MicroCold on 2017/9/12.
 */
@Getter
@Setter
@ToString
public class HostsSearchResult {

    private HostsOperator hostsOperator;

    private int id;

    private String title;

    private String description;

    private int score;

    public HostsSearchResult(HostsOperator hostsOperator, HostBean hostBean) {
        this.hostsOperator = hostsOperator;
        id = hostBean.getId();
        title = hostBean.getDomain();
        description = hostBean.toString();
    }
}
