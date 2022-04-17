package com.snek.shimejiconfutil.tools;

import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.util.ResourceUtil;
import org.w3c.dom.Document;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ResourceRefactors {

    public static Map<String, String> fixAsymmetry(Document doc, ConfigLang lang, Path imageSetDir) {
        Map<String, String> ret = new HashMap<>(60);

        ResourceUtil.forEachPoseElementIn(lang, doc, poseEl -> {
            var imgAttr = poseEl.getAttributeNode(lang.tr("Image"));
            if (imgAttr == null) {
                return;
            }

            var cleanLeft = ResourceUtil.cleanFilename(imgAttr.getValue());
            imgAttr.setValue("/" + cleanLeft);

            ret.put(cleanLeft, null);

            var rightImgAttr = poseEl.getAttributeNode(lang.tr("ImageRight"));
            if (rightImgAttr != null) {
                var cleanRight = ResourceUtil.cleanFilename(rightImgAttr.getValue());
                rightImgAttr.setValue("/" + cleanRight);
                ret.put(cleanLeft, cleanRight);
                return;
            }

            var cleanRight = cleanLeft.replaceAll("\\.[a-zA-Z]+$", "-r$0");
            if (Files.isRegularFile(imageSetDir.resolve(cleanRight))) {
                poseEl.setAttribute(lang.tr("ImageRight"), "/" + cleanRight);
                ret.put(cleanLeft, cleanRight);
            }
        });

        return ret;
    }


    public static Map<String, String> fixRelativeSound(Document doc, ConfigLang lang) {
        Map<String, String> ret = new HashMap<>(10);

        ResourceUtil.forEachSoundAttrIn(lang, doc, sa -> {
            var cleaned = ResourceUtil.cleanFilename(sa.getValue());
            var result = cleaned;

            if (cleaned.startsWith("../../sound/")) {
                result = cleaned.replaceFirst("\\.\\./\\.\\./sound/", "");
                ret.put(cleaned, result);
            }

            sa.setValue("/" + result);
        });

        return ret;
    }

    public static void cleanFilenames(Document doc, ConfigLang lang) {
        ResourceUtil.forEachImageAttrIn(lang, doc, (left, right) -> {
            var cleanLeft = ResourceUtil.cleanFilename(left.getValue());
            left.setValue("/" + cleanLeft);

            if (right != null) {
                var cleanRight = ResourceUtil.cleanFilename(right.getValue());
                right.setValue("/" + cleanRight);
            }
        });

        ResourceUtil.forEachSoundAttrIn(lang, doc, sa -> {
            var cleaned = ResourceUtil.cleanFilename(sa.getValue());
            sa.setValue("/" + cleaned);
        });
    }

}
