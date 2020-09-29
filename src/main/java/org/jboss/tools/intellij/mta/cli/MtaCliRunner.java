package org.jboss.tools.intellij.mta.cli;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.exec.*;
import org.jboss.tools.intellij.mta.model.MtaConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MtaCliRunner {

    private static final String JAVA_HOME = "JAVA_HOME";
    private static final String PROGRESS = ":progress:";

    public static interface CliListener {
        default void onProcessFailed(ExecuteException e) {}
        default void onProcessComplete(int exitValue) {}
    }

    public static void run(MtaConfiguration configuration,
                           ProgressMonitor progressMonitor,
                           String mtaCli,
                           CliListener listener) {
        String javaHome = "";
        List<String> params = MtaCliParamBuilder.buildParams(configuration, mtaCli);
        MtaCliRunner.executeAnalysis(mtaCli, javaHome, params, progressMonitor, listener);
    }

    private static void executeAnalysis(String cli, String javaHome, List<String> params, ProgressMonitor progressMonitor, CliListener listener) {
        System.out.println("exucute mta-cli");
        CommandLine cmdLine = CommandLine.parse(cli);
        Map<String, String> env = Maps.newHashMap();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            env.put(entry.getKey(), entry.getValue());
        }
        if (!javaHome.trim().isEmpty()) {
            env.put(JAVA_HOME, javaHome);
        }

        cmdLine.addArguments(params.toArray(new String[params.size()]), true);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        ExecuteResultHandler handler = new ExecuteResultHandler() {
            @Override
            public void onProcessFailed(ExecuteException e) {
                listener.onProcessFailed(e);
            }
            @Override
            public void onProcessComplete(int exitValue) {
                listener.onProcessComplete(exitValue);
            }
        };
        JsonParser jsonParser = new JsonParser();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(new LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                System.out.println("Message from mta-cli: " + line);
                if (line.contains(PROGRESS)) {
                    line = line.replace(PROGRESS, "").trim();
                    if (line.contains("{\"op\":\"") && !line.contains("\"op\":\"logMessage\"")) {
                        try {
                            progressMonitor.handleMessage(ProgressMonitor.parse(jsonParser, line));
                        }
                        catch (JsonSyntaxException e) {
                            System.out.println("Error parsing mta-cli output: " + e.getMessage());
                        }
                    }
                }
            }
        }));
        executor.setWatchdog(watchdog);
        executor.setExitValue(1);
        try {
            executor.execute(cmdLine, env, handler);
        } catch (IOException e) {
            System.out.println("Error executing mta-cli" + e.getMessage());
        }
    }
}
