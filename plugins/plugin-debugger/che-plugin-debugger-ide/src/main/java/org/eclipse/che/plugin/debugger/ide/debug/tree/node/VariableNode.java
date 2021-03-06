/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug.tree.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

public class VariableNode extends AbstractDebuggerNode<Variable> {

  private final PromiseProvider promiseProvider;
  private Variable data;

  @Inject
  public VariableNode(@Assisted Variable data, PromiseProvider promiseProvider) {
    this.promiseProvider = promiseProvider;
    this.data = data;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.resolve(children);
  }

  @Override
  public String getName() {
    return data.getName();
  }

  @Override
  public boolean isLeaf() {
    return data.isPrimitive();
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    String content = data.getName() + "=" + data.getValue().getString();
    presentation.setPresentableText(content);
  }

  @Override
  public Variable getData() {
    return data;
  }

  @Override
  public void setData(Variable data) {
    this.data = data;
  }
}
