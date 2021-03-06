package eu.netide.zmq.hub.ui.views;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import eu.netide.zmq.hub.server.ZmqPubSubHub;
import eu.netide.zmq.hub.server.ZmqHubManager;

public class ZmqHubView extends ViewPart {
	private Table table;

	private ZmqHubManager hubManager;
	private TableViewer tableViewer;
	private Shell shell;

	public ZmqHubView() {
		hubManager = ZmqHubManager.instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		shell = parent.getShell();
		parent.setLayout(new GridLayout(2, false));

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				IStructuredSelection selection = tableViewer.getStructuredSelection();
				ZmqPubSubHub hub = (ZmqPubSubHub) selection.getFirstElement();

				ZmqLogDialog logDialog = new ZmqLogDialog(shell, hub);
				logDialog.create();
				logDialog.open();
			}
		});
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setWidth(150);
		column.getColumn().setText("Name");
		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setWidth(150);
		column.getColumn().setText("Address");
		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText("Running");
		tableViewer.getTable().setHeaderVisible(true);

		Menu menu = new Menu(table);
		table.setMenu(menu);

		MenuItem itemToggle = new MenuItem(menu, SWT.NONE);
		itemToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = tableViewer.getStructuredSelection();
				ZmqPubSubHub hub = (ZmqPubSubHub) selection.getFirstElement();
				hub.setRunning(!hub.getRunning());
			}
		});
		itemToggle.setText("Toggle");

		ViewerSupport.bind(tableViewer, hubManager.getPubSubHubs(), BeanProperties.values(new String[] { "name", "address", "running" }));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));

		Button btnAdd = new Button(composite, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ZmqAddressDialog dialog = new ZmqAddressDialog(shell);
				dialog.create();
				if (dialog.open() == Window.OK) {
					hubManager.getPubSubHub(dialog.getName(), dialog.getAddress());
				}
			}
		});
		btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAdd.setText("Add");

		Button btnRemove = new Button(composite, SWT.NONE);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!tableViewer.getSelection().isEmpty()) {
					IStructuredSelection selection = tableViewer.getStructuredSelection();
					ZmqPubSubHub hub = (ZmqPubSubHub) selection.getFirstElement();
					hub.setRunning(false);
					hubManager.removePubSubHub(hub);
				}
			}
		});
		btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnRemove.setText("Remove");

		Button btnEdit = new Button(composite, SWT.NONE);
		btnEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnEdit.setText("Edit");

	}

	@Override
	public void setFocus() {}


}
