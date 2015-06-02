package junitfootprint.footprint;

import java.util.ArrayList;
import java.util.List;

import junitfootprint.Activator;
import junitfootprint.junit.JUnitHelper;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

public class FootPrintViewPart extends ViewPart implements IPartListener {
	
	public static final String ID = "junitfootprint.footprint.FootPrintViewPart";
	
	// Command label
	private static final String COMMAND_LABEL_ALL_RUN_JUNIT = "Run all";
	private static final String COMMAND_LABEL_CLEAR = "Clear";
	private static final String COMMAND_LABEL_MOVE = "Move";
	private static final String COMMAND_LABEL_RUN_JUNIT = "Run";
	private static final String COMMAND_LABEL_REMOVE = "Remove";
	
	private static final Image SUCCESS = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/greenbar.png").createImage();
	private static final Image FAIL = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/redbar.png").createImage();
	
	private TableViewer tableViewer;
	public static List<FootPrintInfo> footPrintInfoList = new ArrayList<FootPrintInfo>();


	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, false));

		createColumnTable(composite);

	}

	private void createColumnTable(Composite composite) {
		tableViewer = new TableViewer(composite, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		Table table = tableViewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		/*
		 * If table use image, bug occurs(same image is shown on first column. this bug occurs only on Windows system.).
		 * So, dummy column(size 0) should be created.
		 */
		TableViewerColumn emptyColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		emptyColumn.getColumn().setWidth(0);
		emptyColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});

		TableViewerColumn className = new TableViewerColumn(tableViewer, SWT.NONE);
		className.getColumn().setWidth(250);
		className.getColumn().setText("Class Name");
		className.getColumn().setAlignment(SWT.LEFT);
		className.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FootPrintInfo info = (FootPrintInfo) element;
				return info.getTypeName();
			}
		});

		TableViewerColumn methodName = new TableViewerColumn(tableViewer, SWT.NONE);
		methodName.getColumn().setWidth(150);
		methodName.getColumn().setText("Method Name");
		methodName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FootPrintInfo info = (FootPrintInfo) element;
				return info.getMethodName();
			}
		});

		TableViewerColumn result = new TableViewerColumn(tableViewer, SWT.NONE);
		result.getColumn().setWidth(200);
		result.getColumn().setText("Result");
		result.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
				FootPrintInfo info = (FootPrintInfo) element;
				if (info.isSuccess()) {
					return SUCCESS;
				}
				return FAIL;
			}
		});

		TableViewerColumn time = new TableViewerColumn(tableViewer, SWT.NONE);
		time.getColumn().setWidth(150);
		time.getColumn().setText("Time");
		time.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FootPrintInfo info = (FootPrintInfo) element;
				return info.getTime();
			}
		});

		tableViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object obj) {
				if (obj instanceof List) {
					return ((List) obj).toArray();
				} else {
					return new Object[0];
				}
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				moveToTestcase();
			}
		});

		refresh();

		createContextMenu();

		createToolbar();

	}

	private void createToolbar() {

		RunAllTestsFromFootPrintAction runAllAction = new RunAllTestsFromFootPrintAction();
		runAllAction.setText(COMMAND_LABEL_ALL_RUN_JUNIT);
		runAllAction.setImageDescriptor(Activator.getImageDescriptor("icons/run.gif"));
		getViewSite().getActionBars().getToolBarManager().add(runAllAction);

		ClearFootPrintAction clearFootPrintAction = new ClearFootPrintAction();
		clearFootPrintAction.setText(COMMAND_LABEL_CLEAR);
		clearFootPrintAction.setImageDescriptor(Activator.getImageDescriptor("icons/trash.gif"));
		getViewSite().getActionBars().getToolBarManager().add(clearFootPrintAction);

	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		refresh();
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		refresh();
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		refresh();
	}

	@Override
	public void setFocus() {
		refresh();

	}

	private void moveToTestcase() {
		ISelection selection = tableViewer.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		FootPrintInfo info = (FootPrintInfo) structuredSelection.getFirstElement();
		if (info != null) {
			openEditor(info.getiFile());
		}
	}

	private void createContextMenu() {

		final Action moveAction = new Action() {
			public void run() {
				moveToTestcase();
				super.run();
			}
		};
		moveAction.setImageDescriptor(Activator.getImageDescriptor("icons/move.gif"));
		moveAction.setText(COMMAND_LABEL_MOVE);

		final Action runAction = new Action() {
			public void run() {
				ISelection selection = tableViewer.getSelection();
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				FootPrintInfo info = (FootPrintInfo) structuredSelection.getFirstElement();
				JUnitHelper.runTestCase(info);
				refresh();
				super.run();
			}
		};
		runAction.setImageDescriptor(Activator.getImageDescriptor("icons/junit.png"));
		runAction.setText(COMMAND_LABEL_RUN_JUNIT);

		final Action removeAction = new Action() {
			public void run() {
				ISelection selection = tableViewer.getSelection();
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				FootPrintInfo footPrintInfo = (FootPrintInfo) structuredSelection.getFirstElement();

				for (int i = 0; i < footPrintInfoList.size(); i++) {
					FootPrintInfo info = footPrintInfoList.get(i);
					if (footPrintInfo.getTypeName().equals(info.getTypeName())
							&& footPrintInfo.getMethodName().equals(info.getMethodName())) {
						footPrintInfoList.remove(i);
					}
				}
				refresh();
				super.run();
			}
		};
		removeAction.setImageDescriptor(Activator.getImageDescriptor("icons/delete.gif"));
		removeAction.setText(COMMAND_LABEL_REMOVE);

		MenuManager colMenu = new MenuManager();
		colMenu.setRemoveAllWhenShown(true);
		colMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(moveAction);
				manager.add(runAction);
				manager.add(removeAction);
			}
		});

		Menu menu = colMenu.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);

	}

	public static void putFootPrintInfo(FootPrintInfo footPrintInfo) {

		for (int i = 0; i < footPrintInfoList.size(); i++) {
			FootPrintInfo info = footPrintInfoList.get(i);
			if (footPrintInfo.getTypeName().equals(info.getTypeName())
					&& footPrintInfo.getMethodName().equals(info.getMethodName())) {
				footPrintInfoList.set(i, footPrintInfo);
				return;
			}
		}
		footPrintInfoList.add(footPrintInfo);
	}

	public void refresh() {
		tableViewer.setInput(footPrintInfoList);
		tableViewer.refresh();
	}
	
	
	
	private void openEditor(IFile ifile) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.openEditor(new FileEditorInput(ifile), JavaUI.ID_CU_EDITOR, true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
		} catch (Exception e) {
//			LogUtils.error(e);
		}
	}

}
