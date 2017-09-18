package com.microcold.hosts.test;

import com.microcold.hosts.conf.RemoteHostsFile;
import com.microcold.hosts.operate.Downloader;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/*
 * Created by MicroCold on 2017/9/14.
 */
public class DownloaderTest {

    @Test
    public void test() throws NoSuchAlgorithmException, IOException, KeyManagementException {
        RemoteHostsFile remoteHostsFile = new RemoteHostsFile();
        remoteHostsFile.setName("远程/QA1");
        remoteHostsFile.setUrl("http://10.11.6.101:7002/client/getHostsFile?name=LPS01");
        Downloader.downloadAndWrite(remoteHostsFile);
    }
}
