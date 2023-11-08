/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat. All rights reserved.
 *--------------------------------------------------------------------------------------------*/
package org.jboss.tools.intellij.windup.services;

import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.tree.StructureTreeModel;
import org.apache.commons.io.FileUtils;
import org.jboss.tools.intellij.windup.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ModelService implements Disposable {

    private KantraModel kantraModel;
    private Project project;
    private StructureTreeModel treeModel;

    private static final String STATE_LOCATION = ModelService.getStateLocation();

    public ModelService(Project project) {
        this.project = project;
    }

    public KantraModel getModel() {
        return this.kantraModel;
    }

    public Project getProject() {
        return this.project;
    }

    public void setTreeModel(StructureTreeModel treeModel) {
        this.treeModel = treeModel;
    }

    public StructureTreeModel getTreeModel() {
        return this.treeModel;
    }

    public void forceReload() {
        this.kantraModel = KantraModelParser.parseModel(STATE_LOCATION, this);
    }

    public KantraModel loadModel() {
        if (this.kantraModel != null) {
            return this.kantraModel;
        }
        this.kantraModel = KantraModelParser.parseModel(STATE_LOCATION, this);
        if (this.kantraModel.getConfigurations().isEmpty()) {
            // Create default configuration
            this.createConfiguration();
        }
        return this.kantraModel;
    }

    public boolean deleteConfiguration(KantraConfiguration configuration) {
        return this.kantraModel.deleteConfiguration(configuration);
    }

    public KantraConfiguration createConfiguration() {
        KantraModel model = this.getModel();
        KantraConfiguration configuration = new KantraConfiguration();
        configuration.setId(KantraConfiguration.generateUniqueId());
        configuration.setName(NameUtil.generateUniqueConfigurationName(model));
        configuration.getOptions().put("cli", this.computeKantraCliLocation());
        configuration.getOptions().put("output", ModelService.getConfigurationOutputLocation(configuration));
        configuration.getOptions().put("sourceMode", "true");
        configuration.getOptions().put("legacyReports", "true");
        List<String> target = (List<String>)configuration.getOptions().get("target");
        if (target == null || target.isEmpty()) {
            target = Lists.newArrayList();
            target.add("eap7");
        }
        configuration.getOptions().put("target", target);
        model.addConfiguration(configuration);
        return configuration;
    }

    public void saveModel() {
        JSONObject model = new JSONObject();
        JSONArray configurations = new JSONArray();
        model.put("configurations", configurations);
        for (KantraConfiguration configuration : this.kantraModel.getConfigurations()) {
            JSONObject configObject = new JSONObject();
            configurations.add(configObject);
            configObject.put("id", configuration.getId());
            configObject.put("name", configuration.getName());
            JSONObject options = new JSONObject();
            configObject.put("options", options);
            for (Map.Entry entry : configuration.getOptions().entrySet()) {
                Object value = entry.getValue();
                if (value instanceof List) {
                    List<String> values = (List<String>)value;
                    JSONArray valueArray = new JSONArray();
                    valueArray.addAll(values);
                    options.put(entry.getKey(), valueArray);
                }
                else {
                    options.put(entry.getKey(), entry.getValue());
                }
            }
        }
        boolean canWrite = true;
        try {
            if (!new File(STATE_LOCATION).exists()) {
                File out = new File(STATE_LOCATION);
                out.getParentFile().mkdirs();
                out.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            canWrite = false;
        }
        if (canWrite) {
            try (FileWriter file = new FileWriter(STATE_LOCATION)) {
                String content = model.toJSONString();
                file.write(content);
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteOutput(KantraConfiguration configuration) {
        String output = (String)configuration.getOptions().get("output");
        if (output != null && !"".equals(output)) {
            try {
                FileUtils.deleteDirectory(new File(output));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Unable to delete output location for configuration.");
        }
    }

    @Override
    public void dispose() {
        this.saveModel();
    }

    private static String getStateLocation() {
        return ModelService.getDefaultOutputLocation()
                + File.separator + "model.json";
    }

    public String computeKantraCliLocation() {
        if (this.getModel() == null || this.getModel().getConfigurations().isEmpty()) {
            return "";
        }
        KantraConfiguration configuration = Lists.reverse(this.getModel().getConfigurations()).stream().filter(config -> {
            String cli = (String) config.getOptions().get("cli");
            return cli != null && !"".equals(cli);
        }).findFirst().orElse(null);
        if (configuration != null) {
            return (String) configuration.getOptions().get("cli");
        }
        return "";
    }

    public static String getDefaultOutputLocation() {
        return FileUtils.getUserDirectoryPath()
                + File.separator + ".kantra"
                + File.separator +  "tooling"
                + File.separator + "intellij";
    }

    public static String getConfigurationOutputLocation(KantraConfiguration configuration) {
        return ModelService.getDefaultOutputLocation()
                + File.separator + configuration.getId();
    }
}
