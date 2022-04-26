package com.snek.shimejiconfutil.cli;

import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.cli.bases.ActionsInOut;
import com.snek.shimejiconfutil.tools.ResourceRefactors;
import com.snek.shimejiconfutil.util.XmlUtil;
import org.xml.sax.SAXException;
import picocli.CommandLine;

import picocli.CommandLine.Command;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;

@Command(name = "shimejiconf", mixinStandardHelpOptions = true, description = "Various utilities for editing shimeji config files")
public class ConfMain {

    public static void main(String... args) {
        CommandLine commandLine = new CommandLine(new ConfMain()).addSubcommand("tr", new ConfTr());

        commandLine.execute(args);
    }

    @Command(description = "Makes copies of files with different anchors and refactors xml files.", mixinStandardHelpOptions = true)
    public void anchorcopy(@CommandLine.Mixin ActionsInOut actions, @CommandLine.Option(names = {"-img", "--image-set"}, defaultValue = ".", description = "Path to the image set folder.", showDefaultValue = CommandLine.Help.Visibility.ALWAYS) Path imageSetDir) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);
        if (lang == null) {
            throw new SAXException("Unable to determine language.");
        }

        System.err.println("[Refactoring] " + actions.getInPath());
        ResourceRefactors.separateImageAnchors(doc, lang, imageSetDir).entrySet().forEach(System.out::println);
        XmlUtil.writeDocToFile(doc, actions.getOutPath());
        System.err.println("[Output] " + actions.getOutPath());
    }

    @Command(description = "Normalizes all the filenames (lowercase + path separators).", mixinStandardHelpOptions = true)
    public void clean(@CommandLine.Mixin ActionsInOut actions) throws SAXException, IOException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);
        if (lang == null) {
            throw new SAXException("Unable to determine language.");
        }

        System.err.println("[Cleaning] " + actions.getInPath());
        ResourceRefactors.cleanFilenames(doc, lang);
        XmlUtil.writeDocToFile(doc, actions.getOutPath());

        System.err.println("[Output] " + actions.getOutPath());
    }

    @Command(description = "Converts image sets using the old asymmetrical image support (shimeN-r.png) " + "to use the ImageRight property instead.", mixinStandardHelpOptions = true)
    public void asymfix(@CommandLine.Mixin ActionsInOut actions, @CommandLine.Option(names = {"-img", "--image-set"}, defaultValue = ".", description = "Path to the image set folder.", showDefaultValue = CommandLine.Help.Visibility.ALWAYS) Path imageSetDir) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);
        if (lang == null) {
            throw new SAXException("Unable to determine language.");
        }

        System.err.println("[Refactoring] " + actions.getInPath());
        var res = ResourceRefactors.fixAsymmetry(doc, lang, imageSetDir);
        res.forEach((k, v) -> System.err.println(k + " → " + v));
        XmlUtil.writeDocToFile(doc, actions.getOutPath());

        System.err.println("[Output] " + actions.getOutPath());
    }

    @Command(description = "Removes relative sound paths (../../sound/).", mixinStandardHelpOptions = true)
    public void soundfix(@CommandLine.Mixin ActionsInOut actions) throws IOException, SAXException, TransformerException {
        var doc = XmlUtil.parseDoc(actions.getInPath());
        var lang = ConfigLang.forDoc(doc);
        if (lang == null) {
            throw new SAXException("Unable to determine language.");
        }

        System.err.println("[Refactoring] " + actions.getInPath());
        var res = ResourceRefactors.fixRelativeSound(doc, lang);
        res.forEach((k, v) -> System.err.println(k + " → " + v));
        XmlUtil.writeDocToFile(doc, actions.getOutPath());
        System.err.println("[Output] " + actions.getOutPath());
    }

}
