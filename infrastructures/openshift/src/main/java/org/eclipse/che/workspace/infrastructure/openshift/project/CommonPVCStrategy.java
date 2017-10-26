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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolumeMount;

import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Provides common PVC for each workspace in one OpenShift project.
 *
 * <p>Note that subpaths are used for resolving of backed up data path collisions. Subpaths
 * evaluated as follows: '{workspaceId}/{workspace data folder}'. The number of workspaces that can
 * simultaneously store backups in one PV is limited only by the storage capacity. The number of
 * workspaces that can be running simultaneously depends on access mode configuration and che limits
 * configuration.
 *
 * @author Anton Korneta
 */
public class CommonPVCStrategy implements WorkspacePVCStrategy {

  public static final String COMMON_STRATEGY = "common";

  private final String pvcName;
  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final String projectsPath;

  @Inject
  public CommonPVCStrategy(
      @Named("che.infra.openshift.pvc.name") String pvcName,
      @Named("che.infra.openshift.pvc.quantity") String pvcQuantity,
      @Named("che.infra.openshift.pvc.access_mode") String pvcAccessMode,
      @Named("che.workspace.projects.storage") String projectFolderPath) {
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.projectsPath = projectFolderPath;
  }

  @Override
  public void prepare(OpenShiftEnvironment osEnv, String workspaceId, String machineName)
      throws InfrastructureException {
    osEnv.getPersistentVolumeClaims().put(pvcName, newPVC(pvcName, pvcAccessMode, pvcQuantity));
    for (Pod pod : osEnv.getPods().values()) {
      for (Container container : pod.getSpec().getContainers()) {
        final String machine = pod.getMetadata().getName() + '/' + container.getName();
        if (machine.equals(machineName)) {
          container
              .getVolumeMounts()
              .add(newVolumeMount(pvcName, projectsPath, workspaceId + projectsPath));
          pod.getSpec().getVolumes().add(newVolume(pvcName, pvcName));
          return;
        }
      }
    }
  }

  @Override
  public void cleanup(String workspaceId) {
    // TODO implement https://github.com/eclipse/che/issues/6767
  }
}
