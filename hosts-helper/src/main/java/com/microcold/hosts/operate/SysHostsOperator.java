package com.microcold.hosts.operate;


import com.microcold.hosts.conf.Config;
import com.microcold.hosts.exception.PermissionIOException;
import com.microcold.hosts.utils.SystemUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * Created by MicroCold on 2017/9/1.
 */
public class SysHostsOperator extends HostsOperator {

    public static SysHostsOperator instance;

    public static SysHostsOperator getInstance(){
        if (instance == null){
            instance = new SysHostsOperator();
        }
        return instance;
    }

    private SysHostsOperator(){
        super(Config.getSysHostsPath());
    }

    @Override
    public boolean isOnlyRead() {
        return StringUtils.isBlank(Config.getAdminPassword());
    }

    @Override
    public void flush() throws IOException {
        if (StringUtils.isBlank(Config.getAdminPassword())){
            throw new PermissionIOException("需要管理员权限");
        }
        File cacheFile = new File(Config.getCacheFile(), "currentHost");
        try (FileWriter fileWriter = new FileWriter(cacheFile)) {
            for (HostBean hostBean : getHostBeanList()) {
                fileWriter.write(hostBean.toString() + "\n");
            }
            //  copy to 系统目录
            SystemUtil.adminMove(cacheFile, new File(SystemUtil.getSysHostsPath()), Config.getAdminPassword());
            // 清除系统缓存
            SystemUtil.clearDNSCache(null);
        } catch (IOException e) {
            LOGGER.error("写入Hosts文件发生错误 file=" + cacheFile, e);
            throw e;
        }
        // super.flush();
    }
}
