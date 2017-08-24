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
package io.robo4j.jmc.visualization.jfr;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import com.jrockit.mc.flightrecorder.spi.IEvent;

import io.robo4j.jmc.visualization.scan.PointXY;
import io.robo4j.jmc.visualization.scan.ScanResultImpl;

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

	public void setInput(Iterator<IEvent> eventIterator) {
		String typeName = "";
		int scanID = -1;
		IEvent next = null;
		while (eventIterator.hasNext()) {
			next = eventIterator.next();
			typeName = next.getEventType().getName().toLowerCase();
			if (ScanConstants.TYPE_SCAN.equals(typeName) || ScanConstants.TYPE_SCAN_POINT.equals(typeName)) {
				scanID = (int) next.getValue(ScanConstants.SCAN_ID);
				break;
			}
		}
		if (scanID == -1) {
			return;
		}
		ScanResultImpl scan = new ScanResultImpl(scanID);
		addDataFromEvent(scan, next);
		while (eventIterator.hasNext()) {
			next = eventIterator.next();
			addDataFromEvent(scan, next);
		}
		scan.sort();
		lblRange.setText(String.format("Max y: %2.1fm", scan.getMaxY()));
		scanViewer.setModel(scan);
		scanViewer.redraw();
	}

	private void addDataFromEvent(ScanResultImpl scan, IEvent next) {
		if (ScanConstants.TYPE_SCAN_POINT.equals(next.getEventType().getName().toLowerCase())) {
			scan.addPoint(getPoint(next));
		} else if (ScanConstants.TYPE_TARGET.equals(next.getEventType().getName().toLowerCase())) {
			scan.addTargetPoint(getPoint(next));
		} else if (ScanConstants.TYPE_GOAL.equals(next.getEventType().getName().toLowerCase())) {
			scan.addGoalPoint(getPoint(next));
		} else {
			System.out.println(next.getEventType().getName().toLowerCase());
		}
		// TODO: add the rest of the scan data...
	}

	private PointXY getPoint(IEvent next) {
		float x = (float) next.getValue(ScanConstants.FIELD_X);
		float y = (float) next.getValue(ScanConstants.FIELD_Y);

		return new PointXY(x, y);
	}
}
