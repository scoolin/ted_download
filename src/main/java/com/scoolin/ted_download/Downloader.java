package com.scoolin.ted_download;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.impl.VertxInternal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.scoolin.ted_download.Common.*;

/**
 * @author scoolin on 2017-08-31.
 */
public class Downloader {

    private final static Logger logger = Logger.getLogger(Downloader.class.getName());
    private final Vertx vertx;
    private final HttpClient client;
    private byte[] lastXml;

    public Downloader(Vertx vertx) {
        this.vertx = vertx;
        this.client = vertx.createHttpClient();
        VertxInternal vi = (VertxInternal) vertx;
        vi.addCloseHook(h -> {
            client.close();
            h.handle(Future.succeededFuture());
        });
        try {
            String file = Common.getLastFile();
            if (file != null) {
                logger.info("get last file:" + file);
                this.lastXml = Files.readAllBytes(Paths.get(file));
            } else {
                this.lastXml = new byte[0];
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "get last file failed", e);
        }
    }


    private void compareToDownload(byte[] xml) {
        vertx.executeBlocking(future -> {
            try {
                Set<String> files = Smb.getAllDownloadedTedVideos();
                Set<String> urls = Parser.parse(xml);
                Set<String> diff = Comparer.diff(urls, files);
                System.out.println("NEED TO DOWNLOAD:" + diff.size());
                System.out.println("################################################################################");
                diff.forEach(System.out::println);
                System.out.println("################################################################################");
                System.out.println("NEED TO DOWNLOAD:" + diff.size());
                future.complete();
            } catch (Exception e) {
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

    public void start() {
        logger.info("app start");
        download(downloadResult -> {
            if (downloadResult.failed()) {
                logger.log(Level.WARNING, "download failed", downloadResult.cause());
            } else {
                byte[] body = downloadResult.result();
                if (body.length == lastXml.length && Arrays.hashCode(body) == Arrays.hashCode(lastXml)) {
                    logger.info("download success: duplicated");
                } else {
                    String version = System.currentTimeMillis() + "";
                    Path path = Paths.get(DOWNLOAD_DIR + "ted_" + version + ".xml");
                    try {
                        logger.info("save file: " + path);
                        Files.write(path, body);
                        lastXml = body;
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "save file failed", e);
                        return;
                    }
                    logger.info("download success:" + path);
                }

            }
            compareToDownload(lastXml);
        });
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
