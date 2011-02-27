package org.bitbucket.gashmish.fem.editor.containing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.java.JavaFormattingContext;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.bitbucket.gashmish.fem.editor.contained.ContainedControl;
import org.bitbucket.gashmish.fem.editor.containing.ColorManager;
import org.bitbucket.gashmish.fem.editor.containing.ControlManager;
import org.bitbucket.gashmish.fem.editor.ContainingStyledText;
 
@SuppressWarnings("restriction")
public class ContainingEditor extends CompilationUnitEditor {
	
	static final int EMBEDDED_REPAINT = 32;
    
	/**
     * The same as CompilationUnitEditor.AdaptedSourceViewer,
     * Only need this class because we want to add some functionality to the course viewer,
     * but CompilationUnitEditor.AdaptedSourceViewer is package protected
     */
    class ContainingAdaptedSourceViewer extends JavaSourceViewer  {
    	private final boolean CODE_ASSIST_DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector"));
        
    	public ContainingAdaptedSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
            super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
        }
    	
        public IContentAssistant getContentAssistant() {
            return fContentAssistant;
        }        
        
        public void doOperation(int operation) {
            if (getTextWidget() == null)
                return;

            switch (operation) {
                case CONTENTASSIST_PROPOSALS:
                    long time= CODE_ASSIST_DEBUG ? System.currentTimeMillis() : 0;
                    String msg= fContentAssistant.showPossibleCompletions();
                    if (CODE_ASSIST_DEBUG) {
                        long delta= System.currentTimeMillis() - time;
                        System.err.println("Code Assist (total): " + delta); //$NON-NLS-1$
                    }
                    setStatusLineErrorMessage(msg);
                    return;
                case QUICK_ASSIST:
                    /*
                     * XXX: We can get rid of this once the SourceViewer has a way to update the status line
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=133787
                     */
                    msg= fQuickAssistAssistant.showPossibleQuickAssists();
                    setStatusLineErrorMessage(msg);
                    return;
            }
            
            super.doOperation(operation);
        }
       
        public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
            if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
                return false;
            return super.requestWidgetToken(requester);
        }
        
        public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
            if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
                return false;
            return super.requestWidgetToken(requester, priority);
        }
        
        @SuppressWarnings("unchecked")
		public IFormattingContext createFormattingContext() {
            IFormattingContext context= new JavaFormattingContext();

            Map preferences;
            IJavaElement inputJavaElement= getInputJavaElement();
            IJavaProject javaProject= inputJavaElement != null ? inputJavaElement.getJavaProject() : null;
            if (javaProject == null)
                preferences= new HashMap(JavaCore.getOptions());
            else
                preferences= new HashMap(javaProject.getOptions(true));

            context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);

            return context;
        }
        
        @Override
        public void setSelectedRange(int selectionOffset, int selectionLength) {
            super.setSelectedRange(selectionOffset, selectionLength);
            fControlManager.paint(EMBEDDED_REPAINT);
        }
    }

    /**
     * rather than making changes to the super class, make changes to this
     * we want to ensure that the class ContainingAdaptedSourceViewer stays
     * as close as possible to the JDT class CompilationUnitEditor.AdaptedSourceViewer
     */
    public class ContainingSourceViewer extends ContainingAdaptedSourceViewer {
        
        public ContainingSourceViewer(Composite parent,
                IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
                boolean showAnnotationsOverview, int styles,
                IPreferenceStore store) {
            super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles,
                    store);
        }
        
        @Override
        protected StyledText createTextWidget(Composite parent, int styles) {
            return new ContainingStyledText(parent, styles);
        }
    }
 	
        
    private IDocument fDoc;
    private ControlManager fControlManager;
    private StyledText fStyledText;
    private PaintManager fPaintManager;
    
    private boolean fInternalDirty = false;
    
    /* Get Methods */  
    
    public PaintManager getPaintManager() {
        return fPaintManager;
    }
    
    public ControlManager getControlManager() {
        return fControlManager;
    }

    public ContainingSourceViewer getContainingViewer() {
        return (ContainingSourceViewer) super.getViewer();
    }
        
    /* Overwritten Methods */
    
	@Override
	public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        fControlManager.generateControls();
	}
        
    /**
     * Extend this method to add a few more listeners and create the 
     * control manager
     */
    @Override
    protected JavaSourceViewer createJavaSourceViewer(
    		Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
    		boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
        
    	JavaSourceViewer viewer = new ContainingSourceViewer(
        	parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

        fDoc = this.getDocumentProvider().getDocument(this.getEditorInput());
        fStyledText = viewer.getTextWidget();

        fControlManager = new ControlManager(this, fStyledText, fDoc);
        fControlManager.installPartitioner(viewer);
        
        
        // ensure that text can't be typed in the embedded editor
        fStyledText.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                if (fControlManager.isPositionBehindEditor(e.start, e.end-e.start)) {
                    e.doit = false;
                }
            }
        });
        
        // Whenever selection is completely behind an embedded editor, 
        // give focus to the editor
        final ISelectionProvider provider = viewer.getSelectionProvider();
        final ISelectionChangedListener focusOnEmbeddedListener = new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                if (e.getSelection() instanceof TextSelection) {
                    provider.removeSelectionChangedListener(this);
                    TextSelection sel = (TextSelection) e.getSelection();
                    focusOnContainedEditor(sel.getOffset(), sel.getLength());
                    provider.addSelectionChangedListener(this);
                }
            }
        };
        provider.addSelectionChangedListener(focusOnEmbeddedListener);
        
        // This listener does two things
        //
        // page up and page down must trigger refresh, 
        // so editors are properly redrawn
        //
        // ensures that arrow keys can navigate into embedded editors
        fStyledText.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
                switch (e.keyCode) {
                    case SWT.PAGE_UP:
                    case SWT.PAGE_DOWN:
                    case SWT.HOME:
                    case SWT.END:
                        fControlManager.paint(EMBEDDED_REPAINT);
                        break;
                        
                    case SWT.ARROW_UP:
                    case SWT.ARROW_DOWN:
                    case SWT.ARROW_LEFT:
                    case SWT.ARROW_RIGHT:
                        provider.removeSelectionChangedListener(focusOnEmbeddedListener);
                        focusOnContainedEditor(getSourceViewer().getSelectedRange().x, 0);
                        provider.addSelectionChangedListener(focusOnEmbeddedListener);
                }
                
            }
            public void keyPressed(KeyEvent e) { }
        });

        // XXX for some reason clicking on initializers in the outline view does
        // not select anywhere in the java editor.  I think this is a bug in 
        // eclipse.  This means that the display is not repainted correctly when
        // an initializer is clicked.
        // To counteract, if the initializer is clicked twice, then the second
        // time, the display will repaint properly.  That's what the code below does
        JavaOutlinePage outline = (JavaOutlinePage) getAdapter(IContentOutlinePage.class);
        outline.addPostSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                fControlManager.paint(EMBEDDED_REPAINT);
            }
        });
        
        ScrollBar verticalBar = fStyledText.getVerticalBar();
        if (verticalBar != null) {
            verticalBar.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    fControlManager.paint(EMBEDDED_REPAINT);
                }
            });
        }
        ScrollBar horizontalBar = fStyledText.getHorizontalBar();
        if (horizontalBar != null) {
            horizontalBar.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    fControlManager.paint(EMBEDDED_REPAINT);
                }
            });
        }
        
        // Redraw the embedded editors whenever there is a repaint
        viewer.addTextListener(new ITextListener() {
            public void textChanged(TextEvent event) {
                 if (event.getDocumentEvent() != null) {
                    DocumentEvent de = event.getDocumentEvent();
                    fControlManager.generateControls(de.fOffset, de.getText().length());
                    fControlManager.paint(IPainter.TEXT_CHANGE);
                }
            }
        });
        
      /*  // set up the paint listener
        try {
            Method fPaintManager;
            fPaintManager = TextViewer.class.getDeclaredMethod("getPaintManager");
            fPaintManager.setAccessible(true);
            fPaintManager = (PaintManager) fPaintManager.invoke(viewer);
            fPaintManager.addPainter(fControlManager);
        } catch (SecurityException e) {
        	//...
	    } catch (IllegalArgumentException e) {
	 
	    } catch (IllegalAccessException e) {
	      
	    } catch (NoSuchMethodException e) {
	       
	    } catch (InvocationTargetException e) {
	      
	    }
	    */
                
        // ensures that we have variable height
        fStyledText.setLineSpacing(fStyledText.getLineSpacing());

        return viewer;
    }   
    
    @Override
    protected void selectAndReveal(int selectionStart, int selectionLength, int revealStart, int revealLength) {
        super.selectAndReveal(selectionStart, selectionLength, revealStart, revealLength);
        fControlManager.paint(EMBEDDED_REPAINT);
    }
      
    @Override
    public Object getAdapter(Class required) {
        if (ITextOperationTarget.class.equals(required)) {
            ContainedControl containedControl = fControlManager.getActiveContainedControl();
            if (containedControl != null) {
                return containedControl.getAdapter(required);
            }
        }        
        return super.getAdapter(required);
    }
    
    /**
     * override here because when the StyledText gets focus
     * it immediately passes it onto its first child for focus
     * which in this case is an embedded editor.  This is not th
     * behavior we want
     */
    @Override
    public void setFocus() { }
    
    @Override
    public boolean isDirty() {
        return fInternalDirty || super.isDirty();
    }
    
    
    /* Additional Methods */
    
    public void setDirty() {
        boolean fireChange = !fInternalDirty;
        fInternalDirty = true;

        if (fireChange) {
            firePropertyChange(PROP_DIRTY);
        }
    }
    
    public void focusOnContainedEditor(ContainedControl containedControl) {
        if (containedControl != null) {
        	containedControl.setFocus();
            fControlManager.revealSelection(containedControl, containedControl.getSelection().offset);
        }
    }
    
    /**
     * brings the focus to a contained editor if the position passed in
     * is completely enclosed by an editor
     * @param offset
     * @param length
     */
    private void focusOnContainedEditor(int offset, int length) {
    	ContainedControl containedControl = fControlManager.findEditor(offset, length, false);
        focusOnContainedEditor(containedControl);
    }
  
}
