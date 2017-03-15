package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.scripts.HttpApis;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProvisionerExecutableParams {
    private final EnvironmentContext environmentContext;
    private final ExecutableApiFacade apiFacade;
    private final StackCommand command;
    private final File dstStackDir;
    private final Logger logger;
    private final HttpApis http;
    private final Map<String, String> environment;
    private final List<String> currentNodes;

    public ProvisionerExecutableParams(Map<String, Object> map) {
        this(
                (EnvironmentContext) map.get("context"),
                (ExecutableApiFacade) map.get("api"),
                (StackCommand) map.get("command"),
                (File) map.get("dir"),
                (Logger) map.get("log"),
                (HttpApis) map.get("http"),
                (Map<String, String>) map.get("env"),
                (List<String>) map.get("currentNodes")

        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("log", logger);
        map.put("context", environmentContext);
        map.put("api", apiFacade);
        map.put("env", environment);
        map.put("command", command);
        map.put("dir", dstStackDir);
        map.put("http", http);
        map.put("currentNodes", currentNodes);
        return map;
    }


    public ProvisionerExecutableParams(EnvironmentContext environmentContext,
                                       ExecutableApiFacade apiFacade,
                                       StackCommand command, File dstStackDir,
                                       Logger logger, HttpApis http,
                                       Map<String, String> environment,
                                       List<String> currentNodes) {
        this.environmentContext = environmentContext;
        this.apiFacade = apiFacade;
        this.command = command;
        this.dstStackDir = dstStackDir;
        this.logger = logger;
        this.http = http;
        this.environment = environment;
        this.currentNodes = currentNodes;
    }

    public EnvironmentContext getEnvironmentContext() {
        return environmentContext;
    }

    public ExecutableApiFacade getApiFacade() {
        return apiFacade;
    }

    public StackCommand getCommand() {
        return command;
    }

    public File getDstStackDir() {
        return dstStackDir;
    }

    public Logger getLogger() {
        return logger;
    }

    public HttpApis getHttp() {
        return http;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public List<String> getCurrentNodes() {
        return currentNodes;
    }
}
