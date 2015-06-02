package junitfootprint.junit;

import junitfootprint.footprint.FootPrintInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class JUnitLaunch {

	private static final String RUN_FROM_FOOT_PRINT = "RunFromFootPrint";
	private static final String EMPTY_STRING = "";
	private static final String JUNIT4_TEST_KIND_ID = "org.eclipse.jdt.junit.loader.junit4";

	public JUnitLaunch() {
		// default
	}

	public void performLaunch(IJavaElement element, String mode, FootPrintInfo footPrintInfo) throws InterruptedException, CoreException {

		ILaunchConfigurationType configType = getLaunchManager().getLaunchConfigurationType(JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION);
		ILaunchConfigurationWorkingCopy temporary = configType.newInstance(null,
				getLaunchManager().generateLaunchConfigurationName(RUN_FROM_FOOT_PRINT));

		temporary.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, footPrintInfo.getTypeName());
		temporary.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, element.getJavaProject().getElementName());
		temporary.setAttribute(JUnitLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
		temporary.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, EMPTY_STRING);
		
		// Only JUnit4. if in case it is JUnit3, use "org.eclipse.jdt.junit.loader.junit3"
		temporary.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, JUNIT4_TEST_KIND_ID);
		
		JUnitMigrationDelegate.mapResources(temporary);
		temporary.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
		
		// Specify method. 
		temporary.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, footPrintInfo.getMethodName()); 

		ILaunchConfiguration config = temporary.doSave();
		
		ILaunch launch = config.launch(mode, null);
		launch.getLaunchConfiguration().delete();
//		DebugUITools.launch(config, mode);
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public static class JUnitLaunchConfigurationConstants {

		public static final String ID_JUNIT_APPLICATION = "org.eclipse.jdt.junit.launchconfig";

		public static final String ATTR_KEEPRUNNING = JUnitCorePlugin.PLUGIN_ID + ".KEEPRUNNING_ATTR";

		public static final String ATTR_TEST_CONTAINER = JUnitCorePlugin.PLUGIN_ID + ".CONTAINER";

		public static final String ATTR_TEST_RUNNER_KIND = JUnitCorePlugin.PLUGIN_ID + ".TEST_KIND";

		public static final String ATTR_TEST_METHOD_NAME = JUnitCorePlugin.PLUGIN_ID + ".TESTNAME";

	}
	
	public static class JUnitCorePlugin {
		
		public static final String PLUGIN_ID = "org.eclipse.jdt.junit";
		
		public final static String JUNIT4_ANNOTATION_NAME = "org.junit.Test";
		
		public final static String TEST_SUPERCLASS_NAME = "junit.framework.TestCase";
		
		public static final String CORE_PLUGIN_ID = "org.eclipse.jdt.junit.core";
		
	}

	public static class JUnitMigrationDelegate {
		public static void mapResources(ILaunchConfigurationWorkingCopy config) throws CoreException {
			IResource resource = getResource(config);
			if (resource == null) {
				config.setMappedResources(null);
			} else {
				config.setMappedResources(new IResource[] { resource });
			}
		}

		private static IResource getResource(ILaunchConfiguration config) throws CoreException {
			String projName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
			String containerHandle = config.getAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, (String) null);
			String typeName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String) null);
			IJavaElement element = null;
			if (containerHandle != null && containerHandle.length() > 0) {
				element = JavaCore.create(containerHandle);
			} else if (projName != null && Path.ROOT.isValidSegment(projName)) {
				IJavaProject javaProject = getJavaModel().getJavaProject(projName);
				if (javaProject.exists()) {
					if (typeName != null && typeName.length() > 0) {
						element = javaProject.findType(typeName);
					}
					if (element == null) {
						element = javaProject;
					}
				} else {
					IProject project = javaProject.getProject();
					if (project.exists() && !project.isOpen()) {
						return project;
					}
				}
			}
			IResource resource = null;
			if (element != null) {
				resource = element.getResource();
			}
			return resource;
		}

		/*
		 * Convenience method to get access to the java model.
		 */
		private static IJavaModel getJavaModel() {
			return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		}
	}
}
