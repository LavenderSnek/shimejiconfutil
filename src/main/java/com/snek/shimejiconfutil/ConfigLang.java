package com.snek.shimejiconfutil;

import org.w3c.dom.Document;

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

    public static ConfigLang forDoc(Document doc) {
        for (ConfigLang lang : ConfigLang.values()) {
            var mascotTag = lang.getRb().getString("Mascot");
            if (doc.getDocumentElement().getTagName().equals(mascotTag)) {
                return lang;
            }
        }
        return null;
    }

}