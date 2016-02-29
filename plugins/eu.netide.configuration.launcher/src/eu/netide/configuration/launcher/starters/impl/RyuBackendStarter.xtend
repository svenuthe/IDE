package eu.netide.configuration.launcher.starters.impl

import Topology.Controller
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration

class RyuBackendStarter extends ControllerStarter {

	new(ILaunchConfiguration configuration, Controller controller, IProgressMonitor monitor) {
		super("Ryu Backend", configuration, controller, monitor)
		name = String.format("%s (%s)", name, appPath.lastSegment)
	}

	override getEnvironmentVariables() {
		"PYTHONPATH=$PYTHONPATH:Engine/ryu-backend:Engine/libraries/netip/python"
	}
	
	override getCommandLine() {
		return String.format(
			"sudo ryu-manager --ofp-tcp-listen-port 7733 ~/netide/Engine/ryu-backend/ryu-backend.py ~/netide/controllers/%s/%s",
			appPath.removeFileExtension.lastSegment, appPath.lastSegment)
	}

}