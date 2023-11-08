/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat. All rights reserved.
 *--------------------------------------------------------------------------------------------*/
package org.jboss.tools.intellij.windup.explorer.nodes;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jboss.tools.intellij.windup.model.Ruleset;
import org.jboss.tools.intellij.windup.model.KantraConfiguration.*;

import java.io.File;
import java.util.Collection;

public abstract class ViolationNode extends KantraExplorerNode<Violation> {

    public ViolationNode(AnalysisResultsSummary summary, Violation violation) {
        super(violation, summary);
    }


    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }
}
