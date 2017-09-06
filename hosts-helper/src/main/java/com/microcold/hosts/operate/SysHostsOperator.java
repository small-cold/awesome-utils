package com.microcold.hosts.operate;


import com.microcold.hosts.conf.Config;
import com.microcold.hosts.exception.PermissionIOException;
import com.microcold.hosts.utils.SystemUtil;

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
        super(SystemUtil.getSysHostsPath());
    }

    @Override
    public void flush() throws IOException {
        boolean result = SystemUtil.changeMod(Config.getAdminPassword(), getFile().getPath());
        if (!result){
            throw new PermissionIOException("需要管理员权限");
        }
        super.flush();
    }
}
