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
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceRefactors {

    /**
     * Warning! this modifies files.
     */
    public static Map<String, String> separateImageAnchors(Document doc, ConfigLang lang, Path imageSetDir) throws IOException {
        Map<String, Set<Point>> pointMap = new HashMap<>(64);

        ResourceUtil.forEachImageAttrIn(lang, doc, (leftAttr, rightAttr) -> {
            var anchorAttr = leftAttr.getOwnerElement().getAttributeNode(lang.tr("ImageAnchor"));
            if (anchorAttr == null) {
                return;
            }
            var imgName = leftAttr.getValue();
            if (!pointMap.containsKey(leftAttr.getValue())) {
                pointMap.put(imgName, new HashSet<>());
            }

            var anchorVal = anchorAttr.getValue().split(",", 2);
            var pt = new Point(Integer.parseInt(anchorVal[0]), Integer.parseInt(anchorVal[1]));
            pointMap.get(imgName).add(pt);
        });

        Map<String, String> copyMap = new HashMap<>();

        for (var entry : pointMap.entrySet()) {
            ResourceUtil.forEachImageAttrIn(lang, doc, (leftAttr, rightAttr) -> {
                var anchorAttr = leftAttr.getOwnerElement().getAttributeNode(lang.tr("ImageAnchor"));
                if (anchorAttr == null) {
                    return;
                }
                var imgName = leftAttr.getValue();

                if (entry.getKey().equals(imgName) && entry.getValue().size() > 1) {
                    var anchorVal = anchorAttr.getValue().split(",", 2);
                    var pt = new Point(Integer.parseInt(anchorVal[0]), Integer.parseInt(anchorVal[1]));
                    var newName = imgName.split("\\.[a-zA-Z]+$")[0] + "_" + pt.x + "x" + pt.y + ".png";
                    leftAttr.setValue(newName);
                    copyMap.put(newName, imgName);
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

    public static int setAnchorForImage(Document doc, ConfigLang lang, String targetLeftImage, String newAnchorText) {
        AtomicInteger ct = new AtomicInteger();

        ResourceUtil.forEachImageAttrIn(lang, doc, (leftAttr, rightAttr) -> {
            if (leftAttr.getValue().equals(targetLeftImage)) {
                leftAttr.getOwnerElement().setAttribute(lang.tr("ImageAnchor"), newAnchorText);
                ct.addAndGet(1);
            }
        });

        return ct.intValue();
    }

    public static int renameImage(Document doc, ConfigLang lang, String originalName, String newName) {
        AtomicInteger ct = new AtomicInteger();

        ResourceUtil.forEachImageAttrIn(lang, doc, (leftAttr, rightAttr) -> {
            if (leftAttr.getValue().equals(originalName)) {
                leftAttr.setValue(newName);
                ct.addAndGet(1);
            }
            if (rightAttr != null && rightAttr.getValue().equals(originalName)) {
                rightAttr.setValue(newName);
                ct.addAndGet(1);
            }
        });

        return ct.intValue();
    }


    public static Map<String, String> fixAsymmetry(Document doc, ConfigLang lang, Path imageSetDir) {
        Map<String, String> ret = new HashMap<>(60);

        ResourceUtil.forEachImageAttrIn(lang, doc, (leftAttr, rightAttr) -> {
            if (rightAttr != null) {
                return;
            }

            var possibleCleanRight = leftAttr.getValue()
                    .replaceAll("\\.[a-zA-Z]+$", "-r$0")
                    .replaceAll("^/+", "");

            if (Files.isRegularFile(imageSetDir.resolve(possibleCleanRight))) {
                leftAttr.getOwnerElement().setAttribute(lang.tr("ImageRight"), "/" + possibleCleanRight);
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
