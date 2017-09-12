package com.microcold.hosts.test;

import com.microcold.hosts.command.HostsCommand;

/*
 * Created by MicroCold on 2017/9/4.
 */
public class CommandTest {

    public static void main(String[] args) {
        // HostsCommand.doCommand(new String[] { "-s", "10.10.102.106" });
        // HostsCommand.doCommand(new String[] { "-c" });
        HostsCommand.doCommand(new String[] { "-s", "m.liepin.com" });
        HostsCommand.doCommand(new String[] { "-s", "m.liepin.com", "-disable" });
    }
}
