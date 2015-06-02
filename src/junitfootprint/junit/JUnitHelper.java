package junitfootprint.junit;

import junitfootprint.footprint.FootPrintInfo;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;

public class JUnitHelper {
	
	public static void runTestCase(FootPrintInfo info){
		JUnitLaunch launch = new JUnitLaunch();
		try {
			IJavaElement javaElement = org.eclipse.jdt.core.JavaCore.create(info.getiFile());
			launch.performLaunch(javaElement, ILaunchManager.RUN_MODE, info);
		} catch (InterruptedException e) {
//			LogUtils.error(e);
		} catch (CoreException e) {
//			LogUtils.error(e);
		}
	}
	
}
