//-----BEGIN DISCLAIMER-----
/*******************************************************************************
 * Copyright (c) 2010 JCrypTool Team and Contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
//-----END DISCLAIMER-----
package org.jcryptool.core.views.content.palette;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.jcryptool.core.ApplicationActionBarAdvisor;
import org.jcryptool.core.operations.OperationsPlugin;
import org.jcryptool.core.operations.algorithm.ShadowAlgorithmAction;
import org.jcryptool.core.views.AlgorithmView;
import org.jcryptool.core.views.ISearchable;
import org.jcryptool.core.views.ViewsPlugin;
import org.jcryptool.core.views.content.TreeView;

/**
 * A PaletteViewer for the algorithm extension point.
 *
 * @author mwalthart
 * @version 0.9.4
 */
public class AlgorithmPaletteViewer extends PaletteViewer implements ISearchable {
	private Action doubleClickAction;
	private AlgorithmPaletteViewer viewer = this;
	private PaletteRoot invisibleRoot;
	private ArrayList<ShadowAlgorithmAction> algorithmList = new ArrayList<ShadowAlgorithmAction>();
	private String search;
	private String extensionPointId = "org.jcryptool.core.operations.algorithms"; //$NON-NLS-1$

	/**
	 * creates a palette viewer
	 *
	 * @param parent
	 *            the parent composite
	 */
	public AlgorithmPaletteViewer(Composite parent) {
		super();
		loadAlgorithms();
		createTree(new String[] { "" }); //$NON-NLS-1$
		createControl(parent);

		// close all except classic and symmetric
		for (Object drawer : invisibleRoot.getChildren()) {
			if (!((PaletteDrawer) drawer).getLabel().equals(
					Messages.AlgorithmPaletteViewer_1)
					&& !((PaletteDrawer) drawer).getLabel().equals(
							Messages.AlgorithmPaletteViewer_2))
				((PaletteDrawer) drawer)
						.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
		}

		setPaletteRoot(invisibleRoot);
		setupDragAndDrop();
		makeAndAssignActions();
	}

	/**
	 * enables drag'n'drop functionality
	 */
	private void setupDragAndDrop() {
		viewer.addDragSourceListener(new TransferDragSourceListener() {
			public Transfer getTransfer() {
				return TextTransfer.getInstance();
			}

			public void dragFinished(DragSourceEvent event) {
			}

			public void dragSetData(DragSourceEvent event) {
				Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				Object model = ((PaletteEditPart) element).getModel();
				event.data = ((PaletteEntry) model).getLabel();
			}

			public void dragStart(DragSourceEvent event) {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();

				 // only allow drag&drop for algorithm, not for categories
				if (!(obj instanceof PaletteEditPart))
					event.doit = false;

				// random number generators have also no drag&drop
				final String label = ((PaletteEntry) ((PaletteEditPart) obj).getModel()).getParent().getLabel();
				if (label != null && label.equals(org.jcryptool.core.Messages.applicationActionBarAdvisor_Menu_Algorithms_PRNG)) //$NON-NLS-1$
					event.doit = false;
			}
		});
	}

	/**
	 * loads the algorithms from the extension point
	 */
	private void loadAlgorithms() {
		for (IAction action : OperationsPlugin.getDefault().getAlgorithmsManager().getShadowAlgorithmActions()) {
			if (!algorithmList.contains(((ShadowAlgorithmAction) action))) {
				algorithmList.add(((ShadowAlgorithmAction) action));
			}
		}
	}

	/**
	 * creates a tree representation of the algorithm structure
	 *
	 * @param needles
	 *            a search string to filter the algorithms
	 */
	private void createTree(String[] needles) {
		invisibleRoot = new PaletteRoot();
		TreeMap<String, PaletteDrawer> types = new TreeMap<String, PaletteDrawer>();
		TreeMap<String, SelectionToolEntry> sortList = new TreeMap<String, SelectionToolEntry>();

		Iterator<ShadowAlgorithmAction> it = algorithmList.iterator();
		ShadowAlgorithmAction act = null;

		while (it.hasNext()) {
			act = it.next();

			// filter
			boolean show = true;
			for (String needle : needles) {
				if (!act.getText().toLowerCase()
						.matches(".*" + needle.toLowerCase() + ".*")) //$NON-NLS-1$ //$NON-NLS-2$
					show = false;
			}

			if (show) {
				// Create Category
				String type = act.getType();
				if (types.get(type) == null) {
					// translate
					type = ApplicationActionBarAdvisor.getTypeTranslation(type);

					PaletteDrawer paletteDrawer = new PaletteDrawer(type);
					paletteDrawer.setSmallIcon(ViewsPlugin
							.getImageDescriptor(TreeView.ICON_FOLDER));
					paletteDrawer.setLargeIcon(ViewsPlugin
							.getImageDescriptor(TreeView.ICON_FOLDER));
					types.put(act.getType(), paletteDrawer);
				}

				// Add element
				SelectionToolEntry paletteEntry = new SelectionToolEntry(
						act.getText(), act.getToolTipText());
				if (act.isFlexiProviderAlgorithm()) { // FlexiProvider item
					paletteEntry.setSmallIcon(ViewsPlugin
							.getImageDescriptor(TreeView.ICON_ITEM_FLEXI));
					paletteEntry.setLargeIcon(ViewsPlugin
							.getImageDescriptor(TreeView.ICON_ITEM_FLEXI));
				} else { // JCrypTool item
					paletteEntry.setSmallIcon(ViewsPlugin
							.getImageDescriptor(TreeView.ICON_ITEM_JCT));
					paletteEntry.setLargeIcon(ViewsPlugin
							.getImageDescriptor(TreeView.ICON_ITEM_JCT));
				}
				paletteEntry
						.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
				paletteEntry.setType(act.getType());

				sortList.put(paletteEntry.getLabel(), paletteEntry); // temporary save in list
			}
		}
		ArrayList<PaletteDrawer> parents = new ArrayList<PaletteDrawer>(
				types.values());

		for (SelectionToolEntry paletteEntry : sortList.values()) {
			// read from sorted list
			types.get(paletteEntry.getType()).add(paletteEntry); // put sorted into palette
		}

		// attach tree to the root element
		Iterator<PaletteDrawer> parentIterator2 = parents.iterator();
		while (parentIterator2.hasNext()) {
			invisibleRoot.add(parentIterator2.next());
		}
	}

	/**
	 * Constructs the actions according to the algorithm extension point and
	 * assigns the actions to the doubleclick listener of the viewer
	 */
	private void makeAndAssignActions() {
		doubleClickAction = new Action() {
			public void run() {
				Object selection = ((IStructuredSelection) viewer
						.getSelection()).getFirstElement();

				if (selection instanceof PaletteEditPart) {
					PaletteEditPart paletteEditPart = (PaletteEditPart) selection;

					Object model = paletteEditPart.getModel();

					IEditorReference[] editorReferences = PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().getEditorReferences();
					if (editorReferences.length == 0
							&& (!((PaletteEntry) model)
									.getParent()
									.getLabel()
									.equals(org.jcryptool.core.Messages.applicationActionBarAdvisor_Menu_Algorithms_PRNG))) {
						AlgorithmView
								.showMessage(Messages.AlgorithmPaletteViewer_0);
					} else {
						Iterator<ShadowAlgorithmAction> it9 = algorithmList
								.iterator();
						ShadowAlgorithmAction action = null;
						while (it9.hasNext()) {
							action = it9.next();
							if (model.toString().equals(
									"Palette Entry (" + action.getText() + ")")) { //$NON-NLS-1$ //$NON-NLS-2$
								action.run();
							}
						}
					}
				}
			}
		};

		viewer.getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				if (e.button == 1) { // only left button double clicks
					doubleClickAction.run(); // run assigned action
				}
			}

			@Override
			public void mouseDown(final MouseEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				Object obj = selection.getFirstElement();

				if (obj instanceof PaletteEditPart) {
					AlgorithmView.showContextHelp(extensionPointId,
							((PaletteEntry) ((PaletteEditPart) obj).getModel())
									.getLabel());
					viewer.getControl().setFocus();
					viewer.setSelection(selection);
				}
			}
		});
	}

	/**
	 * returns the current search string of the viewer
	 *
	 * @see ISearchable
	 */
	public String getCurrentSearch() {
		if (search == null)
			return ""; //$NON-NLS-1$
		return search;
	}

	/**
	 * sets the search string for the viewer
	 */
	public void search(String needle) {
		this.search = needle;

		createTree(needle.split(" ")); //$NON-NLS-1$
		setPaletteRoot(invisibleRoot);
	}
}
