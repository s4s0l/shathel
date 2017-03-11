package org.s4s0l.shathel.deployer.shell;

import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.support.util.IOUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.shell.support.util.OsUtils.LINE_SEPARATOR;

/**
 * @author Marcin Wielgus
 */
public class JLineShellComponentCustomLogging extends JLineShellComponent {


    @Override
    public void promptLoop() {
        Handler handler = LogManager.getLogManager().getLogger("").getHandlers()[0];
        handler.setFormatter(new Formatter() {
            @Override
            public String format(final LogRecord record) {
                StringBuffer sb = new StringBuffer();
                if (record.getMessage() != null && record.getThrown() == null) {
                    sb.append(record.getMessage()).append(LINE_SEPARATOR);
                }
                if (record.getThrown() != null) {
                    String stackTracesAndAll = "Exception occurred!";
                    PrintWriter pw = null;
                    try {
                        StringWriter sw = new StringWriter();
                        pw = new PrintWriter(sw);
                        record.getThrown().printStackTrace(pw);
                        stackTracesAndAll = sw.toString();
                    } catch (Exception ex) {
                    } finally {
                        IOUtils.closeQuietly(pw);
                    }

                    sb.append(filterStackTraces(stackTracesAndAll));

                }
                return sb.toString();
            }
        });
        super.promptLoop();
    }

    /**
     * Ugly as hell!
     *
     * @param stackTracesAndAll
     * @return
     */
    private String filterStackTraces(String stackTracesAndAll) {
        StringBuilder sb = new StringBuilder();
        String[] split = stackTracesAndAll.split("\n");
        for (String line : split) {
            if (stackElement.matcher(line).matches()) {
                if (acceptedPatterns.stream().filter(p -> p.matcher(line).matches()).findFirst().isPresent()) {
                    sb.append(line).append("\n");
                }
            } else {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    Pattern stackElement = Pattern.compile("\\s+at .*");
    List<Pattern> acceptedPatterns = Arrays.asList(
            Pattern.compile("\\s+at org\\.s4s0l.*")
    );
}
