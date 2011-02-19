package org.bitbucket.gashmish.fem.editor.containing;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IPropertyListener;
 
@SuppressWarnings("restriction")
public class  ContainedEditorManager extends CompilationUnitEditor {

	public ContainedEditorManager() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void aboutToBeReconciled() {
		// TODO Auto-generated method stub
		super.aboutToBeReconciled();
	}

	@Override
	public void addPropertyListener(IPropertyListener l) {
		// TODO Auto-generated method stub
		super.addPropertyListener(l);
	}

	@Override
	public void addPartPropertyListener(IPropertyChangeListener listener) {
		// TODO Auto-generated method stub
		super.addPartPropertyListener(listener);
	}

	@Override
	public void addRulerContextMenuListener(IMenuListener listener) {
		// TODO Auto-generated method stub
		super.addRulerContextMenuListener(listener);
	}

	@Override
	public void close(boolean save) {
		// TODO Auto-generated method stub
		super.close(save);
	}

	public void collapseComments() {
	}

	@Override
	public void collapseMembers() {
		// TODO Auto-generated method stub
		super.collapseMembers();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createPartControl(parent);
	}
	
	@Override
	public INavigationLocation createEmptyNavigationLocation() {
		// TODO Auto-generated method stub
		return super.createEmptyNavigationLocation();
	}
	
	@Override
	public INavigationLocation createNavigationLocation() {
		// TODO Auto-generated method stub
		return super.createNavigationLocation();
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
	}
	
	@Override
	public void doRevertToSaved() {
		// TODO Auto-generated method stub
		super.doRevertToSaved();
	}
}
