package com.microcold.hosts.operate;

import com.google.common.collect.Lists;
import com.microcold.hosts.conf.Config;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/*
 * Created by MicroCold on 2017/9/6.
 */
@Getter
@Setter
@ToString
public class HostsOperatorCategory {

    private int sort = 0;

    private String name = "";

    private List<HostsOperator> hostsOperatorList = Lists.newArrayList();

    /**
     * 子节点
     */
    private List<HostsOperatorCategory> subCategoryList = Lists.newArrayList();

    public HostsOperator getHostsOperator(String fileName) {
        for (HostsOperator hostsOperator: getHostsOperatorList()){
            if (hostsOperator.getName().equals(fileName)){
                return hostsOperator;
            }
        }
        HostsOperator hostsOperator = null;
        for (HostsOperatorCategory hostsOperatorCategory: getSubCategoryList()){
            hostsOperator = hostsOperatorCategory.getHostsOperator(fileName);
            if (hostsOperator != null){
                return hostsOperator;
            }
        }
        return null;
    }
}
