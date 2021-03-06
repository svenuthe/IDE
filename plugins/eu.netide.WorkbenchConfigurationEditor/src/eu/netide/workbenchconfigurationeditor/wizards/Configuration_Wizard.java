package eu.netide.workbenchconfigurationeditor.wizards;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class Configuration_Wizard extends Wizard implements INewWizard {

	private Configuration_Wizard_Page page;

	public Configuration_Wizard() {
		super();
		setWindowTitle("new Configuration Wizard");
	}

	@Override
	public String getWindowTitle() {
		return "Configuration Wizard";
	}

	@Override
	public void addPages() {

		page = new Configuration_Wizard_Page();
		addPage(page);

	}

	@Override
	public boolean performFinish() {

		return createNewFile(page.getFileName());

	}

	private boolean createNewFile(final String fileName) {

		String filePath = getPathFromSelection();
		filePath += "/" + fileName + ".wb";

		filePath = new Path(filePath).toString();

		File file = new File(filePath);

		if (!file.exists()) {
			try {
				file.createNewFile();

				//writeContent(file);

				try {
					
					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);

				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

//	private void writeContent(File file) {
//		try {
//
//			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//
//			// root elements
//			Document doc = docBuilder.newDocument();
//			Element rootElement = doc.createElement("workbench");
//			doc.appendChild(rootElement);
//
//			
//			//XmlHelper.saveContentToXml(doc, file);
//			
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		}
//	}

	private ISelection selection;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;

		getPathFromSelection();
	}

	private String getPathFromSelection() {

		String path = "";
		if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {

			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
		
			if (obj instanceof IPackageFragment) {
				IPackageFragment pf = (IPackageFragment) obj;
				path = pf.getResource().getLocation().toString();

			} else {
				if (obj instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot pfr = (IPackageFragmentRoot) obj;
					path = pfr.getResource().getLocation().toString();
				} else {
					if (obj instanceof IJavaProject) {
						IJavaProject p = (IJavaProject) obj;
						path = p.getResource().getLocation().toString();

					} else if (obj instanceof IResource) {
						IResource r = (IResource) obj;
						path = r.getLocation().toString();

					} else {
						path = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0].getLocation().toString();
					}
				}

			}

		} else {
			path = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0].getLocation().toString();
		}
		return path;

	}

}
