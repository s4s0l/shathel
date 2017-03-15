package org.s4s0l.shathel.commons.utils;

import groovy.text.SimpleTemplateEngine;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marcin Wielgus
 */
public class TemplateUtils {

    public static String generateTemplate(URL template, Map context) {
        try {
            StringWriter stringWriter = new StringWriter();
            new SimpleTemplateEngine()
                    .createTemplate(ResourceGroovyMethods.getText(template))
                    .make(context)
                    .writeTo(stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to fill template " + template.toString(), e);
        }
    }


    private static Pattern x = Pattern.compile("(?<!\\$)\\$\\{?([A-Za-z0-9_-]+)(:-?[^\\}]+)?\\}?");

    public static String fillEnvironmentVariables(String text, Map<String, String> envs) {
        Matcher matcher = x.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String variable = matcher.group(1);
            if (envs.containsKey(variable)) {
                matcher.appendReplacement(sb, envs.get(variable));
            } else {
                if (matcher.group(2) == null) {
                    matcher.appendReplacement(sb, "${" + variable + "}");
                } else {
                    matcher.appendReplacement(sb, matcher.group(2).substring(2));
                }
            }

        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
