package net.mizucoffee.easygit;

import java.util.prefs.BackingStoreException;
import java.util.regex.Pattern;

import javax.swing.border.TitledBorder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends Dialog {
	String value;
	Double i = 1d;

	public String getValue() {
		return value;
	}

	public InputDialog(Shell parent) {
		super(parent);
	}

	public InputDialog(Shell parent, int style) {
		super(parent, style);
	}

	@Override
	public void setText(String arg0) {
		// TODO Auto-generated method stub
		super.setText(arg0);
		title = arg0;
	}

	public void setMessage(String arg0) {
		message = arg0;
	}

	Label mes;
	Shell shell;
	String title;
	String message;

	public Double open() {
		Shell parent = getParent();
		shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText(title);

		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.horizontalSpacing = 200;
		shell.setLayout(gridLayout);

		mes = new Label(shell, SWT.NULL);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 2;
		mes.setLayoutData(gridData);
		mes.setText(message);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 1;
		Label label = new Label(shell, SWT.NULL);
		label.setText("Github URL");
		label.setLayoutData(gridData);

		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 1;
		final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(gridData);

		Button buttonCancel = new Button(shell, SWT.PUSH);
		buttonCancel.setText("Cancel");
		buttonCancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Button buttonOK = new Button(shell, SWT.PUSH);
		buttonOK.setText("OK");
		buttonOK.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				try {
					value = text.getText();
					buttonOK.setEnabled(true);
					if (!Pattern.compile("https://github.com/.+/.+").matcher(value).find())
						buttonOK.setEnabled(false);

				} catch (Exception e) {
					buttonOK.setEnabled(false);
				}
			}
		});

		buttonOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				i = 0d;
				shell.dispose();
			}
		});

		buttonCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				value = null;
				shell.dispose();
			}
		});

		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE)
					event.doit = false;
			}
		});

		text.setText("");
		shell.pack();
		shell.open();

		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return i;
	}
}
