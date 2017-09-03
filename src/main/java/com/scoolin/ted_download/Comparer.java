package com.scoolin.ted_download;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author scoolin on 2017-09-03.
 */
class Comparer {

    public static void main(String[] args) {
        try {
            String lastFile = Common.getLastFile();
            if (lastFile == null) {
                System.out.println("no file downloaded");
                System.exit(0);
            }
            Set<String> urls = Parser.parse(Files.readAllBytes(Paths.get(lastFile)));
            Set<String> filenames = Smb.getAllDownloadedTedVideos();
            Set<String> diff = diff(urls, filenames);
            diff.forEach(System.out::println);
            System.out.println(diff.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Set<String> diff(Set<String> urls, Set<String> filenames) {
        Set<String> diff = new TreeSet<>();
        for (String url : urls) {
            if (!filenames.contains(getUrlFilename(url))) {
                diff.add(url);
            }
        }
        return diff;
    }

    private static String getUrlFilename(String url) {
        int start = url.lastIndexOf("/");
        String file;
        if (url.contains("?")) {
            file = url.substring(start + 1, url.lastIndexOf("?"));
        } else {
            file = url.substring(start + 1);
        }
        return file;
    }
}
