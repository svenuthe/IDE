package eu.netide.configuration.launcher.starters

import Topology.Controller
import eu.netide.configuration.launcher.starters.backends.Backend
import eu.netide.configuration.launcher.starters.backends.VagrantBackend
import eu.netide.configuration.launcher.starters.impl.DebuggerStarter
import eu.netide.configuration.launcher.starters.impl.FloodlightBackendStarter
import eu.netide.configuration.launcher.starters.impl.MininetStarter
import eu.netide.configuration.launcher.starters.impl.OdlShimStarter
import eu.netide.configuration.launcher.starters.impl.PoxShimStarter
import eu.netide.configuration.launcher.starters.impl.PoxStarter
import eu.netide.configuration.launcher.starters.impl.PyreticBackendStarter
import eu.netide.configuration.launcher.starters.impl.PyreticStarter
import eu.netide.configuration.launcher.starters.impl.RyuBackendStarter
import eu.netide.configuration.launcher.starters.impl.RyuShimStarter
import eu.netide.configuration.launcher.starters.impl.RyuStarter
import eu.netide.configuration.launcher.starters.impl.CoreStarter
import eu.netide.configuration.launcher.starters.impl.EmulatorStarter
import eu.netide.configuration.utils.NetIDE
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration

class StarterFactory {

	private Backend backend

	new() {
		this(new VagrantBackend)
	}

	new(Backend backend) {
		this.backend = backend
	}

	public def IStarter createSingleControllerStarter(ILaunchConfiguration configuration, Controller controller,
		IProgressMonitor monitor) {
		var controllerplatform = configuration.attributes.get("controller_platform_" + controller.name) as String

		if (controllerplatform.equals(NetIDE.CONTROLLER_ENGINE)) {
		} else {
			var IStarter starter
			switch controllerplatform {
				case NetIDE.CONTROLLER_POX:
					starter = new PoxStarter(configuration, controller, monitor)
				case NetIDE.CONTROLLER_RYU:
					starter = new RyuStarter(configuration, controller, monitor)
				case NetIDE.CONTROLLER_PYRETIC:
					starter = new PyreticStarter(configuration, controller, monitor)
			}
			starter.setBackend(backend)
			return starter
		}
	}

	public def IStarter createShimStarter(ILaunchConfiguration configuration, Controller controller,
		IProgressMonitor monitor) {

		var serverplatform = configuration.attributes.get("controller_platform_target_" + controller.name) as String
		var IStarter starter
		switch serverplatform {
			case NetIDE.CONTROLLER_POX:
				starter = new PoxShimStarter(configuration, controller, monitor)
			case NetIDE.CONTROLLER_RYU:
				starter = new RyuShimStarter(configuration, controller, monitor)
			case NetIDE.CONTROLLER_ODL:
				starter = new OdlShimStarter(configuration, controller, monitor)
		}
		starter.setBackend(backend)
		return starter

	}

	public def IStarter createBackendStarter(ILaunchConfiguration configuration, Controller controller,
		IProgressMonitor monitor) {

		var clientplatform = configuration.attributes.get("controller_platform_source_" + controller.name) as String
		var IStarter starter
		switch clientplatform {
			case NetIDE.CONTROLLER_FLOODLIGHT:
				starter = new FloodlightBackendStarter(configuration, controller, monitor)
			case NetIDE.CONTROLLER_RYU:
				starter = new RyuBackendStarter(configuration, controller, monitor)
			case NetIDE.CONTROLLER_PYRETIC:
				starter = new PyreticBackendStarter(configuration, controller, monitor)
		}
		starter.backend = backend
		return starter
	}

	public def IStarter createMininetStarter(ILaunchConfiguration configuration, IProgressMonitor monitor) {
		var starter = new MininetStarter(configuration, monitor)
		starter.backend = backend
		return starter
	}

	public def createDebuggerStarter(ILaunchConfiguration configuration, IProgressMonitor monitor) {
		var starter = new DebuggerStarter(configuration, monitor)
		starter.backend = backend
		return starter
	}
	
	public def createCoreStarter(ILaunchConfiguration configuration, IProgressMonitor monitor) {
		var starter = new CoreStarter(configuration, monitor)
		starter.backend = backend
		return starter
	}
	
	public def createEmulatorStarter(ILaunchConfiguration configuration, IProgressMonitor monitor) {
		var starter = new EmulatorStarter(configuration, monitor)
		starter.backend = backend
		return starter
	}

}