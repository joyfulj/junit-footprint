package junitfootprint.footprint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


public class FootPrintTestRunListener extends org.eclipse.jdt.junit.TestRunListener {

	@Override
	public void testCaseFinished(ITestCaseElement element) {

		String testClassName = element.getTestClassName();
		String testMethodName = element.getTestMethodName();
		boolean result = element.getTestResult(false) == Result.OK ? true : false;

		FootPrintInfo info = new FootPrintInfo();
		info.setTypeName(testClassName);
		info.setMethodName(testMethodName);
		info.setSuccess(result);
		info.setTime(getCurrentTime());
		IProject project = element.getTestRunSession().getLaunchedProject().getProject();

		info.setiFile(getTestCase(project, testClassName));

		FootPrintViewPart.putFootPrintInfo(info);

		showFootPrintView();

	}
	
	private void showFootPrintView() {
		
		new Thread(new Runnable() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
						for (IWorkbenchWindow window : windows) {
							IWorkbenchPage activePage = window.getActivePage();
							if (activePage != null) {
								try {
									activePage.showView(FootPrintViewPart.ID);
								} catch (PartInitException e) {
//									LogUtils.error(e);
								}

								IViewReference[] viewReferences = activePage.getViewReferences();
								for (IViewReference ref : viewReferences) {
									if (FootPrintViewPart.ID.equals(ref.getId())) {
										IViewPart view = ref.getView(false);
										view.setFocus();
									}
								}
							}
						}
					}
				});
			}
		}).start();
	}

	private IFile getTestCase(IProject project, String testClassName) {
		List<IPath> sourceFolderList = getSourceFolders(project);

		IFile iFile = null;
		for (IPath sourceFolder : sourceFolderList) {
			String folderPath = sourceFolder.toPortableString();
			
			String myPath = folderPath + "/" + testClassName.replaceAll("[\\.]", "/") + ".java";
			
			String filePath = myPath.substring(("/" + project.getName()).length()); 
			
			iFile = project.getFile(filePath);
			if (iFile != null && iFile.exists()) {
				return iFile;
			}
		}
		throw new RuntimeException("No Testcase exist.");
	}
	
	private List<IPath> getSourceFolders(IProject project) {
		List<IPath> sourceFolders = new ArrayList<IPath>();
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] resources = javaProject.getRawClasspath();
			for (IClasspathEntry cpe : resources) {
				if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					sourceFolders.add(cpe.getPath());
				}
			}
		} catch (JavaModelException e) {
//			LogUtils.error(e);
		}
		return sourceFolders;

	}

	private String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(System.currentTimeMillis());
	}

}
