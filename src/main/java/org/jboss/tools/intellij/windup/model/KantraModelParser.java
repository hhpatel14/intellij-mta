/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat. All rights reserved.
 *--------------------------------------------------------------------------------------------*/
package org.jboss.tools.intellij.windup.model;

import com.google.common.collect.Lists;
import org.jboss.tools.intellij.windup.cli.RulesetParser;
import org.jboss.tools.intellij.windup.services.ModelService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class KantraModelParser {

    public static KantraModel parseModel(String fileName, ModelService modelService) {
        KantraModel kantraModel = new KantraModel();
        JSONParser parser = new JSONParser();
        if (new File(fileName).exists()) {
            try {
                Object obj = parser.parse(new FileReader(fileName));
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray configurations = (JSONArray) jsonObject.get("configurations");
                Iterator iterator = configurations.iterator();
                while (iterator.hasNext()) {
                    JSONObject config = (JSONObject) iterator.next();
                    KantraConfiguration configuration = KantraModelParser.parseConfigurationObject(config, modelService);
                    kantraModel.addConfiguration(configuration);
                    RulesetParser.parseRulesetForKantraConfig(configuration);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return kantraModel;
    }

    private static KantraConfiguration parseConfigurationObject(JSONObject configurationObjects, ModelService modelService) {
        KantraConfiguration kantraConfiguration = new KantraConfiguration();
        kantraConfiguration.setId((String) configurationObjects.get("id"));
        kantraConfiguration.setName((String) configurationObjects.get("name"));
        KantraModelParser.parseConfigurationOptionsObject(
                (Map) configurationObjects.get("options"),
                kantraConfiguration);
        String kantraCli = (String)kantraConfiguration.getOptions().get("cli");
        if (kantraCli == null || "".equals(kantraCli)) {
            kantraConfiguration.getOptions().put("cli", modelService.computeKantraCliLocation());
        }
        String output = (String)kantraConfiguration.getOptions().get("output");
        if (output == null || "".equals(output)) {
            kantraConfiguration.getOptions().put("output", ModelService.getConfigurationOutputLocation(kantraConfiguration));
        }
        return kantraConfiguration;
    }

    public static void parseConfigurationOptionsObject(Map optionsObject, KantraConfiguration configuration) {
        Iterator<Map.Entry> iterator = optionsObject.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = iterator.next();
            Object key = pair.getKey();
            Object value = pair.getValue();
            if (value instanceof JSONArray) {
                JSONArray optionValues = (JSONArray)value;
                ArrayList<String> values = Lists.newArrayList();
                for (Object v : optionValues) {
                    values.add((String)v);
                }
                configuration.addOption((String)key, values);
            }
            else {
                configuration.addOption((String)key, String.valueOf(value));
            }
        }
    }

}
