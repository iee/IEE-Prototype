package org.bitbucket.gashmish.fem.editor.containing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bitbucket.gashmish.fem.editor.contained.ContainedControl;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

@SuppressWarnings("restriction")
public class ContainingControl extends CompilationUnitEditor {

	private static final boolean CODE_ASSIST_DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector")); //$NON-NLS-1$//$NON-NLS-2$

	static final int EMBEDDED_REPAINT = 32;
	
	class ContainingAdaptedSourceViewer extends JavaSourceViewer {

		public ContainingAdaptedSourceViewer(Composite parent,
				IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
				boolean showAnnotationsOverview, int styles,
				IPreferenceStore store) {
			super(parent, verticalRuler, overviewRuler,
					showAnnotationsOverview, styles, store);
		}

		public IContentAssistant getContentAssistant() {
			return fContentAssistant;
		}

		/*
		 * @see ITextOperationTarget#doOperation(int)
		 */
		public void doOperation(int operation) {

			if (getTextWidget() == null)
				return;

			switch (operation) {
			case CONTENTASSIST_PROPOSALS:
				long time = CODE_ASSIST_DEBUG ? System.currentTimeMillis() : 0;
				String msg = fContentAssistant.showPossibleCompletions();
				if (CODE_ASSIST_DEBUG) {
					long delta = System.currentTimeMillis() - time;
					System.err.println("Code Assist (total): " + delta); //$NON-NLS-1$
				}
				setStatusLineErrorMessage(msg);
				return;
			case QUICK_ASSIST:
				/*
				 * XXX: We can get rid of this once the SourceViewer has a way
				 * to update the status line
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=133787
				 */
				msg = fQuickAssistAssistant.showPossibleQuickAssists();
				setStatusLineErrorMessage(msg);
				return;
			}

			super.doOperation(operation);
		}

		/*
		 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
			if (PlatformUI.getWorkbench().getHelpSystem()
					.isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester);
		}

		/*
		 * @see
		 * IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper,
		 * int)
		 * 
		 * @since 3.0
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester,
				int priority) {
			if (PlatformUI.getWorkbench().getHelpSystem()
					.isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester, priority);
		}

		/*
		 * @see
		 * org.eclipse.jface.text.source.SourceViewer#createFormattingContext()
		 * 
		 * @since 3.0
		 */


		@Override
		public void setSelectedRange(int selectionOffset, int selectionLength) {
			super.setSelectedRange(selectionOffset, selectionLength);
//			controlManager.paint(EMBEDDED_REPAINT);
		}

	}

	/**
	 * rather than making changes to the super class, make changes to this we
	 * want to ensure that the class ContainingAdaptedSourceViewer stays as
	 * close as possible to the JDT class
	 * CompilationUnitEditor.AdaptedSourceViewer
	 */
	public class ContainingSourceViewer extends ContainingAdaptedSourceViewer {

		public ContainingSourceViewer(Composite parent,
				IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
				boolean showAnnotationsOverview, int styles,
				IPreferenceStore store) {
			super(parent, verticalRuler, overviewRuler,
					showAnnotationsOverview, styles, store);
		}

	}

	private PaintManager paintManager;
    private IDocument doc;
	final static int MARGIN = 2; // margin is 0, but we can bump this up if we
	private StyledText styledText;
	// want
	ColorManager colorManager = new ColorManager();
	private ControlManager controlManager;
	private boolean internalDirty = false;

	public ContainingControl() {
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
		super.createPartControl(parent);
		// controlManager.generateControls();
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

	@Override
	protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
		return new ContainingEditorConfiguration(super
				.createJavaSourceViewerConfiguration(), this);
	}

	@Override
	protected JavaSourceViewer createJavaSourceViewer(Composite parent,
			IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {

		// create custom configuration
		ContainingEditorConfiguration config = (ContainingEditorConfiguration) createJavaSourceViewerConfiguration();
		setSourceViewerConfiguration(config);

		JavaSourceViewer viewer = new ContainingSourceViewer(parent,
				verticalRuler, overviewRuler, isOverviewRulerVisible, styles,
				store);

		// set up assorted fields
		doc = this.getDocumentProvider().getDocument(this.getEditorInput());
		styledText = viewer.getTextWidget();

        // ensure that text can't be typed in the embedded editor
//        styledText.addVerifyListener(new VerifyListener() {
//            public void verifyText(VerifyEvent e) {
//                if (controlManager.isPositionBehindEditor(e.start, e.end-e.start)) {
//                    // actually, should bring up a dialog box that
//                    // will ask if should be deleted
//                    e.doit = false;
//                }
//            }
//        });
        
		// use a verify listener to dispose the images
		styledText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				if (event.start == event.end)
					return;
				
				String text = styledText.getText(event.start, event.end - 1);
				int index = text.indexOf("/* <-- *//* --> */");
				while (index != -1) {
					StyleRange style = styledText.getStyleRangeAtOffset(event.start
							+ index);
					if (style != null) {
						Image image = (Image) style.data;
						if (image != null)
							image.dispose();
					}
					index = text.indexOf("/* <-- *//* --> */", index + 1);
				}
			}
		});
		// draw images on paint event
		styledText.addPaintObjectListener(new PaintObjectListener() {
			public void paintObject(PaintObjectEvent event) {
				StyleRange style = event.style;
				if (style == null)
					return;
				Image image = (Image) style.data;
				if (image == null)
					return;
				if (!image.isDisposed()) {
					int x = event.x;
					int y = event.y + event.ascent - style.metrics.ascent;
					event.gc.drawImage(image, x, y);
				}
			}
		});
		
		styledText.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				StyleRange[] styles = styledText.getStyleRanges();
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
		
		// Whenever selection is completely behind an embedded editor,
		// give focus to the editor
		final ISelectionProvider provider = viewer.getSelectionProvider();
		final ISelectionChangedListener focusOnEmbeddedListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				if (e.getSelection() instanceof TextSelection) {
					provider.removeSelectionChangedListener(this);
					TextSelection sel = (TextSelection) e.getSelection();
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
        styledText.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
                switch (e.keyCode) {
                    case SWT.PAGE_UP:
                    case SWT.PAGE_DOWN:
                    case SWT.HOME:
                    case SWT.END:
                        controlManager.paint(EMBEDDED_REPAINT);
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
		// not select anywhere in the java editor. I think this is a bug in
		// eclipse. This means that the display is not repainted correctly when
		// an initializer is clicked.
		// To counteract, if the initializer is clicked twice, then the second
		// time, the display will repaint properly. That's what the code below
		// does

		controlManager = new ControlManager(this, styledText, doc);
		// controlManager.installPartitioner(viewer);

		// redraw the embedded editors whenever there is a repaint
//		viewer.addTextListener(new ITextListener() {
//			public void textChanged(TextEvent event) {
//				// ensure that this is a valid text change
//				// if (event.getReplacedText() != null || event.getLength() !=
//				// event.getText().length()) {
//				if (event.getDocumentEvent() != null) {
//					DocumentEvent de = event.getDocumentEvent();
//
//					// try {
//					// // do the full line
//					// IRegion start =
//					// doc.getLineInformationOfOffset(event.getOffset());
//					// IRegion end =
//					// doc.getLineInformationOfOffset(event.getOffset() +
//					// event.getLength());
//					// controlManager.generateControls(start.getOffset(),
//					// start.getLength());
//
////					controlManager.generateControls(de.fOffset, de.getText()
////							.length());
//					controlManager.paint(IPainter.TEXT_CHANGE);
//					// } catch (BadLocationException e) {
//					// EmbeddedCALPlugin.logError("Error generating controls after text changed",
//					// e);
//					// }
//				}
//			}
//		});

		return viewer;
	}

    PaintManager getPaintManager() {
        return paintManager;
    }
    
	public ControlManager getControlManager() {
		return controlManager;
	}
	
    public ContainingSourceViewer getContainingViewer() {
        return (ContainingSourceViewer) super.getViewer();
    }
    
    /**
     * brings the focus to a contained editor if the position passed in
     * is completely enclosed by an editor
     * @param offset
     * @param length
     */
    private void focusOnContainedEditor(int offset, int length) {
        ContainedControl editor = 
            controlManager.findEditor(offset, length, false);
        focusOnContainedEditor(editor);
    }

    public void focusOnContainedEditor(ContainedControl editor) {
        if (editor != null) {
            editor.setFocus();
            Position p = controlManager.getEditorPosition(editor);
            getViewer().revealRange(p.offset, p.length);
            controlManager.revealSelection(editor, editor.getSelection().offset);
        }
    }
    
    /**
     * extended to ensure that a change to a contained editor will dirty
     * the containing editor
     */
    public void setDirty() {
        boolean fireChange = !internalDirty;
        internalDirty = true;

        if (fireChange) {
            firePropertyChange(PROP_DIRTY);
        }
    }
    
    public ISourceViewer internalGetSourceViewer() {
        return getSourceViewer();
    }

}
