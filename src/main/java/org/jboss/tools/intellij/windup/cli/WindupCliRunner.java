/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat. All rights reserved.
 *--------------------------------------------------------------------------------------------*/
package org.jboss.tools.intellij.windup.cli;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.exec.*;
import org.jboss.tools.intellij.windup.model.WindupConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.jboss.tools.intellij.windup.cli.ProgressMonitor.PROGRESS;

public class WindupCliRunner {

    private static final String JAVA_HOME = "JAVA_HOME";
    private static final String PROGRESS = ":progress:";

    public interface CliListener {
        default void onProcessFailed(ExecuteException e) {}
        default void onProcessComplete(int exitValue) {}
    }

    public static void run(WindupConfiguration configuration,
                           ProgressMonitor progressMonitor,
                           CliListener listener) {
        String javaHome = "";
        String windupCli = (String)configuration.getOptions().get("cli");
        List<String> params = WindupCliParamBuilder.buildParams(configuration, windupCli);
        WindupCliRunner.executeAnalysis(windupCli, javaHome, params, progressMonitor, listener);
    }

    private static void executeAnalysis(String cli, String javaHome, List<String> params, ProgressMonitor progressMonitor, CliListener listener) {
        System.out.println("execute CLI");
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
                System.out.println("Message from CLI: " + line);
                if (line.contains(PROGRESS)) {
                    JsonObject json = ProgressMonitor.parseProgressMessage(line);
                    if (json != null) {
                        progressMonitor.handleMessage(ProgressMonitor.parse(json));
                    }
                }
            }
        }));
        executor.setWatchdog(watchdog);
        executor.setExitValue(1);
        try {
            executor.execute(cmdLine, env, handler);
        } catch (IOException e) {
            System.out.println("Error executing CLI" + e.getMessage());
        }
    }
}
