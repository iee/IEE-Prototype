package org.bitbucket.gashmish.fem.editor.containing;

import org.bitbucket.gashmish.fem.editor.contained.ContainedControl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;

public class SwitchableAction implements IAction, IUpdate {

	/****************************************************************/
    /*******The following methods just delegate to the **************/
    /*******actoin's super class*************************************/
    /****************************************************************/

    /**
     * the action on the Java editor to use if the selection is not in a contained
     * editor
     */
    private final IAction javaEditorAction;

    /** used to determine the currently focused ContainedEditor */
    private final ControlManager controlManager;
    
    public SwitchableAction(IAction javaEditorAction,
            ControlManager controlManager) {
        this.javaEditorAction = javaEditorAction;
        this.controlManager = controlManager;
    }
	
	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
	
		getRelevantAction().addPropertyChangeListener(listener);
	}

	@Override
	public int getAccelerator() {
		return getRelevantAction().getAccelerator();
	}

	@Override
	public String getActionDefinitionId() {
		return getRelevantAction().getActionDefinitionId();
	}

	@Override
	public String getDescription() {
		 return getRelevantAction().getDescription();
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return getRelevantAction().getDisabledImageDescriptor();
	}

	@Override
	public HelpListener getHelpListener() {
		return getRelevantAction().getHelpListener();
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		return getRelevantAction().getHoverImageDescriptor();
	}

	@Override
	public String getId() {
		return getRelevantAction().getId();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return getRelevantAction().getImageDescriptor();
	}

	@Override
	public IMenuCreator getMenuCreator() {
		return getRelevantAction().getMenuCreator();
	}

	@Override
	public int getStyle() {
		return getRelevantAction().getStyle();
	}

	@Override
	public String getText() {
		return getRelevantAction().getText();
	}

	@Override
	public String getToolTipText() {
		return getRelevantAction().getToolTipText();
	}

	@Override
	public boolean isChecked() {
		return getRelevantAction().isChecked();
	}

	@Override
	public boolean isEnabled() {
		return getRelevantAction().isEnabled();
	}

	@Override
	public boolean isHandled() {
		 return getRelevantAction().isHandled();
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		getRelevantAction().removePropertyChangeListener(listener);
		
	}

	@Override
	public void run() {
		getRelevantAction().run();
		
	}

	@Override
	public void runWithEvent(Event event) {
		getRelevantAction().runWithEvent(event);
		
	}

	@Override
	public void setAccelerator(int keycode) {
		getRelevantAction().setAccelerator(keycode);
		
	}

	@Override
	public void setActionDefinitionId(String id) {
		getRelevantAction().setActionDefinitionId(id);
	}

	@Override
	public void setChecked(boolean checked) {
		getRelevantAction().setChecked(checked);
		
	}

	@Override
	public void setDescription(String text) {
		 getRelevantAction().setDescription(text);
		
	}

	@Override
	public void setDisabledImageDescriptor(ImageDescriptor newImage) {
		getRelevantAction().setDisabledImageDescriptor(newImage);
		
	}

	@Override
	public void setEnabled(boolean enabled) {
		getRelevantAction().setEnabled(enabled);
		
	}

	@Override
	public void setHelpListener(HelpListener listener) {
		getRelevantAction().setHelpListener(listener);
	}

	@Override
	public void setHoverImageDescriptor(ImageDescriptor newImage) {
		getRelevantAction().setHoverImageDescriptor(newImage);
		
	}

	@Override
	public void setId(String id) {
		getRelevantAction().setId(id);
	}

	@Override
	public void setImageDescriptor(ImageDescriptor newImage) {
		getRelevantAction().setImageDescriptor(newImage);
		
	}

	@Override
	public void setMenuCreator(IMenuCreator creator) {
		getRelevantAction().setMenuCreator(creator);
		
	}

	@Override
	public void setText(String text) {
		getRelevantAction().setText(text);
		
	}

	@Override
	public void setToolTipText(String text) {
		getRelevantAction().setToolTipText(text);
		
	}

	@Override
	public void update() {
		IAction action = getRelevantAction();
        if (action instanceof IUpdate) {
            if (action instanceof TextOperationAction) {
                // this sets up the TextTarget
                ((TextOperationAction) action).setEditor(
                        controlManager.getContainingEditor());
            } else if (action instanceof TextEditorAction) {
                ((TextEditorAction) action).setEditor(
                        controlManager.getContainingEditor());
            }
            ((IUpdate) action).update();
        }
		
	}
	
	public IAction getJavaEditorAction() {
        return javaEditorAction;
    }
	/**
     * chooses either the JavaAction or one of the actions on a contained editor
     * depending on what is currently selected
     * 
     * @return either the Java editor action, or the contained editor action
     */
    private IAction getRelevantAction() {
        ContainedControl editor = controlManager.getCurrentlyActiveEditor();
        if (editor == null) {
            return javaEditorAction;
        } else {
            IAction action = editor.getAction(javaEditorAction);
            return action != null ? action : javaEditorAction;
        }
    }
	
    /**
     * Utility method used by many of the actions
     * Maps from the projected (widget) offset to the model offset.  
     * Handles code folding.
     * 
     * @param viewer
     * @param widgetOffset
     * @return the model offset
     */
    protected final static int widgetOffset2ModelOffset(ISourceViewer viewer,
            int widgetOffset) {
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            return extension.widgetOffset2ModelOffset(widgetOffset);
        }
        return widgetOffset + viewer.getVisibleRegion().getOffset();
    }

    /**
     * Returns the offset of the given source viewer's text widget that
     * corresponds to the given model offset or <code>-1</code> if there is no
     * such offset.
     * 
     * @param viewer
     *          the source viewer
     * @param modelOffset
     *          the model offset
     * @return the corresponding offset in the source viewer's text widget or
     *         <code>-1</code>
     * @since 3.0
     */
    protected final static int modelOffset2WidgetOffset(ISourceViewer viewer,
            int modelOffset) {
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            return extension.modelOffset2WidgetOffset(modelOffset);
        }
        return modelOffset - viewer.getVisibleRegion().getOffset();
    }

}
