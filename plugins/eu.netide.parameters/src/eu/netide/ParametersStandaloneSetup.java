/*
* generated by Xtext
*/
package eu.netide;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class ParametersStandaloneSetup extends ParametersStandaloneSetupGenerated{

	public static void doSetup() {
		new ParametersStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

