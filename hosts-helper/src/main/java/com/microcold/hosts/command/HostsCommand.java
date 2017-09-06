package com.microcold.hosts.command;

import com.google.common.collect.Lists;
import com.microcold.hosts.conf.Config;
import com.microcold.hosts.conf.ConfigBean;
import com.microcold.hosts.operate.HostBean;
import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.operate.HostsOperatorFactory;
import com.microcold.hosts.utils.IPDomainUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/*
 * Created by MicroCold on 2017/9/1.
 */
public class HostsCommand {

    private static Logger logger = Logger.getLogger(HostsCommand.class);

    private static Options options = new Options();

    static {
        logger.setLevel(Level.DEBUG);
        // 切换命令
        options.addOption("c", false, "复制一份文件替换系统hosts文件");
        options.addOption(Option.builder("s")
                .hasArg()
                .desc("切换host配置")
                .argName("switch")
                .build());
        options.addOption("disable", false, "禁用配置");
        options.addOption("show", false, "显示可用配置");
    }

    public static void main(String[] args) {
        doCommand(args);
    }

    public static void doCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            ConfigBean configBean = Config.getConfigBean();
            // 解析参数
            CommandLine line = parser.parse(options, args);
            boolean disabled = line.hasOption("disable");
            boolean isBackup = BooleanUtils.isTrue(configBean.getAutoBackup())
                    || line.hasOption("backup");
            if (line.hasOption("c")) {
                switchFile(line.getOptionValue("c"), isBackup);
            } else if (line.hasOption("s")) {
                String optionValue = line.getOptionValue("switch");
                switchTo(optionValue, disabled);
            }
        } catch (Exception exp) {
            try {
                logger.error("发生异常: args=" + new ObjectMapper().writeValueAsString(args), exp);
            } catch (IOException e) {
                //
            }
        }
    }

    private static void switchFile(String fileName, boolean isBackup) throws IOException {
        Map<String, HostsOperator> hostsOperatorMap = HostsOperatorFactory.getUserHostsOperatorMap();
        // 没输入文件名，提示选择
        if (StringUtils.isBlank(fileName)) {
            StringBuilder msg = new StringBuilder("可选环境如下：");
            int index = 0;
            for (HostsOperator hostsOperator : hostsOperatorMap.values()) {
                msg.append(index).append(". ").append(hostsOperator.getName());
                index++;
                if (index < hostsOperatorMap.size()) {
                    msg.append(", ");
                }
            }
            logger.info(msg.toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            fileName = br.readLine();
        }
        HostsOperator sysHostsOperator = HostsOperatorFactory.getSystemHostsOperator().init();
        HostsOperator comHostsOperator = HostsOperatorFactory.getCommonHostsOperator();
        if (comHostsOperator != null) {
            comHostsOperator.init();
        }
        HostsOperator newHostsOperator = hostsOperatorMap.get(fileName);
        if (newHostsOperator == null && fileName.matches("\\d+")) {
            Integer fileIndex = Integer.parseInt(fileName);
            if (fileIndex >= 0 && fileIndex < hostsOperatorMap.size()) {
                newHostsOperator = Lists.newArrayList(hostsOperatorMap.values().iterator())
                        .get(fileIndex).init();
            }
        }
        sysHostsOperator.switchTo(comHostsOperator, newHostsOperator);
    }

    private static void switchTo(String opt, boolean disabled) throws IOException {
        if (IPDomainUtil.isIp(opt)) {
            switchByIp(opt);
        } else if (IPDomainUtil.isDomain(opt)) {
            switchByDomain(opt, disabled);
        }
    }

    private static void switchByIp(String targetIp) throws IOException {
        // 切换配置文件
        HostsOperator hostsOperator = HostsOperatorFactory.getSystemHostsOperator().init();
        List<String> ipList = Lists.newArrayList(hostsOperator.getIPSet().iterator());
        logger.info("请选择带替换IP地址：(默认第0个)");
        int index = 0;
        for (String ip : ipList) {
            logger.info(index + ". " + ip);
            index++;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String source = br.readLine();
        Integer selectedIndex = Integer.parseInt(source);
        if (StringUtils.isBlank(source)) {
            selectedIndex = 0;
        }
        hostsOperator.replaceIP(ipList.get(selectedIndex), targetIp);
        hostsOperator.flush();
    }

    private static void switchByDomain(String domain, boolean disabled) throws IOException {
        HostsOperator hostsOperator = HostsOperatorFactory.getSystemHostsOperator().init();
        List<HostBean> hostBeanList = hostsOperator.lookupByDomain(domain);
        int index = 0;
        for (HostBean hostBean : hostBeanList) {
            logger.info(index + ". " + (hostBean.isEnable() ? "[启用]" : "[禁用]")
                    + IPDomainUtil.longToIP(hostBean.getIp()));
            index++;
        }
        logger.info("请输入要切换的IP或要启用的配置序号(默认为127.0.0.1):");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String opt = br.readLine();
        if (StringUtils.isBlank(opt)) {
            opt = IPDomainUtil.SELF_IP;
        }
        if (IPDomainUtil.isIp(opt)) {
            hostsOperator.changeStatus(opt, domain, !disabled);
        } else if (opt.matches("[0-9]+")) {
            int indexSelected = Integer.parseInt(opt);
            hostBeanList.get(indexSelected).setEnable(!disabled);
        }
        hostsOperator.flush();
    }
}
