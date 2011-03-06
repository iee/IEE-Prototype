package org.bitbucket.gashmish.fem.editor.contained;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.bitbucket.gashmish.fem.editor.containing.ContainingControl;
import org.bitbucket.gashmish.fem.editor.contained.ControlImage;

public class ContainedControl implements IAdaptable {

	protected static final FormAttachment TOP_AND_LEFT = new FormAttachment(0,
			0);

	protected static final FormAttachment UNEXPANDED_BOTTOM = new FormAttachment(
			100, -0);

	protected static final FormAttachment RIGHT = new FormAttachment(100, -0);

	protected static final FormAttachment EXPANDED_BOTTOM = new FormAttachment(
			100, -42);

	private ControlImage viewer;
	protected Composite control;
	Image image;

	/**
	 * maps a java editor action to the equivalent action to be used by this
	 * contained editor
	 */
	private Map<String, IAction> actionMap = new HashMap<String, IAction>();
	private final Cursor arrowCursor = new Cursor(Display.getCurrent(),
			SWT.CURSOR_ARROW);

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

	public IAction getAction(IAction javaEditorAction) {
		return actionMap.get(javaEditorAction.getActionDefinitionId());
	}

	public void createControl(final StyledText parent,
			ContainingControl containingEditor) {
		// use a verify listener to dispose the images
		parent.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				if (event.start == event.end)
					return;
				String text = parent.getText(event.start, event.end - 1);
				int index = text.indexOf('\uFFFC');
				while (index != -1) {
					StyleRange style = parent.getStyleRangeAtOffset(event.start
							+ index);
					if (style != null) {
						Image image = (Image) style.data;
						if (image != null)
							image.dispose();
					}
					index = text.indexOf('\uFFFC', index + 1);
				}
			}
		});
		// draw images on paint event
		parent.addPaintObjectListener(new PaintObjectListener() {
			public void paintObject(PaintObjectEvent event) {
				StyleRange style = event.style;
				if (style == null)
					return;
				Image image = (Image) style.data;
				if (!image.isDisposed()) {
					int x = event.x;
					int y = event.y + event.ascent - style.metrics.ascent;
					event.gc.drawImage(image, x, y);
				}
			}
		});
		parent.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				StyleRange[] styles = parent.getStyleRanges();
				for (int i = 0; i < styles.length; i++) {
					StyleRange style = styles[i];
					if (style.data != null) {
						Image image = (Image) style.data;
						if (image != null)
							image.dispose();
					}
				}
			}
		});

		control = new Composite(parent, SWT.BORDER);
		control.setLayout(new FillLayout());
		viewer = new ControlImage(control, SWT.NONE);
		createSubControls();
	}

	/**
	 * subclasses extend to add their own controls
	 */
	protected void createSubControls() {
		control.setCursor(arrowCursor);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.spacing = 5;
		control.setLayout(layout);
		control.setToolTipText("Ctrl Dbl-Click to delete box");

		FormData styledTextData = new FormData();
		styledTextData.top = TOP_AND_LEFT;
		styledTextData.left = TOP_AND_LEFT;
		styledTextData.right = RIGHT;

		viewer.setLayoutData(styledTextData);
		// Layout for image
		// styledText.setLayoutData(styledTextData);
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

	public Rectangle getBounds() {
		return viewer.getDrawingBounds();
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
		control.setLocation(location);
	}

	public Position getSelection() {
		Point sel = styledText.getSelectionRange();
		return new Position(sel.x, sel.y);
	}

	public boolean setFocus() {
		return control.setFocus();
	}

	public void removeListener(IContainedControlListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	public Image getImage() {
		return image;
	}

}
