package com.snek.shimejiconfutil.cli;


import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.cli.bases.ActionsInOut;
import com.snek.shimejiconfutil.util.ResourceUtil;
import com.snek.shimejiconfutil.util.XmlUtil;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static picocli.CommandLine.*;

@Command(mixinStandardHelpOptions = true, description = """
        Utilities for image and sound resources""")
public class ConfResources {
    // most of these are untested

    @Command(description = "Normalizes all the filenames.")
    public void clean(@Mixin ActionsInOut actions) throws SAXException, IOException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        if (lang == null) {
            throw new SAXException("Unable to determine language of: " + actions.getInPath());
        }

        ResourceUtil.forEachImageAttrIn(lang, doc, (left, right) -> {
            var cleanLeft = ResourceUtil.cleanFilename(left.getValue());
            left.setValue("/" + cleanLeft);

            if (right != null) {
                var cleanRight = ResourceUtil.cleanFilename(right.getValue());
                right.setValue(cleanRight);
            }
        });

        ResourceUtil.forEachSoundAttrIn(lang, doc, sa -> {
            var cleaned = ResourceUtil.cleanFilename(sa.getValue());
            sa.setValue("/" + cleaned);
        });

        XmlUtil.writeDocToFile(doc, actions.getOutPath());
    }

    @Command(description = "Converts image sets using the old asymmetrical image support (shimeN-r.png) " +
                           "to use the ImageRight property instead.")
    public void asymfix(
            @Mixin ActionsInOut actions,
            @Option(names = {"-img", "--image-set"}, defaultValue = ".", description = "Path to the image set folder.", showDefaultValue = Help.Visibility.ALWAYS)
            Path imageSetDir
    ) throws IOException, SAXException, TransformerException {

        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        if (lang == null) {
            throw new SAXException("Unable to determine language of: " + actions.getInPath());
        }

        ResourceUtil.forEachPoseElementIn(lang, doc, poseEl -> {

            var imgAttr = poseEl.getAttributeNode(lang.getRb().getString("Image"));
            if (imgAttr == null) {
                return;
            }

            var cleanLeft = ResourceUtil.cleanFilename(imgAttr.getValue());
            imgAttr.setValue("/" + cleanLeft);

            var rightImgAttr = poseEl.getAttributeNode(lang.getRb().getString("ImageRight"));
            if (rightImgAttr != null) {
                var cleanRight = ResourceUtil.cleanFilename(rightImgAttr.getValue());
                rightImgAttr.setValue("/" + cleanRight);
                return;
            }

            var cleanRight = cleanLeft.replaceAll("\\.[a-zA-Z]+$", "-r$0");
            if (Files.isRegularFile(imageSetDir.resolve(cleanRight))) {
                poseEl.setAttribute(lang.getRb().getString("ImageRight"), "/" + cleanRight);
            }

        });

        XmlUtil.writeDocToFile(doc, actions.getOutPath());
    }

    @Command(description = "Removes relative sound paths (../../sound/)")
    public void soundfix(@Mixin ActionsInOut actions) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        if (lang == null) {
            throw new SAXException("Unable to determine language of: " + actions.getInPath());
        }

        ResourceUtil.forEachSoundAttrIn(lang, doc, sa -> {
            var cleaned = ResourceUtil.cleanFilename(sa.getValue());
            if (cleaned.startsWith("../../sound/")) {
                cleaned = cleaned.replaceFirst("\\.\\./\\.\\./sound/", "");
            }

            sa.setValue("/" + cleaned);
        });

        XmlUtil.writeDocToFile(doc, actions.getOutPath());
    }

}
