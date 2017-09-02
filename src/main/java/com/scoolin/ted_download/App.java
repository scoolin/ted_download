package com.scoolin.ted_download;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.impl.VertxInternal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author scoolin on 2017-08-31.
 */
public class App {

    private final static String DOWNLOAD_DIR = "download/";
    private final static String DOWNLOAD_VIDEO_PATH = "smb://192.168.31.1/纪录片/Ted/";
    private final static String DOWNLOAD_HOST = "metated.petarmaric.com";
    private final static String DOWNLOAD_URI = "/metalinks/TED-talks-in-high-quality.zh-cn.metalink";
    private final static Logger logger = Logger.getLogger(App.class.getName());
    private final Vertx vertx;
    private final HttpClient client;
    private byte[] lastFile;

    private App(Vertx vertx) {
        this.vertx = vertx;
        this.client = vertx.createHttpClient();
        VertxInternal vi = (VertxInternal) vertx;
        vi.addCloseHook(h -> {
            client.close();
            h.handle(Future.succeededFuture());
        });
        try {
            String file = getLastFile();
            if (file != null) {
                this.lastFile = Files.readAllBytes(Paths.get(file));
            } else {
                this.lastFile = new byte[0];
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "get last file failed", e);
        }
    }

    public static void main(String[] args) {
        App app = new App(Vertx.vertx());
        app.start();
    }

    private void compareToDownload() {
        vertx.executeBlocking(future -> {
            try {
                Set<String> files = Smb.getAllDownloadedTedVideos(DOWNLOAD_VIDEO_PATH);
                Set<String> urls = parse(lastFile);
                Set<String> diff = new TreeSet<>();
                for (String url : urls) {
                    if (!files.contains(url.replace("https://download.ted.com/talks/", ""))) {
                        diff.add(url);
                    }
                }
                System.out.println("NEED TO DOWNLOAD:" + diff.size());
                System.out.println("################################################################################");
                diff.forEach(System.out::println);
                System.out.println("################################################################################");
                System.out.println("NEED TO DOWNLOAD:" + diff.size());
                future.complete();
            } catch (ParserConfigurationException | IOException | SAXException e) {
                future.fail(e);
            }
        }, res -> {
            if (res.failed()) {
                logger.log(Level.WARNING, "compare failed", res.cause());
            } else {
                logger.info("done");
            }
            vertx.close();
        });
    }

    private void start() {
        logger.info("app start");
        download(downloadResult -> {
            if (downloadResult.failed()) {
                logger.log(Level.WARNING, "download failed", downloadResult.cause());
            } else {
                byte[] body = downloadResult.result();
                if (body.length == lastFile.length && Arrays.hashCode(body) == Arrays.hashCode(lastFile)) {
                    logger.info("download success: duplicated");
                } else {
                    String version = System.currentTimeMillis() + "";
                    Path path = Paths.get(DOWNLOAD_DIR + "ted_" + version + ".xml");
                    try {
                        logger.info("save file: " + path);
                        Files.write(path, body);
                        lastFile = body;
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "save file failed", e);
                        return;
                    }
                    logger.info("download success:" + path);
                }

            }
            compareToDownload();
        });
    }

    private String getLastFile() throws IOException {
        SortedSet<String> set = new TreeSet<>();
        DirectoryStream<Path> d = Files.newDirectoryStream(Paths.get(DOWNLOAD_DIR));
        d.forEach(path -> {
            File file = path.toFile();
            if (file.getName().startsWith("ted_") && file.getName().endsWith(".xml")) {
                set.add(path.toFile().getAbsolutePath());
            }
        });
        if (set.size() == 0) return null;
        logger.info("get last file:" + set.last());
        return set.last();
    }

    private Set<String> parse(byte[] xml) throws IOException, SAXException, ParserConfigurationException {
        try {
            Set<String> l = new TreeSet<>();
            AtomicReference<DocumentBuilderFactory> factory = new AtomicReference<>(DocumentBuilderFactory.newInstance());
            DocumentBuilder builder = factory.get().newDocumentBuilder();
            ByteArrayInputStream is = new ByteArrayInputStream(xml);
            Document doc = builder.parse(is);
            Element e = doc.getDocumentElement();
            NodeList ns = e.getElementsByTagName("url");
            for (int i = 0; i < ns.getLength(); i++) {
                String url = ns.item(i).getTextContent();
                if (url.contains("download.ted.com")) {
                    l.add(url);
                }
            }
            return l;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.log(Level.WARNING, "parse xml failed");
            throw e;
        }
    }

    private void download(Handler<AsyncResult<byte[]>> handler) {
        logger.info("download url: " + DOWNLOAD_HOST + DOWNLOAD_URI);
        client.getNow(DOWNLOAD_HOST, DOWNLOAD_URI, resp -> {
            logger.info("download connected: " + resp.statusCode());
            logger.info("download body...");
            resp.bodyHandler(body -> {
                if (resp.statusCode() == 200) {
                    if (handler != null)
                        handler.handle(Future.succeededFuture(body.getBytes()));
                } else {
                    if (handler != null)
                        handler.handle(Future.failedFuture(body.toString()));
                }
            });
        });
    }
}
