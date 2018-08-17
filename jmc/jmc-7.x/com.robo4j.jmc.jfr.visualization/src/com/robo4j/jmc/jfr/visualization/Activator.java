package com.robo4j.jmc.jfr.visualization;
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


import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Activator for the JMC 7.x plug-in for Robo4J.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.robo4j.jmc.jfr.visualization";
	public static final String IMAGE_STOP = "stop";
	public static final String IMAGE_SCAN = "scan";
	private static Activator plugin;

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		reg.put(IMAGE_STOP, ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/stop32.png"), null)));
		reg.put(IMAGE_SCAN, ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/scan.png"), null)));
	}

	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

}
