package com.snek.shimejiconfutil.tools;

import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.util.ResourceUtil;
import org.w3c.dom.Document;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceRefactors {

    /**
     * Warning! this modifies files.
     */
    public static Map<String, String> separateImageAnchors(Document doc, ConfigLang lang, Path imageSetDir) throws IOException {
        Map<String, Set<Point>> pointMap = new HashMap<>(64);

        // i know this is awful but it works
        ResourceUtil.forEachPoseElementIn(lang, doc, poseEl -> {
            var imgAttr = poseEl.getAttributeNode(lang.tr("Image"));
            var anchorAttr = poseEl.getAttributeNode(lang.tr("ImageAnchor"));
            if (imgAttr == null || anchorAttr == null) {
                return;
            }

            var imgKey = imgAttr.getValue();
            var anchorVal = anchorAttr.getValue().split(",", 2);
            var pt = new Point(Integer.parseInt(anchorVal[0]), Integer.parseInt(anchorVal[1]));

            if (!pointMap.containsKey(imgKey)) {
                pointMap.put(imgKey, new HashSet<>());
            }

            pointMap.get(imgKey).add(pt);
        });

        Map<String, String> copyMap = new HashMap<>();

        for (var entry : pointMap.entrySet()) {
            ResourceUtil.forEachPoseElementIn(lang, doc, poseEl -> {
                var imgAttr = poseEl.getAttributeNode(lang.tr("Image"));
                var anchorAttr = poseEl.getAttributeNode(lang.tr("ImageAnchor"));
                if (imgAttr == null || anchorAttr == null) {
                    return;
                }
                var imgKey = imgAttr.getValue();
                var anchorVal = anchorAttr.getValue().split(",", 2);
                var pt = new Point(Integer.parseInt(anchorVal[0]), Integer.parseInt(anchorVal[1]));

                if (entry.getKey().equals(imgKey) && entry.getValue().size() > 1) {
                    var nv = imgKey.split("\\.[a-zA-Z]+$")[0];
                    nv += "_" + pt.x + "x" + pt.y + ".png";
                    copyMap.put(nv, imgKey);
                    imgAttr.setValue(nv);
                }
            });
        }

        for (Map.Entry<String, String> entry : copyMap.entrySet()) {
            var srcPath = imageSetDir.resolve(entry.getValue().replaceAll("^/+", ""));
            var dstPath = imageSetDir.resolve(entry.getKey().replaceAll("^/+", ""));
            if (Files.isRegularFile(srcPath)) {
                Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return copyMap;
    }


    public static Map<String, String> fixAsymmetry(Document doc, ConfigLang lang, Path imageSetDir) {
        Map<String, String> ret = new HashMap<>(60);

        ResourceUtil.forEachPoseElementIn(lang, doc, poseEl -> {
            var imgAttr = poseEl.getAttributeNode(lang.tr("Image"));
            if (imgAttr == null) {
                return;
            }
            var rightImgAttr = poseEl.getAttributeNode(lang.tr("ImageRight"));
            if (rightImgAttr != null) {
                return;
            }

            var left = imgAttr.getValue();

            var possibleRight = left
                    .replaceAll("\\.[a-zA-Z]+$", "-r$0")
                    .replaceAll("^/+", "");

            if (Files.isRegularFile(imageSetDir.resolve(possibleRight))) {
                poseEl.setAttribute(lang.tr("ImageRight"), "/" + possibleRight);
                ret.put(left, possibleRight);
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
