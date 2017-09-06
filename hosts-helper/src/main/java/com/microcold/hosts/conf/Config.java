package com.microcold.hosts.conf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microcold.hosts.operate.HostsOperator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * Created by MicroCold on 2017/9/3.
 */
public class Config {

    private static final Logger logger = Logger.getLogger(Config.class);

    private static String adminPassword;

    /**
     * 工作目录
     */
    private static Path workPath;

    /**
     * 系统配置属性
     */
    private static Properties sysProperties = new Properties();

    /**
     * 用户配置文件
     */
    private static File userSettingFile;

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        try {
            sysProperties.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            logger.error("读取属性文件异常", e);
        }
        String workPathStr = sysProperties.getProperty("work-path");
        if (StringUtils.isBlank(workPathStr)) {
            workPathStr = "~/.config/awesome/hosts";
        }
        if (workPathStr.startsWith("~")) {
            workPathStr = workPathStr.replaceFirst("^~", System.getProperties().getProperty("user.home"));
        }
        workPath = Paths.get(workPathStr);
        if (!workPath.toFile().exists()) {
            boolean result = Config.workPath.toFile().mkdirs();
            if (!result) {
                logger.error("创建工作文件夹失败");
            }
        }
        // 读取用户配置文件
        userSettingFile = new File(Config.workPath.toFile(), sysProperties.getProperty("user-setting-file"));
    }

    public static ConfigBean getConfigBean(){
        if (!userSettingFile.exists()) {
            ConfigBean configBean = new ConfigBean();
            saveConfig(configBean);
            return configBean;
        }
        try {
            FileReader fileReader = new FileReader(userSettingFile);
            String configStr = "";
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    configStr += line;
                }
            } catch (IOException e) {
                logger.error("读取配置文件发生错误 workPath=" + Config.workPath, e);
            }
            return objectMapper.readValue(configStr, ConfigBean.class);
        } catch (IOException e) {
            logger.error("读取用户配置文件异常", e);
            return new ConfigBean();
        }
    }

    public static boolean saveConfig(ConfigBean configBean) {
        if (configBean == null) {
            return false;
        }
        try (OutputStream os = new FileOutputStream(userSettingFile)) {
            objectMapper.defaultPrettyPrintingWriter().writeValue(os, configBean);
            return true;
        } catch (IOException e) {
            logger.error("保存配置异常", e);
            return false;
        }
    }

    public static List<File> getUserHostFileList(){
        File file = new File(workPath.toFile(), "files");
        List<File> fileList = Lists.newArrayList();
        if (file.exists() && file.listFiles() != null) {
            for (File childFile : file.listFiles()) {
                if (childFile.getName().startsWith(".")){
                    continue;
                }
                fileList.add(childFile);
            }
        }
        return fileList;
    }

    public static File getCommonHostFile() throws IOException {
        ConfigBean configBean = getConfigBean();
        if (configBean != null){
            return new File(workPath.toFile(), getConfigBean().getCommonHostsFileName());
        }
        return null;
    }

    /**
     * 监控文件变化
     */
    private static void watch() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            //给path路径加上文件观察服务
            workPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            // start an infinite loop
            while (true) {
                final WatchKey key = watchService.take();

                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    final WatchEvent.Kind<?> kind = watchEvent.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    //创建事件
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                    }
                    //修改事件
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                    }
                    //删除事件
                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {

                    }
                    // get the filename for the event
                    final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    final Path filename = watchEventPath.context();
                    // print it out
                    System.out.println(kind + " -> " + filename);

                }
                // reset the keyf
                boolean valid = key.reset();
                // exit loop if the key is not valid (if the directory was
                // deleted, for
                if (!valid) {
                    break;
                }
            }

        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
        }
    }

    public static String getAdminPassword() {
        return adminPassword;
    }

    public static void setAdminPassword(String adminPassword) {
        Config.adminPassword = adminPassword;
    }
}
