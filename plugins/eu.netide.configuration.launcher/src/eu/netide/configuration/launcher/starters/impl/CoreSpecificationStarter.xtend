package eu.netide.configuration.launcher.starters.impl

import eu.netide.configuration.launcher.starters.backends.Backend
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration
import eu.netide.configuration.utils.NetIDE

class CoreSpecificationStarter extends Starter {

	private String compositionPath
	private String corePath = NetIDE.CORE_KARAF

	@Deprecated
	new(ILaunchConfiguration configuration, String compositionPath, IProgressMonitor monitor) {
		super("Core Loader", configuration, monitor)
		this.compositionPath = compositionPath
	}

	new(String compositionPath, Backend backend, String corePath, IProgressMonitor monitor) {
		super("Core Loader", compositionPath, backend, monitor)
		this.compositionPath = compositionPath
		if (corePath != null && corePath != "")
			this.corePath = this.getValidPath(corePath)
	}

	new(String compositionPath, Backend backend, IProgressMonitor monitor) {
		this(compositionPath, backend, "", monitor)
	}

	override getCommandLine() {
		var file = compositionPath.IFile
		var fullpath = file.fullPath
		var guestPath = "$HOME/netide/" + fullpath.removeFirstSegments(1)

		var cmd = String.format("bash -c \'cd %s && ./client netide:loadcomposition %s\'", corePath, guestPath)

		return cmd
	}

}
