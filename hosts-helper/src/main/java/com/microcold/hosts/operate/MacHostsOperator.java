package com.microcold.hosts.operate;


import java.io.FileNotFoundException;

/*
 * Created by MicroCold on 2017/9/1.
 */
public class MacHostsOperator extends HostsOperator {

    public static MacHostsOperator instance;

    public static MacHostsOperator getInstance() throws FileNotFoundException {
        if (instance == null){
            instance = new MacHostsOperator();
        }
        return instance;
    }

    private MacHostsOperator() throws FileNotFoundException {
        super("/etc/hosts");
    }
}
