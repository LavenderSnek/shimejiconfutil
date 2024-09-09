package com.lavendersnek.shimejiconfutil.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class XmlUtil {

    private static DocumentBuilder docBuilder;
    private static Transformer transformer;

    static {
        try {
            docBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private XmlUtil() {
    }

    public static Document parseDoc(Path path) throws IOException, SAXException {
        return docBuilder.parse(path.toFile());
    }

    public static void writeDocToFile(Document doc, Path pathToFile) throws TransformerException {
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(pathToFile.toFile());
        transformer.transform(source, result);
    }

    public static void forEachAttrIn(Element el, Consumer<Attr> attrConsumer) {
        var attrs = el.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            var node = attrs.item(i);
            if (node instanceof Attr attr) {
                attrConsumer.accept(attr);
            }
        }
    }

    public static void forEachElementIn(Document doc, Consumer<Element> elementConsumer) {
        forEachElementWithTagName(doc, "*", elementConsumer);
    }

    public static void forEachElementWithTagName(Document doc, String targetTagName, Consumer<Element> elementConsumer) {
        var elements = doc.getElementsByTagName(targetTagName);
        for (int i = 0; i < elements.getLength(); i++) {
            var node = elements.item(i);
            if (node instanceof Element el) {
                elementConsumer.accept(el);
            }
        }
    }

}
