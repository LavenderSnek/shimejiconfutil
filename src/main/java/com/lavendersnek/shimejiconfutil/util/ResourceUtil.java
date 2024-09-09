package com.lavendersnek.shimejiconfutil.util;

import com.lavendersnek.shimejiconfutil.ConfigLang;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ResourceUtil {

    public static void forEachSoundAttrIn(ConfigLang lang, Document doc, Consumer<Attr> soundAttrConsumer) {
        forEachPoseElementIn(lang, doc, el -> {
            var soundAttr = el.getAttributeNode(lang.tr("Sound"));
            if (soundAttr != null) {
                soundAttrConsumer.accept(soundAttr);
            }
        });
    }

    public static void forEachImageAttrIn(ConfigLang lang, Document doc, BiConsumer<Attr, Attr> imageAttrConsumer) {
        forEachPoseElementIn(lang, doc, el -> {
            var imgAttr = el.getAttributeNode(lang.tr("Image"));
            if (imgAttr == null) {
                return;
            }

            var rightImageAttr = el.getAttributeNode(lang.tr("ImageRight"));
            imageAttrConsumer.accept(imgAttr, rightImageAttr);
        });
    }

    public static void forEachPoseElementIn(ConfigLang lang, Document doc, Consumer<Element> poseConsumer) {
        XmlUtil.forEachElementWithTagName(doc, lang.tr("Pose"), poseConsumer);
    }

    public static String cleanFilename(String in) {
        return in.toLowerCase(Locale.ROOT)
                .replaceAll("\\\\", "/")
                .replaceAll("^/+", "");
    }

}
