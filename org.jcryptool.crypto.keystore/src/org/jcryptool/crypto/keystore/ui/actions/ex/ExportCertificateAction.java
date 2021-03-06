// -----BEGIN DISCLAIMER-----
/*******************************************************************************
 * Copyright (c) 2008 JCrypTool Team and Contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
// -----END DISCLAIMER-----
package org.jcryptool.crypto.keystore.ui.actions.ex;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.jcryptool.core.util.directories.DirectoryService;
import org.jcryptool.crypto.keystore.KeyStorePlugin;
import org.jcryptool.crypto.keystore.backend.ImportExportManager;
import org.jcryptool.crypto.keystore.backend.KeyStoreManager;
import org.jcryptool.crypto.keystore.ui.views.interfaces.IViewKeyInformation;

/**
 * @author t-kern
 *
 */
public class ExportCertificateAction extends Action {
    private IViewKeyInformation info;

    /**
     * Creates a new instance of ExportSecretKeyAction
     */
    public ExportCertificateAction(IViewKeyInformation info) {
        this.info = info;
        this.setText(Messages.getString("Label.ExportPublicKey")); //$NON-NLS-1$
        this.setToolTipText(Messages.getString("Label.ExportPublicKey")); //$NON-NLS-1$
        this.setImageDescriptor(KeyStorePlugin.getImageDescriptor("icons/16x16/kgpg_export.png")); //$NON-NLS-1$
    }

    public void run() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterPath(DirectoryService.getUserHomeDir());
        dialog.setFilterExtensions(new String[] {Messages.getString("ExportCertificateAction.0")}); //$NON-NLS-1$
        dialog.setFilterNames(new String[] {Messages.getString("ExportCertificateAction.1")}); //$NON-NLS-1$
        dialog.setOverwrite(true);

        String filename = dialog.open();

        if (filename != null && info != null) {
            ImportExportManager.getInstance().exportCertificate(new Path(filename),
                    KeyStoreManager.getInstance().getPublicKey(info.getSelectedKeyAlias()));
        }
    }
}
