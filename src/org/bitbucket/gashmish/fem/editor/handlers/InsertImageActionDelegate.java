package org.bitbucket.gashmish.fem.editor.handlers;

import javax.swing.text.BadLocationException;

import org.bitbucket.gashmish.fem.editor.contained.ContainedControl;
import org.bitbucket.gashmish.fem.editor.containing.ContainingControl;
import org.bitbucket.gashmish.fem.editor.containing.ControlManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class InsertImageActionDelegate implements IEditorActionDelegate {

	private static String embeddedRegionMarker = ""; // /*<-- -->*/
	
	private ContainingControl currentControl;
	
	Shell shell = null;
	
	public InsertImageActionDelegate() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		if (targetEditor instanceof ContainingControl) {
			currentControl = (ContainingControl) targetEditor;
        }
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		Shell[] shells = Display.getCurrent().getShells();
        
        if (shells.length > 0) {
            shell = shells[0];
        }
		if (currentControl == null) {
            
            MessageDialog.openError(shell, "Invalid editor", 
                    "Cannot embedded CAL editors in this Java editor.\n" +
                    "Close this editor, and re-open as an embeddable editor.");
            return;
        }
        ISelectionProvider provider = currentControl.getSelectionProvider();
        IDocument doc = currentControl.getContainingViewer().getDocument();
        if (doc != null && provider != null && provider.getSelection() instanceof TextSelection) {
            //try {
        	FileDialog dialog = new FileDialog(shell);
			String filename = dialog.open();
			if (filename != null) 
			{
				/*<-- -->*/
				embeddedRegionMarker = "/*<--"+filename+"-->*/";
                TextSelection sel = (TextSelection) provider.getSelection();
                try {
					doc.replace(sel.getOffset(), 1, embeddedRegionMarker);
				} catch (org.eclipse.jface.text.BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
					ControlManager cm = currentControl.getControlManager();
					//cm.generateControls(); use this line for debug 
					StyleRange[] ranges = cm.createAndAddControl(sel.getOffset(), embeddedRegionMarker.length(), filename);
					TextPresentation singlePres = new TextPresentation();
					singlePres.addStyleRange(ranges[0]);
					singlePres.addStyleRange(ranges[1]);
                
					currentControl.internalGetSourceViewer().changeTextPresentation(singlePres, true);
                
                // Focus on the new editor
                //ContainedControl containedEditor = 
                //    cm.findEditorProjected(ranges[0].start, ranges[0].length, true);
               //if (containedEditor != null) {
                //    containedEditor.setFocus();
               // }
                
            //} catch (BadLocationException e) {
            //    System.out.print("wrong");
            //}
		   }
        }

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
