package org.bitbucket.gashmish.fem.editor.action;

import org.bitbucket.gashmish.fem.editor.containing.ContainingControl;
import org.bitbucket.gashmish.fem.editor.containing.ControlManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.swt.custom.StyleRange;

public class InsertImage implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private ContainingControl currentControl;

	private final static String embeddedRegionMarker = "/*" + "Some some"
			+ "*/";

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;

	}

	@Override
	public void run(IAction action) {
		ITextEditor editor = (ITextEditor) window.getActivePage()
				.getActiveEditor();
		IDocumentProvider dp = editor.getDocumentProvider();
		IDocument doc = dp.getDocument(editor.getEditorInput());

		ITextSelection ie = (ITextSelection) editor.getSelectionProvider()
				.getSelection();
	

		if (doc != null) {
			ControlManager cm = currentControl.getControlManager();
			StyleRange[] ranges = cm.createAndAddControl(0,
					embeddedRegionMarker.length());
			TextPresentation singlePres = new TextPresentation();
			singlePres.addStyleRange(ranges[0]);
			singlePres.addStyleRange(ranges[1]);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
