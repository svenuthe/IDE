/*
 * generated by Xtext
 */
package eu.netide;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class SysReqStandaloneSetup extends SysReqStandaloneSetupGenerated{

	public static void doSetup() {
		new SysReqStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

