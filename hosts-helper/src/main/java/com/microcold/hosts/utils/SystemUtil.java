package com.microcold.hosts.utils;

import com.microcold.hosts.enums.EnumOS;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/*
 * Created by MicroCold on 2017/9/3.
 */
public class SystemUtil {
    private static String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static final EnumOS CURRENT_OS = getCurrentOS();

    private static EnumOS getCurrentOS(){
        if (OS_NAME.contains("mac")){
            return EnumOS.MacOS;
        }
        if (OS_NAME.contains("win")){
            return EnumOS.Windows;
        }
        if (OS_NAME.contains("linux")){
            return EnumOS.Linux;
        }
        return EnumOS.OTHER;
    }

    public void clearDNSCache(String cmd){
        if (StringUtils.isBlank(cmd)){
            if (CURRENT_OS == EnumOS.MacOS){
                cmd = "killall -HUP mDNSResponder";
            }else if (CURRENT_OS == EnumOS.Windows){
                cmd = "ipconfig /flushdns";
            }
        }
        try {
            Runtime.getRuntime().exec(cmd);
            try {
                Thread.sleep(200L);
            } catch (InterruptedException ie) {
                Logger.getLogger(SystemUtil.class).warn(ie);
            }
        } catch (IOException ioe) {
            Logger.getLogger(SystemUtil.class).warn(ioe);
        }
    }
}
