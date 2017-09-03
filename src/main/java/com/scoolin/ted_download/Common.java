package com.scoolin.ted_download;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author scoolin on 2017-09-03.
 */
public class Common {

    final static String DOWNLOAD_DIR = "download/";
    final static String DOWNLOAD_HOST = "metated.petarmaric.com";
    final static String DOWNLOAD_URI = "/metalinks/TED-talks-in-high-quality.zh-cn.metalink";

    static String getLastFile() throws IOException {
        SortedSet<String> set = new TreeSet<>();
        DirectoryStream<Path> d = Files.newDirectoryStream(Paths.get(DOWNLOAD_DIR));
        d.forEach(path -> {
            File file = path.toFile();
            if (file.getName().startsWith("ted_") && file.getName().endsWith(".xml")) {
                set.add(path.toFile().getAbsolutePath());
            }
        });
        if (set.size() == 0) return null;
        return set.last();
    }

}
