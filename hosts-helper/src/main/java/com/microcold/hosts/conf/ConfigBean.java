package com.microcold.hosts.conf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microcold.hosts.resources.ControlResources;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
 * Created by MicroCold on 2017/9/3.
 */
@ToString
public class ConfigBean {

    /**
     * 远程Hosts文件地址，名称-url
     */
    @Getter
    @Setter
    private Map<String, URL> remoteHostsURLMap = Maps.newTreeMap();

    /**
     * 是否自动备份
     */
    @Getter
    @Setter
    private Boolean autoBackup = false;

    /**
     * 最大备份的文件数
     */
    @Getter
    @Setter
    private int maxBackupLimit = 10;

    /**
     * 通用Hosts文件名
     */
    @Getter
    @Setter
    private String commonHostsFileName = ControlResources.getString("common-hosts-file");

    /**
     * 系统环境变量
     */
    @Getter
    @Setter
    private String sysHostsPath;

    /**
     * hosts 文件分组深度
     */
    @Getter
    private int hostsCategoryDeep = 1;

    @Getter
    @Setter
    private List<RemoteHostsFile> remoteHostsFileList = Lists.newArrayList();

    public ConfigBean setHostsCategoryDeep(int hostsCategoryDeep) {
        if (hostsCategoryDeep < 1) {
            hostsCategoryDeep = 1;
        }
        if (hostsCategoryDeep > 3) {
            hostsCategoryDeep = 3;
        }
        this.hostsCategoryDeep = hostsCategoryDeep;
        return this;
    }

    public boolean addRemoteHostsFile(String path, String url) {
        RemoteHostsFile remoteHostsFile = new RemoteHostsFile();
        remoteHostsFile.setName(path);
        remoteHostsFile.setUrl(url);
        remoteHostsFile.setCreateDateTime(LocalDateTime.now());
        return remoteHostsFileList.contains(remoteHostsFile) || remoteHostsFileList.add(remoteHostsFile);
    }

    public boolean removeRemoteHostsFile(RemoteHostsFile remoteHostsFile) {
        return remoteHostsFileList.remove(remoteHostsFile);
    }

}
