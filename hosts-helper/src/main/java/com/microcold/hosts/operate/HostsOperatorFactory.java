package com.microcold.hosts.operate;

import com.google.common.collect.Maps;
import com.microcold.hosts.conf.Config;
import com.microcold.hosts.enums.EnumOS;
import com.microcold.hosts.utils.SystemUtil;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
 * Created by MicroCold on 2017/9/3.
 */
public class HostsOperatorFactory {

    private static HostsOperator sysHostsOperator;
    private static HostsOperator comHostsOperator;

    public static HostsOperator getSystemHostsOperator()  {
        if (sysHostsOperator != null){
            return sysHostsOperator;
        }
        if (SystemUtil.CURRENT_OS == EnumOS.MacOS){
            sysHostsOperator = new HostsOperator("/etc/hosts");
        }
        return sysHostsOperator;
    }

    public static Map<String, HostsOperator> getUserHostsOperatorMap() {
        Map<String, HostsOperator> hostsOperatorTreeMap = Maps.newTreeMap();
        List<File> fileList = Config.getUserHostFileList();
        if (CollectionUtils.isNotEmpty(fileList)) {
            for (File childFile : fileList) {
                if (childFile.getName().startsWith(".")){
                    continue;
                }
                HostsOperator hostsOperator = new HostsOperator(childFile);
                hostsOperatorTreeMap.put(hostsOperator.getName(), hostsOperator);
            }
        }
        return hostsOperatorTreeMap;
    }

    public static HostsOperator getCommonHostsOperator() throws IOException {
        File file = Config.getCommonHostFile();
        if (file != null && file.exists() && file.isFile()){
            comHostsOperator = new HostsOperator(file);
        }
        return comHostsOperator;
    }
}
