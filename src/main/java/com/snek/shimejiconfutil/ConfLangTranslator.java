package com.snek.shimejiconfutil;

import com.snek.shimejiconfutil.util.XmlUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfLangTranslator {

    private final Map<String, String> xmlRenameMap;
    private final List<String> xmlRenameKeys;

    private final Map<String, String> scriptTranslations;

    private final Map<String, String> actionTranslations;
    private final List<String> actionTranslationsKeys;

    public enum Warning {
        UNTRANSLATED_SCRIPT, ACTION_IN_SCRIPT
    }

    public ConfLangTranslator(Map<String, String> xmlRenameMap, Map<String, String> scriptTranslations, Map<String, String> actionsTranslations) {
        this.xmlRenameMap = xmlRenameMap;
        this.xmlRenameKeys = xmlRenameMap.keySet().stream().toList();
        this.scriptTranslations = scriptTranslations;
        this.actionTranslations = actionsTranslations;
        this.actionTranslationsKeys = actionsTranslations.keySet().stream().toList();
    }

    private static Map<Warning, List<String>> createWarnMap() {
        Map<Warning, List<String>> ret = new HashMap<>(4);

        for (Warning w : Warning.values()) {
            ret.put(w, new ArrayList<>());
        }

        return ret;
    }

    public Map<Warning, List<String>> translate(Document doc) {
        renameXmlElements(doc);

        var ret = createWarnMap();

        // param values - not including names
        XmlUtil.forEachElementIn(doc, el -> {
            if (el.getTagName().equals(xmlRenameMap.getOrDefault("Pose", "Pose"))) {
                return;
            }
            XmlUtil.forEachAttrIn(el, attr -> {
                Map<Warning, List<String>> warn = translateAttrValue(attr);
                for (Warning w : warn.keySet()) {
                    ret.get(w).addAll(warn.get(w));
                }
            });
        });

        return ret;
    }

    private Map<Warning, List<String>> translateAttrValue(Attr attr) {
        var av = attr.getValue().trim();

        var ret = createWarnMap();

        if ((av.startsWith("${") || av.startsWith("#{")) && av.endsWith("}")) {
            var script = av.substring(2, av.length() - 1).trim();

            if (scriptTranslations.containsKey(script)) {
                attr.setValue(av.charAt(0) + "{" + scriptTranslations.get(script) + "}");
            } else if (xmlRenameKeys.stream().anyMatch(script::contains)) {
                ret.get(Warning.UNTRANSLATED_SCRIPT).add(av);
                attr.setValue("???" + av); // breaks it on purpose, so it can't just be ignored
            } else if (actionTranslationsKeys.stream().anyMatch(script::contains)) {
                ret.get(Warning.ACTION_IN_SCRIPT).add(av);
                System.err.println("ScriptMayContainActionName:" + av);
            }
        } else {
            var actVal = actionTranslations.getOrDefault(av, av);
            attr.setValue(xmlRenameMap.getOrDefault(av, actVal));
        }

        return  ret;
    }

    private void renameXmlElements(Document doc) {
        XmlUtil.forEachElementIn(doc, el -> {
            if (xmlRenameMap.containsKey(el.getTagName())) {
                doc.renameNode(el, null, xmlRenameMap.get(el.getTagName()));
            }

            XmlUtil.forEachAttrIn(el, attr -> {
                var name = attr.getName();
                if (xmlRenameMap.containsKey(name)) {
                    el.setAttribute(xmlRenameMap.get(name), attr.getValue());
                    el.removeAttribute(name);
                }
            });

            // I don't know why this doesn't work with getAttributes
            if (xmlRenameMap.containsKey("IEの端Y") && el.hasAttribute("IEの端Y")) {
                var ieY = el.getAttributeNode("IEの端Y").getValue();
                el.setAttribute(xmlRenameMap.get("IEの端Y"), ieY);
                el.removeAttribute("IEの端Y");
            }
        });
    }

}