package com.lavendersnek.shimejiconfutil.cli;

import com.lavendersnek.shimejiconfutil.ConfigLang;
import com.lavendersnek.shimejiconfutil.cli.bases.ActionsInOut;
import com.lavendersnek.shimejiconfutil.tools.ResourceRefactors;
import com.lavendersnek.shimejiconfutil.util.XmlUtil;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;

@Command(name = "shimejiconf", mixinStandardHelpOptions = true, description = "Various utilities for editing shimeji config files")
@SuppressWarnings("unused")
public class ConfMain {

    public static void main(String... args) {
        CommandLine commandLine = new CommandLine(new ConfMain())
                .addSubcommand("tr", new ConfTr())
                .addSubcommand("info", new ConfInfo());
        commandLine.execute(args);
    }

    @Command(description = "Makes separate copies of filenames that have multiple anchors. " +
                           "Does not consider asymmetry, use at your own risk and make backups.",
            mixinStandardHelpOptions = true)
    public void anchorcopy(
            @Mixin ActionsInOut actions,
            @Option(names = {"-img", "--image-set"}, defaultValue = ".", description = "Path to the image set folder.", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
                    Path imageSetDir
    ) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        System.err.println("[Refactoring] " + actions.getInPath());
        ResourceRefactors.separateImageAnchors(doc, lang, imageSetDir).entrySet().forEach(System.err::println);
        XmlUtil.writeDocToFile(doc, actions.getOutPath());
        System.err.println("[Output] " + actions.getOutPath());
    }

    @Command(description = "Sets the ImageAnchor text for all poses with the specified Image property. " +
                           "Does not consider ImageRight.",
            mixinStandardHelpOptions = true)
    public void imagereanchor(
            @Mixin ActionsInOut actions,
            @Option(names = {"-t", "--target"}, required = true, description = "Target Image text.")String target,
            @Option(names = {"-p", "--point"}, required = true, description = "New ImageAnchor text.") String anchorText
    ) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        System.err.println("[Refactoring] " + actions.getInPath());
        var ct = ResourceRefactors.setAnchorForImage(doc, lang, target, anchorText);
        System.err.println("Re-anchored " + ct + " occurrences: " + target + " : " + anchorText);
        XmlUtil.writeDocToFile(doc, actions.getOutPath());
        System.err.println("[Output] " + actions.getOutPath());
    }

    @Command(description = "Renames images in actions.xml (does not actually rename files).",
            mixinStandardHelpOptions = true)
    public void imagerename(
            @Mixin ActionsInOut actions,
            @Option(names = {"-t", "--target"}, required = true, description = "Target Image/ImageAnchor name.")String target,
            @Option(names = {"-n", "--name"}, required = true, description = "New name.") String name
    ) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        System.err.println("[Refactoring] " + actions.getInPath());
        var ct = ResourceRefactors.renameImage(doc, lang, target, name);
        System.err.println("Renamed " + ct + " occurrences: " + target + " -> " + name);
        XmlUtil.writeDocToFile(doc, actions.getOutPath());
        System.err.println("[Output] " + actions.getOutPath());
    }


    @Command(description = "Normalizes all the filenames (lowercase + path separators).",
            mixinStandardHelpOptions = true)
    public void clean(@Mixin ActionsInOut actions) throws SAXException, IOException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        System.err.println("[Cleaning] " + actions.getInPath());
        ResourceRefactors.cleanFilenames(doc, lang);
        XmlUtil.writeDocToFile(doc, actions.getOutPath());

        System.err.println("[Output] " + actions.getOutPath());
    }

    @Command(description = "Converts image sets using the old asymmetrical image support (shimeN-r.png) "
                           + "to use the ImageRight property instead.",
            mixinStandardHelpOptions = true)
    public void asymfix(
            @Mixin ActionsInOut actions,
            @Option(names = {"-img", "--image-set"}, defaultValue = ".", description = "Path to the image set folder.", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
                    Path imageSetDir
    ) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        System.err.println("[Refactoring] " + actions.getInPath());
        var res = ResourceRefactors.fixAsymmetry(doc, lang, imageSetDir);
        res.forEach((k, v) -> System.err.println(k + " → " + v));
        XmlUtil.writeDocToFile(doc, actions.getOutPath());

        System.err.println("[Output] " + actions.getOutPath());
    }

    @Command(description = "Removes relative sound paths (../../sound/).", mixinStandardHelpOptions = true)
    public void soundfix(@Mixin ActionsInOut actions) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);

        System.err.println("[Refactoring] " + actions.getInPath());
        var res = ResourceRefactors.fixRelativeSound(doc, lang);
        res.forEach((k, v) -> System.err.println(k + " → " + v));
        XmlUtil.writeDocToFile(doc, actions.getOutPath());
        System.err.println("[Output] " + actions.getOutPath());
    }

}
