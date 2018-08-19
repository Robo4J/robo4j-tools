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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.jmc.jfr.visualization.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.openjdk.jmc.common.item.Aggregators;
import org.openjdk.jmc.common.item.IAggregator;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.IItemIterable;
import org.openjdk.jmc.common.item.IMemberAccessor;
import org.openjdk.jmc.common.item.ItemFilters;
import org.openjdk.jmc.common.item.ItemToolkit;
import org.openjdk.jmc.common.unit.IQuantity;

import com.robo4j.jmc.jfr.visualization.Robo4JAttributes;
import com.robo4j.jmc.jfr.visualization.Robo4JFilters;
import com.robo4j.jmc.jfr.visualization.scans.ScanResultImpl;
import com.robo4j.math.geometry.Point2f;
import com.robo4j.math.geometry.ScanResult2D;

/**
 * The SWT component for showig scan data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanPanel extends Composite {

	private final ScanViewer scanViewer;
	private final Label lblRange;

	public ScanPanel(Composite parent, int style) {
		super(parent, style);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		setLayout(new GridLayout(2, false));

		lblRange = new Label(this, SWT.NONE);
		lblRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblRange.setAlignment(SWT.CENTER);
		lblRange.setText("<no selection>");
		new Label(this, SWT.NONE);

		scanViewer = new ScanViewer(this, SWT.NONE);
		scanViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));

		final Button btnRenderRaycast = new Button(composite, SWT.CHECK);
		btnRenderRaycast.setText("Render Raycast");
		btnRenderRaycast.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent se) {
				scanViewer.setRaycastEnabled(btnRenderRaycast.getSelection());
				scanViewer.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Button btnRenderSegments = new Button(composite, SWT.CHECK);
		btnRenderSegments.setBounds(0, 0, 94, 18);
		btnRenderSegments.setText("Render Segments");
		btnRenderSegments.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				scanViewer.setSegmentsEnabled(btnRenderSegments.getSelection());
				scanViewer.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Button btnRenderAngles = new Button(composite, SWT.CHECK);
		btnRenderAngles.setBounds(0, 0, 94, 18);
		btnRenderAngles.setText("Render Angles");
		btnRenderAngles.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				scanViewer.setAnglesEnabled(btnRenderAngles.getSelection());
				scanViewer.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		final Button btnRenderFeatures = new Button(composite, SWT.CHECK);
		btnRenderFeatures.setText("Render Features");
		btnRenderFeatures.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent se) {
				scanViewer.setFeaturesEnabled(btnRenderFeatures.getSelection());
				scanViewer.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setInput(IItemCollection events) {
		IItemCollection scans = events.apply(Robo4JFilters.SCAN);
		IAggregator<Set<IQuantity>, ?> distinctScansAggregator = Aggregators.distinct(Robo4JAttributes.SCAN_ID);
		Set<IQuantity> distinctScans = scans.getAggregate(distinctScansAggregator);
		IItemCollection scanPoints = events.apply(Robo4JFilters.SCAN_POINT_2D);

		List<ScanResult2D> scanResults = new ArrayList<>();

		for (IQuantity scanID : distinctScans) {
			// Add point creation here...
			IItemCollection scan = scanPoints.apply(ItemFilters.equals(Robo4JAttributes.SCAN_ID, scanID));
			ScanResult2D scanResult = createScanResult(scanID, scan);
			scanResults.add(scanResult);
		}
		lblRange.setText(scanResults.size() > 1 ? "(Multiple scans) " : "" + String.format("Max y: %2.1fm", getMaxY(scanResults)));
		scanViewer.setModel(scanResults);
		scanViewer.redraw();
	}

	private double getMaxY(List<ScanResult2D> scanResults) {
		double max = 0;
		for (ScanResult2D result : scanResults) {
			max = Math.max(max, result.getMaxY());
		}
		return max;
	}

	private ScanResult2D createScanResult(IQuantity scanID, IItemCollection scans) {
		ScanResultImpl scanResult = new ScanResultImpl((int) scanID.longValue());
		@SuppressWarnings("deprecation")
		IMemberAccessor<IQuantity, IItem> xAccessor = ItemToolkit.accessor(Robo4JAttributes.SCAN_POINT_2D_X);
		@SuppressWarnings("deprecation")
		IMemberAccessor<IQuantity, IItem> yAccessor = ItemToolkit.accessor(Robo4JAttributes.SCAN_POINT_2D_Y);
		for (IItemIterable iterable : scans) {
			for (IItem item : iterable) {
				IQuantity x = xAccessor.getMember(item);
				IQuantity y = yAccessor.getMember(item);
				scanResult.addPoint(Point2f.fromCartesian((float) x.doubleValue(), (float) y.doubleValue()));
			}
		}
		scanResult.sort();
		return scanResult;
	}
}
