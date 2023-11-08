/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat. All rights reserved.
 *--------------------------------------------------------------------------------------------*/
package org.jboss.tools.intellij.mta.model;

import com.intellij.openapi.project.Project;
import org.jboss.tools.intellij.windup.model.KantraConfiguration;
import org.jboss.tools.intellij.windup.services.ModelService1;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class WindupModelTest {

    private Project project;
    private ModelService1 modelService;

    @Before
    public void before() {
        this.project = mock(Project.class);
        modelService = new ModelService1(this.project);
        modelService.loadModel();
    }

//    @Test
//    public void testConfigurationAdded() {
//        this.modelService.createConfiguration();
//        assertFalse(this.modelService.getModel().getConfigurations().isEmpty());
//    }
//
//    @Test
//    public void testConfigurationDeleted() {
//        this.modelService.getModel().getConfigurations().clear();
//        KantraConfiguration config = this.modelService.createConfiguration();
//        this.modelService.deleteConfiguration(config);
//        assertTrue(this.modelService.getModel().getConfigurations().isEmpty());
//    }
}