package org.s4s0l.shathel.commons.utils;

import groovy.text.SimpleTemplateEngine;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public class TemplateUtils {

    public static String generateTemplate(URL template, Map context){
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
}
