package eu.netide.toolpanel.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import eu.netide.configuration.launcher.starters.backends.Backend;
import eu.netide.configuration.launcher.starters.backends.SshBackend;
import eu.netide.configuration.launcher.starters.impl.ProfilerStarter;
import eu.netide.configuration.launcher.starters.impl.VerificatorStarter;
import eu.netide.configuration.utils.NetIDEUtil;
import eu.netide.lib.netip.Message;
import eu.netide.lib.netip.MessageType;
import eu.netide.toolpanel.connectors.ProfilerConnector;
import eu.netide.zmq.hub.client.IZmqNetIpListener;
import eu.netide.zmq.hub.server.IZmqPubSubHub;
import eu.netide.zmq.hub.server.IZmqSendReceiveHub;
import eu.netide.zmq.hub.server.ZmqHubManager;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class ToolPanel extends ViewPart implements IZmqNetIpListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "eu.netide.toolpanel.views.Toolpanel";
	private Composite composite;
	private Backend backend;
	private IFile file;
	private Text textVerificatorOutput;
	private String verifierOutput;

	private IZmqPubSubHub verifierOutputHub;
	private IZmqSendReceiveHub verifierCommandSender;
	private Text textLoops;
	private Text text;
	private ProfilerConnector profilerConnector;
	private String address;
	private String toolPath;

	/**
	 * The constructor.
	 */
	public ToolPanel() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		


		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		ScrolledComposite scrolledComposite = new ScrolledComposite(composite,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite_1 = new Composite(scrolledComposite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, true));

		Group grpVerificator = new Group(composite_1, SWT.NONE);
		GridLayout gl_grpVerificator = new GridLayout(3, false);
		gl_grpVerificator.marginWidth = 1;
		grpVerificator.setLayout(gl_grpVerificator);
		grpVerificator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpVerificator.setText("Verificator");
		grpVerificator.setBounds(0, 0, 66, 66);

		Button btnStartVerificator = new Button(grpVerificator, SWT.NONE);
		btnStartVerificator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verifierOutputHub.setRunning(false);
				VerificatorStarter vs = new VerificatorStarter(NetIDEUtil.toPlatformUri(file), backend, toolPath,
						new NullProgressMonitor());
				vs.syncStart();
				verifierOutputHub.setRunning(true);

			}
		});
		btnStartVerificator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		btnStartVerificator.setBounds(0, 0, 70, 27);
		btnStartVerificator.setText("Start Verificator");

		Button btnCoreInfo = new Button(grpVerificator, SWT.NONE);
		btnCoreInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verifierCommandSender.send("1");
			}
		});
		btnCoreInfo.setText("Show Core Info");

		Button btnLoopDetect = new Button(grpVerificator, SWT.NONE);
		btnLoopDetect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verifierCommandSender.send("2");
			}
		});
		btnLoopDetect.setText("Detect Loops");

		Label lblCoreStatus = new Label(grpVerificator, SWT.NONE);
		lblCoreStatus.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCoreStatus.setText("Core Status");

		textVerificatorOutput = new Text(grpVerificator, SWT.BORDER);
		textVerificatorOutput.setEditable(false);
		textVerificatorOutput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblLoops = new Label(grpVerificator, SWT.NONE);
		lblLoops.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLoops.setText("Loops");

		textLoops = new Text(grpVerificator, SWT.BORDER);
		textLoops.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Group grpProfiler = new Group(composite_1, SWT.NONE);
		grpProfiler.setLayout(new GridLayout(1, false));
		grpProfiler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpProfiler.setText("Profiler");
		grpProfiler.setBounds(0, 0, 66, 66);

		Composite composite_3 = new Composite(grpProfiler, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite_3.setLayout(new GridLayout(1, false));

		Button btnStartProfiler = new Button(composite_3, SWT.NONE);
		btnStartProfiler.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (profilerConnector != null) {
					profilerConnector.destroy();
				}
				ProfilerStarter ps = new ProfilerStarter("Profiler Starter", NetIDEUtil.toPlatformUri(file), backend, toolPath,
						new NullProgressMonitor());
				ps.syncStart();
				profilerConnector = new ProfilerConnector(file, address);
			}
		});
		btnStartProfiler.setText("Start Profiler");

		Group composite_2 = new Group(grpProfiler, SWT.NONE);
		composite_2.setText("Port Statistics");
		GridLayout gl_composite_2 = new GridLayout(4, false);
		gl_composite_2.marginWidth = 0;
		composite_2.setLayout(gl_composite_2);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
						
								Label lblUpdateInterval = new Label(composite_2, SWT.NONE);
								lblUpdateInterval.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
								lblUpdateInterval.setText("Update Interval:");
						new Label(composite_2, SWT.NONE);
						
								text = new Text(composite_2, SWT.BORDER);
								text.setText("1");
								text.addModifyListener(new ModifyListener() {
									public void modifyText(ModifyEvent e) {
										profilerConnector.setPollInterval(Double.parseDouble(text.getText()));
									}
								});
								text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						new Label(composite_2, SWT.NONE);
						
						Button btnPollProfiler = new Button(composite_2, SWT.CHECK);
						btnPollProfiler.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								if (btnPollProfiler.getSelection())
									profilerConnector.startPolling(Double.parseDouble(text.getText()));
								else
									profilerConnector.stopPolling();
							}
						});
						btnPollProfiler.setText("Poll Profiler");
						new Label(composite_2, SWT.NONE);
						
								Button btnRecordButton = new Button(composite_2, SWT.CHECK);
								btnRecordButton.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e) {
										profilerConnector.setRecording(btnRecordButton.getSelection());				
									}
								});
								btnRecordButton.setText("Record");
		scrolledComposite.setContent(composite_1);
		scrolledComposite.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		composite.setFocus();
	}

	public void setBackend(Backend backend) {
		this.backend = backend;
		address = "localhost";
		if (backend instanceof SshBackend) {
			SshBackend sshBackend = (SshBackend) backend;
			address = sshBackend.getHostname();
		}
		verifierOutputHub = ZmqHubManager.instance.getPubSubHub("Verificator",
				String.format("tcp://%s:%s", address, 5560));
		verifierOutputHub.setRunning(false);
		verifierOutputHub.setRunning(true);
		verifierOutputHub.register(this);

		verifierCommandSender = ZmqHubManager.instance.getSendReceiveHub("Verifier Command",
				String.format("tcp://%s:%s", address, 30556));
	}

	public void setFile(IFile file) {
		this.file = file;

	}

	@Override
	public void dispose() {
		super.dispose();
		verifierOutputHub.setRunning(false);
		verifierOutputHub.remove(this);

	}

	@Override
	public void update(Message msg) {
		if (msg.getHeader().getMessageType() == MessageType.MANAGEMENT) {
			UIJob job = new UIJob("Set Text") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (msg.getPayload()[0] == 27)
						textVerificatorOutput.setText(new String(msg.getPayload()));
					else
						textLoops.setText(new String(msg.getPayload()));
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	public void setSshModel(String sshModelAtIndex) {
		this.toolPath = sshModelAtIndex;
		
	}

}
