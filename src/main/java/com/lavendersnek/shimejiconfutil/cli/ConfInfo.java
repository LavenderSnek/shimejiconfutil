package com.lavendersnek.shimejiconfutil.cli;

import com.lavendersnek.shimejiconfutil.ConfigLang;
import com.lavendersnek.shimejiconfutil.tools.ResourceInfo;
import com.lavendersnek.shimejiconfutil.util.XmlUtil;
import org.xml.sax.SAXException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

@Command(mixinStandardHelpOptions = true, description = "Utilities providing info on conf files.")
public class ConfInfo {

    private static class ActionsIn {
        @Option(names = {"-a", "--actions"}, description = "Path to the actions file.", required = true)
        private Path inPath;
    }

    @Command(description = "Lists all images used in an actions.xml file.", mixinStandardHelpOptions = true)
    public void lsimage(@Mixin ActionsIn ai) throws SAXException, IOException {
        var doc = XmlUtil.parseDoc(ai.inPath);
        var lang = ConfigLang.forDoc(doc);
        ResourceInfo.getImageFrequencyMap(doc, lang).entrySet().forEach(System.out::println);
    }

    @Command(description = "Lists all sounds used in an actions.xml file.", mixinStandardHelpOptions = true)
    public void lssound(@Mixin ActionsIn ai) throws SAXException, IOException {
        var doc = XmlUtil.parseDoc(ai.inPath);
        var lang = ConfigLang.forDoc(doc);
        ResourceInfo.getSoundFrequencyMap(doc, lang).entrySet().forEach(System.out::println);
    }

    @Command(description = "Lists all image set dependencies (Breed/Transform mascots) used in an actions.xml file.",
            mixinStandardHelpOptions = true)
    public void lsdeps(@Mixin ActionsIn ai) throws SAXException, IOException {
        var doc = XmlUtil.parseDoc(ai.inPath);
        var lang = ConfigLang.forDoc(doc);
        ResourceInfo.getImageSetDeps(doc, lang).forEach(System.out::println);
    }

}
