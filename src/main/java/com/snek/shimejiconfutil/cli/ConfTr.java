package com.snek.shimejiconfutil.cli;


import com.snek.shimejiconfutil.ConfLangTranslator;
import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.util.MiscUtil;
import com.snek.shimejiconfutil.util.XmlUtil;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(mixinStandardHelpOptions = true, description = """
        Partially converts JP shimeji config to EN. Manual touch-up is often required.
        Untranslated scripts are prefixed with "???". This ensures that possibly broken scripts are reviewed""")
class ConfTr implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, required = true, description = "Files to be translated. Any files with the same basename will be overwritten in the output directory.")
    Path[] confPaths;

    @Option(names = {"-d", "--dest-dir"}, description = "Output directory for the translated files.", defaultValue = "_translated", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    Path outputDir;

    @Option(names = {"-s", "--script-tr"}, description = "UTF-8 properties files with only script content translations (#{} and $${} removed)")
    Path[] scriptTrProperties = {};

    @Option(names = {"-a", "--action-tr"}, description = "UTF-8 properties files with translations for action/behaviour names. Any keys already available in the JP schema will be ignored.")
    Path[] actionTrProperties = {};

    private static Map<String, String> combinePropertyFiles(Path[] paths) {
        Map<String, String> ret = new HashMap<>();

        for (Path sp : paths) {
            if (Files.isRegularFile(sp)) {
                ret.putAll(MiscUtil.propertiesToMap(sp));
                System.err.println("Read file:" + sp);
            } else {
                System.err.println("Not a file:" + sp);
            }
        }

        return ret;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Reading translation properties");

        Files.createDirectories(outputDir);

        var scriptTr = MiscUtil.getPropertiesMapFromJar("tr-scripts.properties");
        scriptTr.putAll(combinePropertyFiles(scriptTrProperties));

        var actionsTr = MiscUtil.getPropertiesMapFromJar("tr-behaviornames.properties");
        actionsTr.putAll(combinePropertyFiles(actionTrProperties));

        System.err.println();

        var translator = createJpToEnTranslator(scriptTr, actionsTr);

        for (Path confPath : confPaths) {
            try {
                translateAndWrite(confPath, outputDir.resolve(confPath.getFileName()), translator);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.err.println("[Unable to translate] " + confPath + "\n");
            }
        }

        return 0;
    }

    static void translateAndWrite(Path inPath, Path outPath, ConfLangTranslator translator) throws IOException, SAXException, TransformerException {

        var doc = XmlUtil.parseDoc(inPath);

        System.err.println("[Translating] " + inPath);
        var warns = translator.translate(doc);

        // print error
        for (ConfLangTranslator.Warning warning : warns.keySet()) {
            var prefix = "[" + warning.name() + "] ";
            warns.get(warning).forEach(s -> {
                var fs = Ansi.AUTO.string("@|red " + prefix + s + "|@");
                System.err.println(fs);
            });
        }

        XmlUtil.writeDocToFile(doc, outPath);

        System.err.println("[Translated] " + inPath);
        System.err.println("[Output] " + outPath + "\n");
    }

    private static ConfLangTranslator createJpToEnTranslator(Map<String, String> scriptTr, Map<String, String> actionTr) {
        var jpRb = ConfigLang.JP.getRb();
        var enRb = ConfigLang.EN.getRb();

        var jpKeys = new HashSet<>(jpRb.keySet());
        jpKeys.removeIf(k -> jpRb.getString(k).equals(enRb.getString(k)));

        Map<String, String> renameMap = new HashMap<>();
        for (String key : jpKeys) {
            renameMap.put(jpRb.getString(key), enRb.getString(key));
        }

        return new ConfLangTranslator(renameMap, scriptTr, actionTr);
    }

}
