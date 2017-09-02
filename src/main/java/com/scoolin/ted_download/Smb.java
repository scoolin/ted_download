package com.scoolin.ted_download;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author scoolin on 2017-09-02.
 */
public class Smb {

    private final static Logger logger = Logger.getLogger(Smb.class.getName());

    public static void main(String[] args) throws MalformedURLException, SmbException {
        String path = "smb://192.168.31.1/纪录片/Ted/";
        Set<String> set = getAllDownloadedTedVideos(path);
        set.forEach(System.out::println);
        System.out.println(set.size());
        set.forEach(Smb::printNoZhCn);
    }

    private static void printNoZhCn(String file) {
        if (!file.contains("zh-cn")) {
            System.out.println(file);
        }
    }

    static Set<String> getAllDownloadedTedVideos(String path) throws SmbException, MalformedURLException {
        try {
            Set<String> set = new TreeSet<>();
            SmbFile sf = new SmbFile(path);
            getAllDownloadedTedVideos(sf, set);
            return set;
        } catch (SmbException | MalformedURLException e) {
            Smb.logger.log(Level.WARNING, "get downloaded ted videos from samba failed");
            throw e;
        }
    }

    private static void getAllDownloadedTedVideos(SmbFile sf, Set<String> set) throws SmbException {
        if (sf.isDirectory()) {
            for (SmbFile s : sf.listFiles()) {
                if (s.isDirectory()) {
                    getAllDownloadedTedVideos(s, set);
                } else if (s.getName().endsWith(".mp4")) {
                    set.add(s.getName());
                }
            }
        } else if (sf.getName().endsWith(".mp4")) {
            set.add(sf.getName());
        }
    }
}
