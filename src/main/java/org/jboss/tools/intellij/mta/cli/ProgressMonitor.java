package org.jboss.tools.intellij.mta.cli;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class ProgressMonitor {

    public static interface IProgressListener {
        void report(String message, int percentage);
        void onComplete();
    }

    public static class ProgressMessage {
        String op = "";
        String task = "";
        int totalWork = 0;
        String value = "";
    }

    private IProgressListener progressListener;

    private boolean started = false;
    private int preWork = 0;
    private String title = "";
    private int totalWork = 0;
    private boolean finalizing = false;
    private boolean done = false;

    public ProgressMonitor(IProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void handleMessage(ProgressMessage msg) {
        this.delegateMessage(msg);
    }

    private void delegateMessage(ProgressMessage msg) {
        if (msg.op.equals("beginTask")) {
            String task = msg.task;
            int work = msg.totalWork;
            this.beginTask(task, work);
            return;
        }

        if (msg.op.equals("complete")) {
            this.finish();
            return;
        }

        String value = msg.value;
        switch (msg.op) {
            case "logMessage":
                this.logMessage(value);
                break;
            case "done":
                this.done();
                break;
            case "setTaskName":
                this.setTaskName(value);
                break;
            case "subTask":
                this.subTask(value);
                break;
            case "worked":
                this.worked(Integer.valueOf(value));
                break;
        }

        if (!this.started) {
            this.started = true;
            this.report("Launching analysis...", -1);
        }
    }

    public void logMessage(String message) {
    }

    public void beginTask(String task, int total) {
        this.title = "Analysis in progress";
        this.totalWork = total;
        this.setTitle(this.title, -1);
    }

    public void done() {
        this.done = true;
        this.report("Finalizing...", -1);
    }

    public boolean isDone() {
        return this.done;
    }

    public void setTaskName(String task) {
        this.report(task, -1);
    }

    public void subTask(String name) {
    }

    public void worked(int worked) {
        this.preWork += worked;
        this.setTitle(this.computeTitle(), this.getPercentangeDone());
    }

    private int getPercentangeDone() {
        System.out.println(".1) " + (this.preWork * 100 / this.totalWork));
        System.out.println(".2) " + Math.min((this.preWork * 100 / this.totalWork), 100));
        System.out.println(".3) " + Math.floor(Math.min((this.preWork * 100 / this.totalWork), 100)));
        return (int)Math.floor(Math.min((this.preWork * 100 / this.totalWork), 100));
    }

    private String computeTitle() {
        int done = this.getPercentangeDone();
        String label = this.title;
        if (done > 0) {
            label += " (" + done + " % done)";
        }
        return label;
    }



    private void setTitle(String value, int percentage) {
        this.report(value, percentage);
    }

    public void report(String msg, int percentage) {
        if (!this.done && !this.finalizing) {
            if (this.getPercentangeDone() == 99) {
                this.finalizing = true;
                msg = "Finalizing...";
            }
            this.progressListener.report(msg, percentage);
        }
        else {
            System.out.println("progress done or cancelled, cannot report: " + msg);
        }
    }

    private void finish() {
        System.out.println("analysis complete...");
        this.finalizing = true;
        this.progressListener.onComplete();
    }

    public static ProgressMessage parse(JsonParser jsonParser, String line) throws JsonSyntaxException {
        JsonObject json = jsonParser.parse(line).getAsJsonObject();
        ProgressMonitor.ProgressMessage msg = new ProgressMonitor.ProgressMessage();
        msg.op = json.get("op").getAsString();
        if (json.has("value")) {
            msg.value = json.get("value").getAsString();
        }
        if (json.has("task")) {
            msg.task = json.get("task").getAsString();
        }
        if (json.has("totalWork")) {
            msg.totalWork = json.get("totalWork").getAsInt();
        }
        return msg;
    }
}
