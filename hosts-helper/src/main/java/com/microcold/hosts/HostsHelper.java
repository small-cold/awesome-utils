package com.microcold.hosts;

import com.microcold.hosts.command.HostsCommand;
import org.apache.log4j.Logger;

import java.io.IOException;

/*
 * Created by MicroCold on 2017/9/4.
 */
public class HostsHelper {

    public static void main(String[] args) {
        if (args != null && args.length > 0){
            HostsCommand.doCommand(args);
        }else {
            // TODO 启动图形页面
            Logger.getLogger(HostsHelper.class).warn("请输入参数");
        }
    }
}
