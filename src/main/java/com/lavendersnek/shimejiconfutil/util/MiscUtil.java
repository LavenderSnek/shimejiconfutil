package com.lavendersnek.shimejiconfutil.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MiscUtil {

    public static Map<String, String> propertiesToMap(Path propsPath) {
        Properties props = new Properties();

        if (propsPath != null && Files.isRegularFile(propsPath)) {
            try (var ins = new InputStreamReader(new FileInputStream(propsPath.toString()), StandardCharsets.UTF_8)) {
                props.load(ins);
            } catch (IOException ignored) {
            }
        }

        Map<String, String> ret = new HashMap<>();
        props.forEach((k, v) -> ret.put((String) k, (String) v));

        return ret;
    }

    public static Map<String, String> getPropertiesMapFromJar(String filename) {
        Properties props = new Properties();

        var stream = MiscUtil.class.getResourceAsStream("/" + filename);

        if (stream != null) {
            try (var ins = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                props.load(ins);
            } catch (Exception ignored) {
            }
        }

        Map<String, String> ret = new HashMap<>();
        props.forEach((k, v) -> ret.put((String) k, (String) v));

        return ret;
    }

}
