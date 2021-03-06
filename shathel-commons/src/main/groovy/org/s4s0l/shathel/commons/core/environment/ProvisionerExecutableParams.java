package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.scripts.HttpApis;
import org.s4s0l.shathel.commons.scripts.ansible.AnsibleScriptContext;
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
    private final List<ShathelNode> currentNodes;
    private final AnsibleScriptContext ansibleScriptContext;

    public ProvisionerExecutableParams(Map<String, Object> map) {
        this(
                (EnvironmentContext) map.get("context"),
                (ExecutableApiFacade) map.get("api"),
                (StackCommand) map.get("command"),
                (File) map.get("dir"),
                (Logger) map.get("log"),
                (HttpApis) map.get("http"),
                (Map<String, String>) map.get("env"),
                (List<ShathelNode>) map.get("currentNodes"),
                (AnsibleScriptContext) map.get("ansible")
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
        map.put("ansible", ansibleScriptContext);
        return map;
    }


    public ProvisionerExecutableParams(EnvironmentContext environmentContext,
                                       ExecutableApiFacade apiFacade,
                                       StackCommand command, File dstStackDir,
                                       Logger logger, HttpApis http,
                                       Map<String, String> environment,
                                       List<ShathelNode> currentNodes, AnsibleScriptContext ansibleScriptContext) {
        this.environmentContext = environmentContext;
        this.apiFacade = apiFacade;
        this.command = command;
        this.dstStackDir = dstStackDir;
        this.logger = logger;
        this.http = http;
        this.environment = environment;
        this.currentNodes = currentNodes;
        this.ansibleScriptContext = ansibleScriptContext;
    }

    public EnvironmentContext getContext() {
        return environmentContext;
    }

    public ExecutableApiFacade getApi() {
        return apiFacade;
    }

    public StackCommand getCommand() {
        return command;
    }

    public File getDir() {
        return dstStackDir;
    }

    public Logger getLog() {
        return logger;
    }

    public HttpApis getHttp() {
        return http;
    }

    public Map<String, String> getEnv() {
        return environment;
    }

    public List<ShathelNode> getCurrentNodes() {
        return currentNodes;
    }

    public AnsibleScriptContext getAnsible() {
        return ansibleScriptContext;
    }
}
