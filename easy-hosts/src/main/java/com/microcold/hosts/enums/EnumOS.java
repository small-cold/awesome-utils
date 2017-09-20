package com.microcold.hosts.enums;

/*
 * Created by MicroCold on 2017/9/3.
 */
public enum EnumOS {

    MacOS("MacOS"),
    Linux("Linux"),
    Windows("Windows"),
    OTHER("Other"),;

    private EnumOS(String desc) {
        this.description = desc;
    }

    public String toString() {
        return description;
    }

    private String description;
}
