package eu.netide.workbenchconfigurationeditor.dialogs;

import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import eu.netide.configuration.utils.NetIDE;
import eu.netide.workbenchconfigurationeditor.model.LaunchConfigurationModel;

/**
 * 
 * @author Jan-Niclas Struewer
 *
 */
public class ConfigurationShell extends Shell {

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	private void openShell(Display display) {
		try {
			this.open();
			this.layout();
			if (edit) {
				this.appPathText.setText(model.getAppPath());
				this.appPathSet = true;

				this.nameSet = true;
				this.textName.setText(model.getName());
				// set checkbox according to using network engine

				if (model.getPlatform().equals(NetIDE.CONTROLLER_ENGINE)) {
					this.btnCheckButton.setSelection(true);
					this.platformCombo.select(this.platformCombo.indexOf(model.getClientController()));
				} else {
					int platformIndex = this.platformCombo.indexOf(model.getPlatform());
					this.platformCombo.select(platformIndex);
				}
				this.platformSet = true;
				
				if(model.getFlagBackend() == null)
					model.setFlagBackend("");
				if(model.getFlagApp() == null)
					model.setFlagApp("");
				
				text_flag_app.setText(model.getFlagApp());
				text_flag_backend.setText(model.getFlagBackend());
				portText.setText(model.getAppPort());

				checkForFinish();
			}
			while (!this.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Display display;
	private boolean edit;
	private LaunchConfigurationModel model;

	/**
	 * 
	 * @param model
	 *            may be null, else start edit view
	 */
	public void openShell(LaunchConfigurationModel model) {
		if (model != null) {
			edit = true;
			this.model = model;

		} else {
			edit = false;
		}
		openShell(display);
	}

	private ConfigurationShell shell;
	private CCombo platformCombo;
	private Button btnCheckButton;
	private Button btnSaveConfig;

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public ConfigurationShell(Display display) {

		super(display, SWT.SHELL_TRIM);
		this.display = display;
		this.shell = this;
		setLayout(new GridLayout(1, false));

		Group appGroup = new Group(this, SWT.NONE);
		GridData gd_appGroup = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_appGroup.heightHint = 155;
		appGroup.setLayoutData(gd_appGroup);
		appGroup.setLayout(new GridLayout(3, false));

		Label lblName = new Label(appGroup, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name");

		textName = new Text(appGroup, SWT.BORDER);

		textName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (!textName.getText().equals("")) {
					nameSet = true;
				} else {
					nameSet = false;
				}
				checkForFinish();

			}
		});
		textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(appGroup, SWT.NONE);

		Label lblApp = new Label(appGroup, SWT.NONE);
		lblApp.setText("App");

		appPathText = new Text(appGroup, SWT.BORDER);
		GridData gd_appPathText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_appPathText.widthHint = 192;
		appPathText.setLayoutData(gd_appPathText);
		appPathText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (!appPathText.getText().equals("")) {
					appPathSet = true;
				} else {
					appPathSet = false;
				}
				checkForFinish();

			}
		});

		Button btnBrowseApp = new Button(appGroup, SWT.NONE);
		btnBrowseApp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IFile selectedFile = null;
				String path = null;
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, new WorkbenchLabelProvider(),
						new BaseWorkbenchContentProvider());
				dialog.setTitle("Tree Selection");
				dialog.setMessage("Select the elements from the tree:");
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				if (dialog.open() == ElementTreeSelectionDialog.OK) {
					Object[] result = dialog.getResult();
					if (result.length == 1) {
						if (result[0] instanceof IFile) {

							selectedFile = (IFile) result[0];

							path = selectedFile.getFullPath().toString();

						} else {
							showMessage("Please select an app.");
						}
					}
				}

				if (path != null) {
					path = "platform:/resource".concat(path);
					appPathText.setText(path);
				}
			}

		});

		btnBrowseApp.setText("Browse");

		Label lblPort = new Label(appGroup, SWT.NONE);
		lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPort.setText("Open Flow Port");

		portText = new Text(appGroup, SWT.BORDER);
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(appGroup, SWT.NONE);

		Label lblFlags = new Label(appGroup, SWT.NONE);
		lblFlags.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFlags.setText("Flag Backend");

		text_flag_backend = new Text(appGroup, SWT.BORDER);
		text_flag_backend.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(appGroup, SWT.NONE);
		
		Label lblFlagApp = new Label(appGroup, SWT.NONE);
		lblFlagApp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFlagApp.setText("Flag App");
		
		text_flag_app = new Text(appGroup, SWT.BORDER);
		text_flag_app.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(appGroup, SWT.NONE);

		Group platform = new Group(this, SWT.NONE);
		platform.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		platform.setLayout(new GridLayout(2, false));

		Label lblAppController = new Label(platform, SWT.NONE);
		GridData gd_lblAppController = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblAppController.widthHint = 91;
		lblAppController.setLayoutData(gd_lblAppController);
		lblAppController.setText("Platform");

		platformCombo = new CCombo(platform, SWT.BORDER);
		GridData gd_platformCombo = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_platformCombo.heightHint = 23;
		gd_platformCombo.widthHint = 191;
		platformCombo.setLayoutData(gd_platformCombo);
		platformCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = platformCombo.getSelectionIndex();
				if (index != -1) {
					platformSet = true;

				} else {
					platformSet = false;
				}
				checkForFinish();
			}
		});
		setComboboxContent(platformCombo);

		Composite checkBoxComposite = new Composite(this, SWT.NONE);
		checkBoxComposite.setLayout(new GridLayout(1, false));
		checkBoxComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		btnCheckButton = new Button(checkBoxComposite, SWT.CHECK);
		btnCheckButton.setText("Use Network Engine");

		Composite btnComposite = new Composite(this, SWT.NONE);
		GridData gd_btnComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_btnComposite.widthHint = 210;
		btnComposite.setLayoutData(gd_btnComposite);
		btnComposite.setLayout(new GridLayout(3, false));
		new Label(btnComposite, SWT.NONE);

		Button btnCancle = new Button(btnComposite, SWT.NONE);
		btnCancle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model = null;
				shell.dispose();
			}
		});
		btnCancle.setText("Cancel");

		btnSaveConfig = new Button(btnComposite, SWT.NONE);
		btnSaveConfig.setEnabled(false);

		btnSaveConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!edit) {
					model = new LaunchConfigurationModel();
					model.setID(UUID.randomUUID().toString());
				}

				if (!appPathText.getText().equals("")) {

					model.setAppPath(appPathText.getText());

					if (platformCombo.getSelectionIndex() != -1) {

						if (btnCheckButton.getSelection()) {
							model.setPlatform(NetIDE.CONTROLLER_ENGINE);
							model.setClientController(platformCombo.getItem(platformCombo.getSelectionIndex()));

						} else {
							model.setPlatform(platformCombo.getItem(platformCombo.getSelectionIndex()));
							model.setClientController("");
						}

					}
					model.setAppPort(portText.getText());

				
					String[] tmp = model.getAppPath().split("/");
					String appName = tmp[tmp.length - 1];
					model.setAppName(appName);
					model.setName(textName.getText());

					model.setFlagBackend(text_flag_backend.getText());
					model.setFlagApp(text_flag_app.getText());
				}
				shell.dispose();

			}
		});
		btnSaveConfig.setText("Save Config");
		createContents();
	}

	private boolean appPathSet = false;
	private boolean platformSet = false;
	private boolean nameSet = false;

	private void checkForFinish() {
		if (appPathSet && platformSet && nameSet)
			btnSaveConfig.setEnabled(true);
		else
			btnSaveConfig.setEnabled(false);
	}

	private Text appPathText;
	private Text portText;
	private Text textName;
	private Text text_flag_backend;
	private Text text_flag_app;

	/**
	 * 
	 * @return 0 = topology, 1 = platform, 2 = clientController, 3 = appPort, 4
	 *         = appPath, null if content wasn't set or an error occurred
	 */
	public LaunchConfigurationModel getModel() {
		return this.model;
	}

	private void setComboboxContent(CCombo combo) {

		combo.add(NetIDE.CONTROLLER_FLOODLIGHT);

		combo.add(NetIDE.CONTROLLER_PYRETIC);
		combo.add(NetIDE.CONTROLLER_RYU);

	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Choose App Run Configuration");
		setSize(396, 363);

	}

	private void showMessage(String msg) {
		MessageDialog.openInformation(shell, "NetIDE Workbench View", msg);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
