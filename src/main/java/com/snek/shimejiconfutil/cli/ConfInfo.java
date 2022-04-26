package com.snek.shimejiconfutil.cli;

import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.util.ResourceUtil;
import com.snek.shimejiconfutil.util.XmlUtil;
import org.xml.sax.SAXException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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

        Set<String> imgs = new HashSet<>(64);
        ResourceUtil.forEachImageAttrIn(lang, doc, (l, r) -> {
            imgs.add(l.getValue());
            if (r != null) {
                imgs.add(r.getValue());
            }
        });
        imgs.forEach(System.out::println);
    }

    @Command(description = "Lists all sounds used in an actions.xml file.", mixinStandardHelpOptions = true)
    public void lssound(@Mixin ActionsIn ai) throws SAXException, IOException {
        var doc = XmlUtil.parseDoc(ai.inPath);
        var lang = ConfigLang.forDoc(doc);

        Set<String> sounds = new HashSet<>(64);
        ResourceUtil.forEachSoundAttrIn(lang, doc, sa -> sounds.add(sa.getValue()));
        sounds.forEach(System.out::println);
    }


}
