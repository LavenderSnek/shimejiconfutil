package com.snek.shimejiconfutil.tools;

import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.util.ResourceUtil;
import com.snek.shimejiconfutil.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResourceInfo {

    public static Set<String> getImageSetDeps(Document doc, ConfigLang lang) {
        Set<String> ret = new HashSet<>();
        Consumer<Element> addDeps = (el) -> {
            var bm = el.getAttributeNode(lang.tr("BornMascot"));
            var tm = el.getAttributeNode(lang.tr("TransformMascot"));
            if (bm != null) {
                ret.add(bm.getValue());
            }
            if (tm != null) {
                ret.add(tm.getValue());
            }
        };
        XmlUtil.forEachElementWithTagName(doc, lang.tr("Action"), addDeps);
        XmlUtil.forEachElementWithTagName(doc, lang.tr("ActionReference"), addDeps);
        return ret;
    }

    public static Set<String> getMissingImages(Document doc, ConfigLang lang, Path imageSetDir) {
        var names = new HashSet<>(getImageNames(doc, lang));
        names.removeIf(s -> Files.isRegularFile(imageSetDir.resolve(s.replaceAll("^/+", ""))));
        return names;
    }

    public static Map<String, Set<Point>> getImageAnchorMap(Document doc, ConfigLang lang) {
        Map<String, Set<Point>> ret = new HashMap<>(64);

        ResourceUtil.forEachImageAttrIn(lang, doc, (leftAttr, rightAttr) -> {
            var anchorAttr = leftAttr.getOwnerElement().getAttributeNode(lang.tr("ImageAnchor"));
            if (anchorAttr == null) {
                return;
            }
            var imgName = leftAttr.getValue();
            if (!ret.containsKey(leftAttr.getValue())) {
                ret.put(imgName, new HashSet<>());
            }

            try {
                var anchorVal = anchorAttr.getValue().split(",", 2);
                var pt = new Point(Integer.parseInt(anchorVal[0]), Integer.parseInt(anchorVal[1]));
                ret.get(imgName).add(pt);
            } catch (Exception ignored) {}
        });

        return ret;
    }

    public static Map<String, Long> getImageFrequencyMap(Document doc, ConfigLang lang) {
        return getImageNames(doc,lang).stream()
                .collect(Collectors.groupingBy(Function.identity(), HashMap::new, Collectors.counting()));
    }

    public static Map<String, Long> getSoundFrequencyMap(Document doc, ConfigLang lang) {
        return getSoundNames(doc,lang).stream()
                .collect(Collectors.groupingBy(Function.identity(), HashMap::new, Collectors.counting()));
    }

    private static List<String> getSoundNames(Document doc, ConfigLang lang) {
        List<String> sounds = new ArrayList<>();
        ResourceUtil.forEachSoundAttrIn(lang, doc, sa -> sounds.add(sa.getValue()));
        return sounds;
    }

    private static List<String> getImageNames(Document doc, ConfigLang lang) {
        List<String> imgs = new ArrayList<>(120);
        ResourceUtil.forEachImageAttrIn(lang, doc, (l, r) -> {
            imgs.add(l.getValue());
            if (r != null) {
                imgs.add(r.getValue());
            }
        });
        return imgs;
    }

}
