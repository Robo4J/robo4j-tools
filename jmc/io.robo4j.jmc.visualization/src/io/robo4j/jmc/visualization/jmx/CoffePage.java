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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import com.jrockit.mc.rjmx.IConnectionHandle;

/**
 * TODO(Marcus/Mar 9, 2017): Will fix this later... Currently Coff-E only.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CoffePage extends FormPage {

	public CoffePage() {
		super(null, "", "");
	}

	private IConnectionHandle getConnectionHandle() {
		return (IConnectionHandle) getEditor().getAdapter(IConnectionHandle.class);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		managedForm.getToolkit().decorateFormHeading(managedForm.getForm().getForm());
		managedForm.getForm().getBody().setLayout(new GridLayout(1, true));
		managedForm.getForm().getBody().setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		new JMXCoffeControl(managedForm.getForm().getBody(), SWT.NONE, getConnectionHandle());
	}
}
