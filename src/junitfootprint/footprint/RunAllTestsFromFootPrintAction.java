package junitfootprint.footprint;

import junitfootprint.junit.JUnitHelper;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class RunAllTestsFromFootPrintAction extends Action implements IWorkbenchAction {

	private static final String ID = "junitfootprint.footprint.ClearFootPrintAction";

	public RunAllTestsFromFootPrintAction() {
		setId(ID);
	}

	@Override
	public void run() {
		for (FootPrintInfo info : FootPrintViewPart.footPrintInfoList) {
			JUnitHelper.runTestCase(info);
		}
	}

	@Override
	public void dispose() {
	}

}
