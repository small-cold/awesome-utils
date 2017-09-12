package com.microcold.hosts;

import com.google.common.collect.Lists;
import com.microcold.hosts.command.HostsCommand;
import com.microcold.hosts.view.HostsHelperApp;

import java.util.List;

/*
 * Created by MicroCold on 2017/9/4.
 */
public class HostsHelper {

    public static void main(String[] args) {
        List<String> argList = Lists.newArrayList(args);
        if (argList.contains("windows")) {
            HostsHelperApp.launch(args);
        } else {
            HostsCommand.doCommand(args);
        }
    }
}
