package com.snek.shimejiconfutil.tools;

import com.snek.shimejiconfutil.ConfLangTranslator;
import com.snek.shimejiconfutil.ConfigLang;
import com.snek.shimejiconfutil.util.MiscUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JpEnTranslate {

    public static ConfLangTranslator createJpToEnTranslator(Map<String, String> addedScriptTr, Map<String, String> addedActionTr) {
        var scriptTr = MiscUtil.getPropertiesMapFromJar("tr-scripts.properties");
        scriptTr.putAll(addedScriptTr);

        var actionTr = MiscUtil.getPropertiesMapFromJar("tr-behaviornames.properties");
        actionTr.putAll(addedActionTr);

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
