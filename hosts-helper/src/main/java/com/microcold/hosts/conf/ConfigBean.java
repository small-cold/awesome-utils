package com.microcold.hosts.conf;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.URL;
import java.util.Map;

/*
 * Created by MicroCold on 2017/9/3.
 */
@Getter
@Setter
@ToString
public class ConfigBean {

    /**
     * 远程Hosts文件地址，名称-url
     */
    private Map<String, URL> remoteHostsURLMap = Maps.newTreeMap();

    /**
     * 是否自动备份
     */
    private Boolean autoBackup = false;

    /**
     * 最大备份的文件数
     */
    private int maxBackupLimit = 10;

    /**
     * 通用Hosts文件名
     */
    private String commonHostsFileName = "common";
}
