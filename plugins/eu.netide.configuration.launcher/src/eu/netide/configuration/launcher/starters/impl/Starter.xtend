package eu.netide.configuration.launcher.starters.impl

import Topology.Controller
import eu.netide.configuration.launcher.starters.IStarter
import eu.netide.configuration.preferences.NetIDEPreferenceConstants
import eu.netide.configuration.utils.NetIDE
import java.io.File
import java.util.Map
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Platform
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.model.IProcess
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants
import org.eclipse.xtend.lib.annotations.Accessors
import java.util.ArrayList
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import java.util.HashMap
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.IStatus
import org.eclipse.debug.core.RefreshUtil
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.common.util.URI
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager
import org.eclipse.tm.terminal.connector.local.launcher.LocalLauncherDelegate
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalTabListener

abstract class Starter implements IStarter {

	@Accessors(#[PROTECTED_GETTER, PROTECTED_SETTER])
	private IProcess process

	@Accessors(PROTECTED_GETTER)
	private ILaunchConfiguration configuration

	@Accessors(PROTECTED_GETTER)
	private ILaunch launch

	@Accessors(PROTECTED_GETTER)
	private Controller controller

	@Accessors(PROTECTED_GETTER)
	private File workingDir

	@Accessors(PROTECTED_GETTER)
	private String vagrantpath

	@Accessors(PUBLIC_GETTER, PROTECTED_SETTER)
	private String name

	protected IProgressMonitor monitor

	new(String name, ILaunch launch, ILaunchConfiguration configuration, IProgressMonitor monitor) {
		this.name = name
		this.launch = launch
		this.configuration = configuration
		this.controller = controller
		this.monitor = monitor

		this.vagrantpath = Platform.getPreferencesService.getString(NetIDEPreferenceConstants.ID,
			NetIDEPreferenceConstants.VAGRANT_PATH, "", null)

		var path = configuration.attributes.get("topologymodel") as String

		this.workingDir = path.getIFile.project.location.append("/gen" + NetIDE.VAGRANTFILE_PATH).toFile
	}

	override void setLaunchConfiguration(ILaunchConfiguration configuration) {
		this.configuration = configuration
	}

	public override syncStart() {
		var line = getCommandLine()
		var env = null
		var ts = TerminalServiceFactory.service

		var delegate = new LocalLauncherDelegate

		var options = newHashMap(
			// ITerminalsConnectorConstants.PROP_ID -> ""+Math.random as Object,
			ITerminalsConnectorConstants.PROP_TITLE -> name as Object,
			ITerminalsConnectorConstants.PROP_FORCE_NEW -> true as Object,
			ITerminalsConnectorConstants.PROP_DELEGATE_ID ->
				"org.eclipse.tm.terminal.connector.local.launcher.local" as Object,
			ITerminalsConnectorConstants.PROP_PROCESS_PATH -> vagrantpath as Object,
			ITerminalsConnectorConstants.PROP_PROCESS_MERGE_ENVIRONMENT -> true as Object,
			ITerminalsConnectorConstants.PROP_PROCESS_ARGS -> String.format("ssh -c \"%s\" -- -t", line) as Object,
			ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR -> workingDir.absolutePath as Object
		)

		ts.openConsole(options, null)

	// startProcess(line, workingDir, new Path(vagrantpath), monitor, launch, configuration, env)
	}

	public override asyncStart() {
		val vagrantpath = Platform.getPreferencesService.getString(NetIDEPreferenceConstants.ID,
			NetIDEPreferenceConstants.VAGRANT_PATH, "", null)

		val env = null

		var cmdline = getCommandLine()

		var controllerthread = new Thread() {
			var File workingDir
			var String cmdline

			def setParameters(File wd, String cl) {
				this.workingDir = wd
				this.cmdline = cl
			}

			override run() {
				super.run()

				var line = getCommandLine()
				var env = null
				var ts = TerminalServiceFactory.service

				var delegate = new LocalLauncherDelegate

				var options = newHashMap(
//					ITerminalsConnectorConstants.PROP_ID -> ""+Math.random as Object,
					ITerminalsConnectorConstants.PROP_TITLE -> name as Object,
					ITerminalsConnectorConstants.PROP_FORCE_NEW -> true as Object,
					ITerminalsConnectorConstants.PROP_DELEGATE_ID ->
						"org.eclipse.tm.terminal.connector.local.launcher.local" as Object,
					ITerminalsConnectorConstants.PROP_PROCESS_PATH -> vagrantpath as Object,
					ITerminalsConnectorConstants.PROP_PROCESS_MERGE_ENVIRONMENT -> true as Object,
					ITerminalsConnectorConstants.PROP_PROCESS_ARGS ->
						String.format("ssh -c \"%s\" -- -t", line) as Object,
					ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR -> workingDir.absolutePath as Object
				)

				ts.openConsole(options, null)

			// startProcess(cmdline, workingDir, new Path(vagrantpath), new NullProgressMonitor, launch, configuration,
			// env)
			}
		}

		controllerthread.setParameters(workingDir, cmdline)
		Thread.sleep(2000)
		controllerthread.start
	}

	override void stop() {
		process.launch.terminate
	}

	def ArrayList<String> cmdLineArray(String cline) {

		var location = new Path(
			Platform.getPreferencesService.getString(NetIDEPreferenceConstants.ID,
				NetIDEPreferenceConstants.VAGRANT_PATH, "", null))

		newArrayList(location.toOSString, "ssh", "-c", "screen -S " + name.replaceAll("[ ()]", "_") + " " + cline, "--",
			"-tt")
	}

	/**
	 * Starts a process 
	 */
	def startProcess(ArrayList<String> cmdline, File workingDir, Path location, IProgressMonitor monitor,
		ILaunch launch, ILaunchConfiguration configuration, String[] env) {
		var p = DebugPlugin.exec(cmdline, workingDir, env)

		var IProcess process = null;

		// add process type to process attributes
		var Map<String, String> processAttributes = new HashMap<String, String>();
		var programName = location.lastSegment();
		var ext = location.getFileExtension();
		if (ext != null) {
			programName = programName.substring(0, programName.length() - (ext.length() + 1));
		}
		programName = programName.toLowerCase();
		var programLabel = String.format("%s", this.name)
		processAttributes.put(IProcess.ATTR_PROCESS_LABEL, programLabel)
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName)

		if (p != null) {
			monitor.beginTask("Vagrant up", 0);
			process = DebugPlugin.newProcess(launch, p, location.toOSString(), processAttributes);
		}
		if (p == null || process == null) {
			if (p != null) {
				p.destroy();
			}
			throw new CoreException(new Status(IStatus.ERROR, "eu.netide.core.launcher", "No process available"))
		}

		process.setAttribute(IProcess.ATTR_CMDLINE, generateCommandLine(cmdline))
		this.process = process

		while (!process.isTerminated()) {
			try {
				if (monitor.isCanceled()) {
					var quitline = newArrayList(location.toOSString, "ssh", "-c", "screen", "-X", "-S", name, "quit")
					startProcess(quitline, workingDir, location, monitor, launch, configuration, env)
					process.launch.terminate
				}
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// refresh resources
			RefreshUtil.refreshResources(configuration, monitor)
		}

	}

	def generateCommandLine(String[] commandLine) {
		if (commandLine.length < 1) {
			return ""
		}

		val buf = new StringBuffer()

		commandLine.forEach [ a |
			buf.append(' ')
			var characters = a.toCharArray
			val command = new StringBuffer()
			var containsSpace = false
			for (c : characters) {
				if (c == '\"') {
					command.append('\\');
				} else if (c == ' ') {
					containsSpace = true;
				}
				command.append(c)
			}
			if (containsSpace) {
				buf.append('\"');
				buf.append(command);
				buf.append('\"');
			} else {
				buf.append(command);
			}
		]

		return buf.toString
	}

	def getIFile(String s) {

		var resSet = new ResourceSetImpl
		var res = resSet.getResource(URI.createURI(s), true)

		var eUri = res.getURI()
		if (eUri.isPlatformResource()) {
			var platformString = eUri.toPlatformString(true)
			return ResourcesPlugin.getWorkspace().getRoot().findMember(platformString)
		}
		return null
	}

}