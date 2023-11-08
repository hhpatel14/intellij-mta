/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat. All rights reserved.
 *--------------------------------------------------------------------------------------------*/
package org.jboss.tools.intellij.windup.explorer.nodes;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.tree.StructureTreeModel;
import org.jboss.tools.intellij.windup.explorer.WindupTreeCellRenderer;
import org.jboss.tools.intellij.windup.model.KantraConfiguration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public abstract class KantraExplorerNode<T> extends AbstractTreeNode<T> {

    protected KantraConfiguration.AnalysisResultsSummary summary;

    protected KantraExplorerNode(T value, KantraConfiguration.AnalysisResultsSummary summary) {
        super(null, value);
        this.summary = summary;
    }

    public void onDoubleClick(Project project, StructureTreeModel treeModel) {
    }

    public void onClick(Project project) {
    }

    public void onClick(DefaultMutableTreeNode treeNode, TreePath path, WindupTreeCellRenderer renderer) {

    }

    public KantraConfiguration.AnalysisResultsSummary getSummary() {
        return this.summary;
    }
}
