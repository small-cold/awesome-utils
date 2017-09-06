package com.microcold.hosts.operate;

import com.google.common.collect.Lists;
import com.microcold.hosts.utils.IPDomainUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hosts 操作工具类
 * Created by MicroCold on 2017/9/1.
 */
public class HostsOperator {
    private static final Logger LOGGER = Logger.getLogger(HostsOperator.class);

    @Getter
    @Setter
    private List<HostBean> hostBeanList;

    @Getter
    @Setter
    private File file;

    @Getter
    @Setter
    private String name;

    public HostsOperator(String path) {
        this(new File(path));
    }

    public HostsOperator(File file) {
        this(file, file.getName());
    }

    public HostsOperator(File file, String name) {
        this.file = file;
        this.name = name;
        this.hostBeanList = Collections.emptyList();
    }

    public HostsOperator init() throws FileNotFoundException {
        hostBeanList = readHostFile();
        return this;
    }

    /**
     * 读取Hosts 文件
     *
     * @param path hosts 文件地址
     * @return List<HostBean>
     * @throws FileNotFoundException 文件不存在
     */
    public List<HostBean> readHostFile() throws FileNotFoundException {
        FileReader reader = new FileReader(file);
        List<HostBean> hostBeanList = Lists.newArrayList();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                for (HostBean hostBean : HostBean.build(line)) {
                    hostBean.setId(hostBeanList.size());
                    hostBeanList.add(hostBean);
                }
            }
        } catch (IOException e) {
            LOGGER.error("读取Hosts文件发生错误 file=" + file, e);
        }
        return hostBeanList;
    }

    /**
     * 刷新写入文件
     *
     * @param hostBeanList host实体类
     * @param path         写入地址
     */
    public void flush() throws IOException {
        // TODO 重新排序
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (HostBean hostBean : hostBeanList) {
                fileWriter.write(hostBean.toString() + "\n");
            }
        } catch (IOException e) {
            LOGGER.error("写入Hosts文件发生错误 file=" + file + ", hostBeanList=" + hostBeanList, e);
            throw e;
        }
    }

    /**
     * 根据域名找到已有的配置
     *
     * @param domain
     * @return
     */
    public List<HostBean> lookupByDomain(String domain) {
        List<HostBean> matchHostBeanList = Lists.newArrayList();
        for (HostBean hostBean : getHostBeanList()) {
            if (hostBean.isValid() && hostBean.getDomain().equals(domain)) {
                matchHostBeanList.add(hostBean);
            }
        }
        return matchHostBeanList;
    }

    /**
     * 添加host
     *
     * @param hostBean
     */
    public boolean saveHost(HostBean hostBean) throws IOException {
        if (hostBean == null) {
            return false;
        }
        if (!hostBean.isValid()) {
            return getHostBeanList().add(hostBean);
        }
        if (hostBean.isEnable()) {
            enable(hostBean.getIp(), hostBean.getDomain());
        } else {
            disable(hostBean.getIp(), hostBean.getDomain());
        }
        return true;
    }

    public void changeStatus(String ipStr, String domain, boolean enable) throws IOException {
        if (enable) {
            enable(IPDomainUtil.ipToLong(ipStr), domain);
        } else {
            disable(IPDomainUtil.ipToLong(ipStr), domain);
        }
    }

    private void disable(long ip, String domain) throws IOException {
        if (StringUtils.isBlank(domain)) {
            throw new RuntimeException("禁用域名不能为空");
        }
        boolean changed = false;
        List<HostBean> hostBeanList = Lists.newArrayList(getHostBeanList().iterator());
        for (HostBean existHostBean : hostBeanList) {
            if (!existHostBean.isValid()
                    || !existHostBean.getDomain().startsWith(domain)) {
                continue;
            }
            // 指定IP 只禁用对应的，否则禁用全部
            if (ip >= 0 && existHostBean.getIp() != ip) {
                continue;
            }
            existHostBean.setEnable(false);
            changed = true;
        }
        if (changed) {
            flush();
        }
    }

    private void enable(long ip, String domain) throws IOException {
        if (ip < 0) {
            throw new RuntimeException("启用IP无效");
        }
        if (StringUtils.isBlank(domain)) {
            throw new RuntimeException("启用域名不能为空");
        }
        boolean changed = false;
        List<HostBean> hostBeanList = Lists.newArrayList(getHostBeanList().iterator());
        for (HostBean existHostBean : hostBeanList) {
            if (!existHostBean.isValid()) {
                continue;
            }
            // 不包含，跳过
            if (!existHostBean.getDomain().equals(domain)) {
                continue;
            }
            // 包含，但是已经启用了
            if (existHostBean.isEnable() && existHostBean.getIp() == ip) {
                continue;
            }
            if (existHostBean.getIp() != ip) { // IP 不同，全部禁用
                existHostBean.setEnable(false);
                changed = true;
            } else if (!existHostBean.isEnable()) { // IP 相同，原来的禁用
                existHostBean.setEnable(true);
                changed = true;
            }
            HostBean newHostBean = new HostBean(ip, domain, true);
            if (!getHostBeanList().contains(newHostBean)) {
                getHostBeanList().add(newHostBean);
                changed = true;
            }
        }
        if (changed) {
            flush();
        }
    }

    /**
     * 切换配置到
     *
     * @param otherHostsOperator
     * @param isBackup
     */
    public void switchTo(HostsOperator... hostsOperators) throws IOException {
        if (hostsOperators == null) {
            return;
        }
        List<HostBean> newHostBeanList = Lists.newArrayList();
        for (HostsOperator hostsOperator : hostsOperators) {
            if (hostsOperator == null
                    || CollectionUtils.isEmpty(hostsOperator.getHostBeanList())) {
                continue;
            }
            for (HostBean hostBean : hostsOperator.getHostBeanList()) {
                if (hostBean.isValid() && newHostBeanList.contains(hostBean)) {
                    continue;
                }
                newHostBeanList.add(hostBean);
            }
        }
        setHostBeanList(newHostBeanList);
        flush();
    }

    @Override
    public String toString() {
        return name;
    }

    public void replaceIP(String source, String target) throws IOException {
        long sourceIp = IPDomainUtil.ipToLong(source);
        long targetIp = IPDomainUtil.ipToLong(target);
        if (sourceIp == targetIp) {
            return;
        }
        for (HostBean hostBean : getHostBeanList()) {
            if (!hostBean.isValid()) {
                continue;
            }
            if (hostBean.getIp() == sourceIp) {
                hostBean.setIp(targetIp);
                flush();
            }
        }
    }

    public Set<String> getIPSet() {
        Set<String> IPSet = new HashSet<>();
        for (HostBean hostBean : getHostBeanList()) {
            if (hostBean.isValid()) {
                IPSet.add(IPDomainUtil.longToIP(hostBean.getIp()));
            }
        }
        return IPSet;
    }

    public boolean enable(int i, boolean enable) throws IOException {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        HostBean hostBean = hostBeanList.get(i);
        if (enable != hostBean.isEnable()) {
            hostBean.setEnable(enable);
            flush();
            return true;
        }
        return true;
    }

    public boolean saveDomain(int i, @NonNull String domain) throws IOException {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        HostBean hostBean = hostBeanList.get(i);
        if (StringUtils.isBlank(domain)) {
            throw new IllegalArgumentException("域名不能为空");
        }
        if (hostBean.getIp() != IPDomainUtil.SELF_IP_LONG && !IPDomainUtil.isDomain(domain)
                || hostBean.getIp() == IPDomainUtil.SELF_IP_LONG && !IPDomainUtil.isSelfDomain(domain)) {
            throw new IllegalArgumentException("域名非法");
        }

        if (!hostBean.getDomain().equals(domain)) {
            hostBean.setDomain(domain);
            flush();
            return true;
        }
        return false;
    }

    public boolean saveIp(int i, @NonNull String ipStr) throws IOException {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        if (StringUtils.isBlank(ipStr)) {
            throw new IllegalArgumentException("IP不能为空");
        }
        if (!IPDomainUtil.isIp(ipStr)) {
            throw new IllegalArgumentException("IP非法");
        }
        long ip = IPDomainUtil.getIPLong(ipStr);
        HostBean hostBean = hostBeanList.get(i);
        if (hostBean.getIp() != ip) {
            hostBean.setIp(ip);
            flush();
            return true;
        }
        return false;
    }

    public HostBean get(int i) {
        return getHostBeanList().get(i);
    }

    public boolean saveComment(int i, String newValue) {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        return false;
    }
}
