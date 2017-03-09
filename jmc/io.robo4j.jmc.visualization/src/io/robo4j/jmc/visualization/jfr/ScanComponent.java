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
package io.robo4j.jmc.visualization.jfr;

import java.util.Iterator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.jrockit.mc.components.ui.base.AbstractUIComponent;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.ui.common.IEventConsumer;
import com.jrockit.mc.flightrecorder.ui.components.inputs.Role;

/**
 * Special JFR UI Component for showing scan data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanComponent extends AbstractUIComponent implements IEventConsumer {

	private ScanPanel viewer;

	@Override
	public Control createPart(Composite parent) {
		setViewer(new ScanPanel(parent, 0));
		return getViewer();
	}

	@Override
	public void consumeEvents(Iterable<IEvent> eventIterable, Role role) {
		Iterator<IEvent> eventIterator = eventIterable.iterator();
		if (eventIterator.hasNext()) {
			updateInput(eventIterator);
		} else {
			updateInput(null);
		}
		getViewer().redraw();
	}

	private void updateInput(Iterator<IEvent> eventIterator) {
		getViewer().setInput(eventIterator);
	}

	public ScanPanel getViewer() {
		return viewer;
	}

	public void setViewer(ScanPanel viewer) {
		this.viewer = viewer;
	}
}
