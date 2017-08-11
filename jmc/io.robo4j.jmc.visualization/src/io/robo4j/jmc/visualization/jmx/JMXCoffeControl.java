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
package io.robo4j.jmc.visualization.jmx;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wb.swt.SWTResourceManager;

import com.jrockit.mc.rjmx.IConnectionHandle;

import io.robo4j.jmc.visualization.Activator;

/**
 * TODO(Marcus/Mar 9, 2017): Will fix this later... Currently Coff-E only
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JMXCoffeControl extends Composite {
	private static final String ACTUATORS_OBJECT_NAME = "se.hirt.tank:type=Actuators";
	private static final String SENSORS_OBJECT_NAME = "se.hirt.tank:type=Sensors";

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private final Text textSpeed;
	private final Text textDirection;
	private final MBeanServerConnection serverConnection;
	private volatile ScheduledExecutorService executor;
	private final Scale scaleDirection;
	private final Scale scaleSpeed;
	private final Label lblNearest;
	private final Label lblFarthest;
	private final Label lblTarget;
	private final Label lblAhead;
	private final Label lblIsAuto;
	private Text textMaxSpeed;

	private class UpdateCommand implements Runnable {

		@Override
		public void run() {
			try {
				final String textTarget = String.format("Target: %2.1fm",
						(float) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "DistanceToCurrentGoal"));
				final String textNearest = String.format("Nearest: %2.1fm",
						(float) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "DistanceToClosestPoint"));
				final String textFarthest = String.format("Farthest: %2.1fm",
						(float) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "DistanceToFarthestPoint"));
				final float direction = (float) serverConnection.getAttribute(getObjectName(ACTUATORS_OBJECT_NAME), "Direction");
				final float speed = (float) serverConnection.getAttribute(getObjectName(ACTUATORS_OBJECT_NAME), "Speed");
				final boolean isAuto = (boolean) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "Running");

				lblTarget.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						lblTarget.setText(textTarget);
						lblNearest.setText(textNearest);
						lblFarthest.setText(textFarthest);
						lblIsAuto.setText(Boolean.toString(isAuto));

						scaleDirection.setSelection(Math.round(direction + 80));
						textDirection.setText(String.format("%d\u00b0", Math.round(direction)));
						int speedPercent = Math.round(speed * 100.0f);
						scaleSpeed.setSelection(speedPercent);
						textSpeed.setText(String.format("%d%%", speedPercent));

					}
				});
			} catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public JMXCoffeControl(Composite parent, int style, IConnectionHandle connectionHandle) {
		super(parent, style);
		serverConnection = connectionHandle.getServiceOrNull(MBeanServerConnection.class);

		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);
		setLayout(new GridLayout(1, false));
		GridData gdThis = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1);
		this.setLayoutData(gdThis);

		Composite root = new Composite(this, SWT.NONE);
		root.setLayout(new GridLayout(3, false));
		GridData gd_composite = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1);
		gd_composite.heightHint = 325;

		Composite composite_1 = new Composite(root, SWT.NONE);
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_1.heightHint = 263;
		composite_1.setLayoutData(gd_composite_1);
		composite_1.setBounds(0, 0, 64, 64);
		toolkit.adapt(composite_1);
		toolkit.paintBordersFor(composite_1);

		Button btnScan = new Button(composite_1, SWT.NONE);
		btnScan.setBounds(10, 287, 104, 28);
		toolkit.adapt(btnScan, true, true);
		btnScan.setText("Scan");

		lblNearest = new Label(composite_1, SWT.NONE);
		lblNearest.setBounds(0, 10, 124, 20);
		toolkit.adapt(lblNearest, true, true);
		lblNearest.setText("Nearest: <do scan>");

		lblFarthest = new Label(composite_1, SWT.NONE);
		lblFarthest.setBounds(0, 30, 124, 20);
		toolkit.adapt(lblFarthest, true, true);
		lblFarthest.setText("Farthest: <do scan>");

		lblTarget = new Label(composite_1, SWT.NONE);
		lblTarget.setBounds(0, 50, 124, 20);
		toolkit.adapt(lblTarget, true, true);
		lblTarget.setText("Target: <do scan>");

		lblAhead = new Label(composite_1, SWT.NONE);
		lblAhead.setBounds(0, 70, 114, 20);
		toolkit.adapt(lblAhead, true, true);
		lblAhead.setText("Ahead: <do scan>");

		Label lblIsAutoCaption = new Label(composite_1, SWT.NONE);
		lblIsAutoCaption.setBounds(27, 128, 59, 20);
		toolkit.adapt(lblIsAutoCaption, true, true);
		lblIsAutoCaption.setText("Is auto?:");

		lblIsAuto = new Label(composite_1, SWT.NONE);
		lblIsAuto.setBounds(27, 148, 87, 14);
		toolkit.adapt(lblIsAuto, true, true);
		lblIsAuto.setText("<unknown>");

		btnScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					serverConnection.invoke(getObjectName(SENSORS_OBJECT_NAME), "doScan", null, null);
					Thread.sleep(1500);
					lblTarget.setText(String.format("Target: %2.1fm",
							(float) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "DistanceToCurrentGoal")));
					lblNearest.setText(String.format("Nearest: %2.1fm",
							(float) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "DistanceToClosestPoint")));
					lblFarthest.setText(String.format("Farthest: %2.1fm",
							(float) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "DistanceToFarthestPoint")));
					lblAhead.setText(String.format("Ahead: %2.1fm",
							(float) serverConnection.getAttribute(getObjectName(SENSORS_OBJECT_NAME), "ClearDistanceAhead")));

				} catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException | InterruptedException
						| AttributeNotFoundException e) {
					Logger.getLogger(JMXCoffeControl.class.getName()).log(Level.WARNING, "Could not do scan!", e);
				}
			}
		});

		Composite composite = new Composite(root, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite.setLayoutData(gd_composite);

		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);

		scaleDirection = new Scale(composite, SWT.NONE);
		scaleDirection.setMaximum(160);
		scaleDirection.setMinimum(0);
		scaleDirection.setSelection(80);
		scaleDirection.setBounds(0, 200, 300, 16);
		scaleDirection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent se) {
				setDirection(scaleDirection.getSelection() - 80);
			}
		});
		toolkit.adapt(scaleDirection, true, true);

		scaleSpeed = new Scale(composite, SWT.VERTICAL);
		scaleSpeed.setBounds(142, 0, 16, 200);
		scaleSpeed.setSelection(0);

		scaleSpeed.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setSpeed(scaleSpeed.getSelection() / 100.0f);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		toolkit.adapt(scaleSpeed, true, true);

		Button btnStop = new Button(composite, SWT.CENTER);
		btnStop.setBounds(101, 242, 100, 53);
		btnStop.setImage(getImage(composite.getDisplay(), "icons/stop32.png"));

		toolkit.adapt(btnStop, true, true);
		btnStop.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				stop();
				scaleSpeed.setSelection(0);
				scaleDirection.setSelection(80);
				setSpeed(0);
				setDirection(0);
			}
		});

		Label lblSpeed = new Label(composite, SWT.NONE);
		lblSpeed.setBounds(29, 34, 59, 19);
		toolkit.adapt(lblSpeed, true, true);
		lblSpeed.setText("Speed:");

		textSpeed = new Text(composite, SWT.BORDER | SWT.CENTER);
		textSpeed.setEditable(false);
		textSpeed.setText("0%");
		textSpeed.setBounds(29, 54, 64, 19);
		toolkit.adapt(textSpeed, true, true);

		Label lblDirection = new Label(composite, SWT.NONE);
		lblDirection.setBounds(176, 34, 59, 19);
		toolkit.adapt(lblDirection, true, true);
		lblDirection.setText("Direction:");

		textDirection = new Text(composite, SWT.BORDER | SWT.CENTER);
		textDirection.setEditable(false);
		textDirection.setText("0\u00b0");
		textDirection.setBounds(176, 54, 64, 19);
		toolkit.adapt(textDirection, true, true);

		final Button btnReadCont = new Button(composite, SWT.CHECK);
		btnReadCont.setBounds(203, 297, 87, 18);
		toolkit.adapt(btnReadCont, true, true);
		btnReadCont.setText("Subscribe");

		Composite autonomyControlComposite = new Composite(root, SWT.NONE);
		GridData gd_autonomyControlComposite = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
		gd_autonomyControlComposite.heightHint = 284;
		gd_autonomyControlComposite.widthHint = 167;
		autonomyControlComposite.setLayoutData(gd_autonomyControlComposite);
		toolkit.adapt(autonomyControlComposite);
		toolkit.paintBordersFor(autonomyControlComposite);

		Button btnRunAutonomously = new Button(autonomyControlComposite, SWT.NONE);
		btnRunAutonomously.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				runAutonomously();
			}
		});
		btnRunAutonomously.setBounds(10, 281, 147, 34);
		toolkit.adapt(btnRunAutonomously, true, true);
		btnRunAutonomously.setText("Run Autonomously");

		Label lblMaxSpeed = new Label(autonomyControlComposite, SWT.NONE);
		lblMaxSpeed.setBounds(65, 119, 79, 19);
		toolkit.adapt(lblMaxSpeed, true, true);
		lblMaxSpeed.setText("Max speed:");

		textMaxSpeed = new Text(autonomyControlComposite, SWT.BORDER);
		textMaxSpeed.setText("30%");
		textMaxSpeed.setBounds(65, 139, 64, 19);
		toolkit.adapt(textMaxSpeed, true, true);

		final Scale scaleMaxSpeed = new Scale(autonomyControlComposite, SWT.VERTICAL);
		scaleMaxSpeed.setSelection(30);
		scaleMaxSpeed.setBounds(20, 21, 31, 245);
		toolkit.adapt(scaleMaxSpeed, true, true);

		scaleMaxSpeed.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setMaxSpeed(scaleMaxSpeed.getSelection() / 100.0f);
			}
		});
		
		
		btnReadCont.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (btnReadCont.getSelection()) {
					startReading();
				} else {
					stopReading();
				}
			}
		});

		if (serverConnection == null) {
			Label lblFail = new Label(parent, SWT.NONE);
			lblFail.setText("Could not get the server connection!");
			return;
		}
	}

	private Image getImage(Display display, String string) {
		return Activator.getDefault().getImageRegistry().get(Activator.STOP_ID);
	}

	private void setDirection(int direction) {
		textDirection.setText(String.format("%d\u00b0", direction));
		if (serverConnection != null) {
			Attribute attribute = new Attribute("Direction", direction);
			try {
				serverConnection.setAttribute(getObjectName(ACTUATORS_OBJECT_NAME), attribute);
			} catch (InstanceNotFoundException | AttributeNotFoundException | InvalidAttributeValueException | MBeanException
					| ReflectionException | IOException e) {
				Logger.getLogger(JMXCoffeControl.class.getName()).log(Level.WARNING, "Could not set Direction!", e);
			}
		}
	}

	private void setSpeed(float speed) {
		textSpeed.setText(String.format("%d%%", Math.round(speed * 100)));
		if (serverConnection != null) {
			Attribute attribute = new Attribute("Speed", speed);
			try {
				serverConnection.setAttribute(getObjectName(ACTUATORS_OBJECT_NAME), attribute);
			} catch (InstanceNotFoundException | AttributeNotFoundException | InvalidAttributeValueException | MBeanException
					| ReflectionException | IOException e) {
				Logger.getLogger(JMXCoffeControl.class.getName()).log(Level.WARNING, "Could not set Speed!.", e);
			}
		}
	}
	
	private void setMaxSpeed(float maxSpeed) {
		textMaxSpeed.setText(String.format("%d%%", Math.round(maxSpeed * 100)));
		if (serverConnection != null) {
			Attribute attribute = new Attribute("MaxSpeed", maxSpeed);
			try {
				serverConnection.setAttribute(getObjectName(ACTUATORS_OBJECT_NAME), attribute);
			} catch (InstanceNotFoundException | AttributeNotFoundException | InvalidAttributeValueException | MBeanException
					| ReflectionException | IOException e) {
				Logger.getLogger(JMXCoffeControl.class.getName()).log(Level.WARNING, "Could not set MaxSpeed!.", e);
			}
		}
	}

	private void stopReading() {
		executor.shutdown();
	}

	private void startReading() {
		ScheduledExecutorService old = executor;
		if (old != null) {
			old.shutdown();
		}
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new UpdateCommand(), 0, 1, TimeUnit.SECONDS);
	}

	private static ObjectName getObjectName(String objectName) {
		try {
			return new ObjectName(objectName);
		} catch (MalformedObjectNameException e) {
			// Will never feed it malformed ObjectNames
			Logger.getLogger(JMXCoffeControl.class.getName()).log(Level.SEVERE, "Idiot developer error", e);
		}
		return null;
	}

	public void stop() {
		try {
			serverConnection.invoke(getObjectName(SENSORS_OBJECT_NAME), "stop", null, null);
		} catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
			Logger.getLogger(JMXCoffeControl.class.getName()).log(Level.SEVERE, "Could not stop!", e);
		}
	}

	public void runAutonomously() {
		try {
			serverConnection.invoke(getObjectName(SENSORS_OBJECT_NAME), "runAutonomously", null, null);
		} catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
			Logger.getLogger(JMXCoffeControl.class.getName()).log(Level.SEVERE, "Could not start!", e);
		}
	}
}
