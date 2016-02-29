package eu.netide.configuration.launcher.starters.impl

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration

class DebuggerStarter extends Starter {
	
	new(ILaunchConfiguration configuration, IProgressMonitor monitor) {
		super("Debugger", configuration, monitor)
	}
	
	override getCommandLine() {
		String.format("sudo python ~/netide/Tools/debugger/Ryu_shim/debugger.py")
	}
	
}