/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.jmc.jfr.visualization.pages;

import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.common.IState;
import org.openjdk.jmc.common.IWritableState;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkAttributes;
import org.openjdk.jmc.flightrecorder.ui.IDataPageFactory;
import org.openjdk.jmc.flightrecorder.ui.IDisplayablePage;
import org.openjdk.jmc.flightrecorder.ui.IPageContainer;
import org.openjdk.jmc.flightrecorder.ui.IPageDefinition;
import org.openjdk.jmc.flightrecorder.ui.IPageUI;
import org.openjdk.jmc.flightrecorder.ui.StreamModel;
import org.openjdk.jmc.flightrecorder.ui.common.AbstractDataPage;
import org.openjdk.jmc.flightrecorder.ui.common.DataPageToolkit;
import org.openjdk.jmc.flightrecorder.ui.common.ItemHistogram;
import org.openjdk.jmc.flightrecorder.ui.common.ItemHistogram.ItemHistogramBuilder;
import org.openjdk.jmc.ui.column.TableSettings;
import org.openjdk.jmc.ui.column.TableSettings.ColumnSettings;

import com.robo4j.jmc.jfr.visualization.Activator;
import com.robo4j.jmc.jfr.visualization.Robo4JAttributes;
import com.robo4j.jmc.jfr.visualization.Robo4JFilters;

public class ScansPage extends AbstractDataPage {

	public static class ScansPageFactory implements IDataPageFactory {
		@Override
		public String getName(IState state) {
			return "Robo4J Laser Scans";
		}

		@Override
		public ImageDescriptor getImageDescriptor(IState state) {
			return Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMAGE_SCAN);
		}

		@Override
		public String[] getTopics(IState state) {
			return new String[] { PageUtils.ROBO4J_TOPIC };
		}

		@Override
		public IDisplayablePage createPage(IPageDefinition dpd, StreamModel items, IPageContainer editor) {
			return new ScansPage(dpd, items, editor);
		}
	}

	private class ScansPageUI implements IPageUI {
		private final SashForm sash;
		private final ItemHistogram scansTable;
		private final ScanPanel scanPanel;
		private static final String SCANS_TABLE = "scansTable"; //$NON-NLS-1$

		public ScansPageUI(Composite container, FormToolkit toolkit, IPageContainer pageContainer, IState state) {
			Form form = DataPageToolkit.createForm(container, toolkit, getName(), getIcon());

			sash = new SashForm(form.getBody(), SWT.VERTICAL | SWT.SMOOTH);
			toolkit.adapt(sash);

			scansTable = createTable(sash, state);
			scansTable.show(getDataSource().getItems().apply(Robo4JFilters.ALL_ROBO4J));
			scansTable.getManager().getViewer().addSelectionChangedListener(e -> onScansSelected());
			scanPanel = new ScanPanel(sash, SWT.SMOOTH);
		}

		private void onScansSelected() {
			scanPanel.setInput(scansTable.getSelection().getItems());
		}

		private ItemHistogram createTable(Composite tableComposite, IState state) {
			return HISTOGRAM.build(tableComposite, Robo4JAttributes.SCAN_ID, getScansTableSettings(state.getChild(SCANS_TABLE)));
		}

		private TableSettings getScansTableSettings(IState state) {
			if (state == null) {
				return new TableSettings(Robo4JAttributes.SCAN_ID.getIdentifier(),
						Arrays.asList(new ColumnSettings(ItemHistogram.KEY_COL_ID, false, 75, false),
								new ColumnSettings(JfrAttributes.DURATION.getIdentifier(), false, 75, false)));
			} else {
				return new TableSettings(state);
			}
		}

		@Override
		public void saveTo(IWritableState arg0) {

		}
	}

	private static final ItemHistogramBuilder HISTOGRAM = new ItemHistogramBuilder();

	static {
		HISTOGRAM.addColumn(Robo4JAttributes.SCAN_ID);
		HISTOGRAM.addColumn(JfrAttributes.DURATION);
		HISTOGRAM.addColumn(JdkAttributes.EVENT_THREAD_ID);
		HISTOGRAM.addCountColumn();
		HISTOGRAM.addColumn(Robo4JAttributes.SCAN_LEFT_RIGHT);
		HISTOGRAM.addColumn(Robo4JAttributes.SCAN_INFO);
	}

	public ScansPage(IPageDefinition definition, StreamModel model, IPageContainer editor) {
		super(definition, model, editor);
	}

	@Override
	public IPageUI display(Composite composite, FormToolkit toolkit, IPageContainer container, IState state) {
		return new ScansPageUI(composite, toolkit, container, state);
	}

}
