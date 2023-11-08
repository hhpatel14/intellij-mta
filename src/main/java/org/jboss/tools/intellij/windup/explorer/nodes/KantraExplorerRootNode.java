/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat. All rights reserved.
 *--------------------------------------------------------------------------------------------*/
package org.jboss.tools.intellij.windup.explorer.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class KantraExplorerRootNode extends KantraExplorerNode<KantraNodeModel> {

    public KantraExplorerRootNode(KantraNodeModel nodeModel) {
        super(nodeModel, null);
    }

    @NotNull
    @Override
    public Collection<ConfigurationNode> getChildren() {
        return super.getValue().getConfigurationNodes();
    }

    @Override
    protected void update(PresentationData presentation) {
    }

    @Override
    protected boolean shouldUpdateData() {
        return true;
    }
}
