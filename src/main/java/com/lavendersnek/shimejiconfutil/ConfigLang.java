package com.lavendersnek.shimejiconfutil;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.util.Locale;
import java.util.ResourceBundle;

public enum ConfigLang {

    EN(Locale.ENGLISH),
    JP(Locale.JAPANESE);

    private final ResourceBundle resourceBundle;

    ConfigLang(Locale locale) {
        this.resourceBundle = ResourceBundle.getBundle("schema", locale);
    }

    public ResourceBundle getRb() {
        return resourceBundle;
    }

    public String tr(String s) {
        return getRb().getString(s);
    }

    public static ConfigLang forDoc(Document doc) throws SAXException {
        for (ConfigLang lang : ConfigLang.values()) {
            var mascotTag = lang.tr("Mascot");
            if (doc.getDocumentElement().getTagName().equals(mascotTag)) {
                return lang;
            }
        }
        throw new SAXException("Unable to determine language.");
    }

}