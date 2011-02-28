package org.bitbucket.gashmish.fem.editor.contained;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.bitbucket.gashmish.fem.editor.containing.ContainingControl;
import org.bitbucket.gashmish.fem.editor.contained.ControlImage;

public class ContainedControl implements IAdaptable {

	private ControlImage viewer;
	protected Composite control;
	Image image;
	private List<IContainedControlListener> listeners;
	protected StyledText styledText;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ITextOperationTarget.class
				|| adapter == ContainedControl.class) {
			return viewer;
		}
		
		return null;
	}

	public void createControl(StyledText parent,
			ContainingControl containingEditor) {
		control = new Composite(parent, SWT.BORDER);
		control.setLayout(new FillLayout());
		viewer = new ControlImage(control, SWT.NONE);
	}

	public void initializeControlContents(ContainingControl containingEditor,
			String PathToImage) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(PathToImage));
			Image newImage = new Image(Display.getDefault(), in);
			viewer.setImage(newImage);
			disposeImage();
			image = newImage;
		} catch (Exception e) {
			// If there's an exception, do nothing. Life goes on...
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    
    public void addListener(IContainedControlListener listener) {
        if (listeners == null) {
            listeners = new LinkedList<IContainedControlListener>();
        }
        listeners.add(listener);
    }
    
	private void disposeImage() {
		if (image == null)
			return;
		image.dispose();
		image = null;
	}
	
    public Composite getControl() {
        return control;
    }

	public void setLocation(Point location) {
		// TODO Auto-generated method stub
	}
	
    public Position getSelection() {
        Point sel = styledText.getSelectionRange();
        return new Position(sel.x, sel.y);
    }
}
