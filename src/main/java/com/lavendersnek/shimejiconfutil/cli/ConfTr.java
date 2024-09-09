package com.lavendersnek.shimejiconfutil.cli;


import com.lavendersnek.shimejiconfutil.ConfLangTranslator;
import com.lavendersnek.shimejiconfutil.tools.JpEnTranslate;
import com.lavendersnek.shimejiconfutil.util.MiscUtil;
import com.lavendersnek.shimejiconfutil.util.XmlUtil;
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
import java.util.Map;
import java.util.concurrent.Callable;

@Command(mixinStandardHelpOptions = true,
        description = "Partially converts JP shimeji config to EN. Manual touch-up is often required. " +
                      "Untranslated scripts are prefixed with \"???\". This ensures that possibly broken scripts are reviewed")
class ConfTr implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, required = true, description = "Files to be translated. Any files with the same basename will be overwritten in the output directory.")
    Path[] confPaths;

    @Option(names = {"-d", "--dest-dir"}, description = "Output directory for the translated files.", defaultValue = "_translated", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    Path outputDir;

    @Option(names = {"-tS", "--script-tr"}, description = "UTF-8 properties files with only script content translations (#{} and $${} removed)")
    Path[] scriptTrProperties = {};

    @Option(names = {"-tA", "--action-tr"}, description = "UTF-8 properties files with translations for action/behaviour names. Any keys already available in the JP schema will be ignored.")
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
    public Integer call() {
        try {
            Files.createDirectories(outputDir);
        } catch (Exception e) {
            System.err.println("Unable to make dest dir.");
            System.err.println(e.getMessage());
            return 1;
        }

        System.out.println("Reading translation properties");

        var addedScriptTr = combinePropertyFiles(scriptTrProperties);
        var addedActionTr = combinePropertyFiles(actionTrProperties);

        System.err.println();

        var translator = JpEnTranslate.createJpToEnTranslator(addedScriptTr, addedActionTr);

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

    private static void translateAndWrite(Path inPath, Path outPath, ConfLangTranslator translator) throws IOException, SAXException, TransformerException {

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

}
