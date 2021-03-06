package eu.netide.configuration.launcher.starters.impl

import Topology.Controller
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.xtend.lib.annotations.Accessors
import eu.netide.configuration.utils.NetIDE

class RyuStarter extends ControllerStarter {

	@Accessors(PUBLIC_GETTER,PUBLIC_SETTER)
	private String appFolderPath
	private String appFlag;

	@Deprecated
	new(ILaunchConfiguration configuration, Controller controller, IProgressMonitor monitor) {
		super("Ryu", configuration, controller, monitor)
		name = String.format("%s (%s)", name, appPath.lastSegment)
	}

	new(int port, String appPath, IProgressMonitor monitor, String appFolderPath, String appFlag) {
		super("Ryu", port, appPath, monitor)
		if (appFolderPath != null && appFolderPath != "")
			// TODO: validate appFolderPath format
			this.appFolderPath = super.getValidPath(appFolderPath)
		else
			this.appFolderPath = NetIDE.APP_FOLDER

		if (appFlag != null && appFlag != "")
			this.appFlag = appFlag
		else
			this.appFlag = ""

		name = String.format("%s (%s)", name, this.appPath.lastSegment)
	}

	override getCommandLine() {
		var ryuline = String.format("ryu-manager %s --ofp-tcp-listen-port=%d %s%s", appFlag, port, this.appFolderPath,
			getAppPath.removeFirstSegments(1))

		return ryuline
	}

}
