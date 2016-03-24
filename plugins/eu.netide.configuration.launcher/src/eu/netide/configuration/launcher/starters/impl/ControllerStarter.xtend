package eu.netide.configuration.launcher.starters.impl

import Topology.Controller
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.xtend.lib.annotations.Accessors

import static extension eu.netide.configuration.utils.NetIDEUtil.absolutePath
import eu.netide.configuration.launcher.starters.backends.VagrantBackend
import org.eclipse.core.resources.ResourcesPlugin

abstract class ControllerStarter extends Starter {

	@Accessors(PROTECTED_GETTER)
	private Controller controller

	@Accessors(PROTECTED_GETTER)
	private IPath appPath

	@Accessors(PROTECTED_GETTER)
	private String controllerplatform

	@Accessors(PUBLIC_GETTER,PUBLIC_SETTER)
	private int port

	@Deprecated
	new(String name, ILaunchConfiguration configuration, Controller controller, IProgressMonitor monitor) {
		super(name, configuration, monitor)

		this.controller = controller

		this.controllerplatform = if (controller != null)
			configuration.attributes.get("controller_platform_" + controller.name) as String
		else
			null

		this.appPath = if (controller != null)
			(configuration.attributes.get("controller_data_" + controller.name + "_" + controllerplatform) as String).
				absolutePath
		else
			null
	}

	new(String name, int port, String appPath, IProgressMonitor monitor) {
		super(name, appPath, new VagrantBackend, monitor)

		this.controller = controller

		this.controllerplatform = ""

		this.port = port

		var tempAppPath = ResourcesPlugin.workspace.root.findMember(appPath.replaceFirst("platform:/resource", "")).projectRelativePath

		this.appPath = tempAppPath
	}

}
