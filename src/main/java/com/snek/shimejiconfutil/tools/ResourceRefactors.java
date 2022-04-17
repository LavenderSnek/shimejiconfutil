package com.snek.shimejiconfutil.tools;

import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.util.ResourceUtil;
import org.w3c.dom.Document;

import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceRefactors {

    public static void fixAsymmetry(Document doc,ConfigLang lang, Path imageSetDir) {
        ResourceUtil.forEachPoseElementIn(lang, doc, poseEl -> {
            var imgAttr = poseEl.getAttributeNode(lang.tr("Image"));
            if (imgAttr == null) {
                return;
            }

            var cleanLeft = ResourceUtil.cleanFilename(imgAttr.getValue());
            imgAttr.setValue("/" + cleanLeft);

            var rightImgAttr = poseEl.getAttributeNode(lang.tr("ImageRight"));
            if (rightImgAttr != null) {
                var cleanRight = ResourceUtil.cleanFilename(rightImgAttr.getValue());
                rightImgAttr.setValue("/" + cleanRight);
                return;
            }

            var cleanRight = cleanLeft.replaceAll("\\.[a-zA-Z]+$", "-r$0");
            if (Files.isRegularFile(imageSetDir.resolve(cleanRight))) {
                poseEl.setAttribute(lang.tr("ImageRight"), "/" + cleanRight);
            }
        });
    }


    public static void fixRelativeSound(Document doc, ConfigLang lang) {
        ResourceUtil.forEachSoundAttrIn(lang, doc, sa -> {
            var cleaned = ResourceUtil.cleanFilename(sa.getValue());
            if (cleaned.startsWith("../../sound/")) {
                cleaned = cleaned.replaceFirst("\\.\\./\\.\\./sound/", "");
            }

            sa.setValue("/" + cleaned);
        });
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
