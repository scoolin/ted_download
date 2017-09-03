package com.scoolin.ted_download;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author scoolin on 2017-09-03.
 */
class Parser {

    private final static Logger logger = Logger.getLogger(Parser.class.getName());

    public static void main(String[] args) throws Exception {
        String filename = Common.getLastFile();
        if (filename == null) {
            return;
        }
        Set<String> set = parse(Files.readAllBytes(Paths.get(filename)));
        set.forEach(System.out::println);
        System.out.println(set.size());
    }

    static Set<String> parse(byte[] xml) throws Exception {
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
            logger.log(Level.WARNING, "parse xml failed", e);
            throw new Exception("Parse xml failed. Check xml format.");
        }
    }

}
