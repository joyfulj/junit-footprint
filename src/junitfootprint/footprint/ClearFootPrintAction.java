package junitfootprint.footprint;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ClearFootPrintAction extends Action implements IWorkbenchAction {

	private static final String ID = "junitfootprint.footprint.ClearFootPrintAction";
	
	public ClearFootPrintAction(){
		setId(ID);
	}
	
	@Override
	public void run(){
		FootPrintViewPart.footPrintInfoList.clear();
		
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = activePage.findView(FootPrintViewPart.ID);
		if(activePage != null && activePage.isPartVisible(view)){
			FootPrintViewPart footPrintViewPart = (FootPrintViewPart)view;
			footPrintViewPart.refresh();
		};
	}
	
	@Override
	public void dispose() {
	}

}
