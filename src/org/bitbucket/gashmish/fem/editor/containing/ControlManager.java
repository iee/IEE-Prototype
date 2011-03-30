/*
 * Copyright (c) 2011 EditorDesignFEM 
 * Copyright (c) 2007 BUSINESS OBJECTS SOFTWARE LIMITED
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *  
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *  
 *     * Neither the name of Business Objects nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * ControlManager.java
 * Created: Jun 18, 2007
 * By: Andrew Eisenberg
 */
package org.bitbucket.gashmish.fem.editor.containing;

import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.DELETE_NEXT_WORD;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.LINE_END;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.LINE_START;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.SELECT_LINE_END;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.SELECT_LINE_START;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.SELECT_WORD_NEXT;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.TEXT_END;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.TEXT_START;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.WORD_NEXT;
import static org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.WORD_PREVIOUS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bitbucket.gashmish.fem.editor.contained.ContainedControl;
import org.bitbucket.gashmish.fem.editor.contained.ContainedControlProperties;
import org.bitbucket.gashmish.fem.editor.contained.IContainedControlListener;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import org.bitbucket.gashmish.fem.editor.containing.ContainingControlScanner;
import org.bitbucket.gashmish.fem.editor.containing.ContainingControl;

public class ControlManager implements IPainter, ITextPresentationListener,
		IContainedControlListener {

	/**
	 * The paint position manager used by this paint manager. The paint position
	 * manager is installed on a single document and control the
	 * creation/disposed and updating of a position category that will be used
	 * for managing positions.
	 * 
	 * This one is essentially the same as Eclipse's PositionManager, but the
	 * IPositionUpdater must be a default one, not one used by the default
	 * PositionManager
	 * 
	 */
	static class ControlPositionManager implements IPaintPositionManager {

		/** The document this position manager works on */
		private IDocument fDocument;
		/** The position updater used for the managing position category */
		private IPositionUpdater fPositionUpdater;
		/** The managing position category */
		private String fCategory;

		/**
		 * Creates a new position manager. Initializes the managing position
		 * category using its class name and its hash value.
		 * 
		 * @param doc
		 *            the document managed by this control manager
		 */
		public ControlPositionManager(IDocument doc) {
			fCategory = getClass().getName() + hashCode();
			fPositionUpdater = new DefaultPositionUpdater(fCategory);
			install(doc);
		}

		public String getCategory() {
			return fCategory;
		}

		/**
		 * Installs this position manager in the given document. The position
		 * manager stays active until <code>uninstall</code> or
		 * <code>dispose</code> is called.
		 * 
		 * @param document
		 *            the document to be installed on
		 */
		public void install(IDocument document) {
			if (document != null && this.fDocument != document) {
				fDocument = document;
				fDocument.addPositionCategory(fCategory);
				fDocument.addPositionUpdater(fPositionUpdater);
			}
		}

		/**
		 * Disposes this position manager. The position manager is automatically
		 * removed from the document it has previously been installed on.
		 */
		public void dispose() {
			uninstall(fDocument);
		}

		/**
		 * Uninstalls this position manager form the given document. If the
		 * position manager has no been installed on this document, this method
		 * is without effect.
		 * 
		 * @param document
		 *            the document form which to uninstall
		 */
		public void uninstall(IDocument document) {
			if (document == fDocument && document != null) {
				try {
					fDocument.removePositionUpdater(fPositionUpdater);
					fDocument.removePositionCategory(fCategory);
				} catch (BadPositionCategoryException x) {
					// ...
				}
				fDocument = null;
			}
		}

		/*
		 * @see IPositionManager#addManagedPosition(Position)
		 */
		public void managePosition(Position position) {
			try {
				fDocument.addPosition(fCategory, position);
			} catch (BadPositionCategoryException x) {
				// ...
			} catch (BadLocationException x) {
				// ...
			}
		}

		/*
		 * @see IPositionManager#removeManagedPosition(Position)
		 */
		public void unmanagePosition(Position position) {
			try {
				fDocument.removePosition(fCategory, position);
			} catch (BadPositionCategoryException x) {
				// ...
			}
		}
	}

	/**
	 * Tiny font for holding text that should be invisible. Used for text after
	 * new lines that should be behind embedded editors. really, the text height
	 * should be 0, but this is the closest we can get
	 */
	private final static Font TINY_FONT = new Font(Display.getDefault(),
			new FontData("Times", 1, 0));

	private final ContainingControlScanner fScanner = new ContainingControlScanner();
	private final ContainingControl fContainingEditor;
	private final IDocument fDoc;
	private final StyledText fStyledText;
	private IPaintPositionManager fPaintPositionManager;
	private Map<ContainedControl, Position> fContainedControlPositionMap = new HashMap<ContainedControl, Position>();
	private ContainedControl fActiveContainedControl = null;
	private ContainedControl currentlyActiveEditor = null;

	public ContainedControl getActiveContainedControl() {
		return fActiveContainedControl;
	}

	public ContainingControl getContainingEditor() {
		return fContainingEditor;
	}

	/**
	 * Creates a new Control manager for the given containing editor
	 * 
	 * @param embeddedEditor
	 * @param doc
	 * @param styledText
	 */
	@SuppressWarnings("restriction")
	ControlManager(ContainingControl containingEditor, StyledText styledText,
			IDocument doc) {
		this.fContainingEditor = containingEditor;
		this.fStyledText = styledText;
		this.fDoc = doc;
	}

	/**
	 * Installs a document partitioner on the containing editor's document. This
	 * partitioner will determine where contained editors should go.
	 * 
	 * @param viewer
	 */
	public void installPartitioner(ISourceViewer viewer) {
		FastPartitioner partitioner = new FastPartitioner(fScanner,
				new String[] { ContainingControlScanner.CONTAINED_EDITOR });

		partitioner.connect(fDoc);

		IDocumentExtension3 documentExtension = (IDocumentExtension3) fDoc;
		documentExtension.setDocumentPartitioner(
				ContainingControlScanner.CONTAINED_EDITOR, partitioner);

		if (viewer instanceof ITextViewerExtension4) {
			ITextViewerExtension4 extension = (ITextViewerExtension4) viewer;
			extension.addTextPresentationListener(this);
		} else {
			// print error message
		}
	}

	public void addImage(Image image, int offset, int length) {
		StyleRange style = new StyleRange ();
		style.start = offset;
		style.length = length;
		style.data = image;
		Rectangle rect = image.getBounds();
		style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		fStyledText.setStyleRange(style);		
	}
	
	/**
	 * Extended so that we use our own position manager, not the one that is
	 * passed in.
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		// ignore the passed in position manager
		fPaintPositionManager = new ControlPositionManager(fDoc);
		fContainingEditor.getPaintManager().inputDocumentChanged(null, fDoc);
	}

	/**
	 * this is the workhorse method that goes through the entire document and
	 * generates the controls for the contained editors where they are supposed
	 * to be located.
	 */
	public boolean generateControls() {
		return generateControls(0, fDoc.getLength());
	}

	boolean generateControls(int start, int length) {
		// set up a scan for the entire document.
		fScanner.setPartialRange(fDoc, start, length,
				IDocument.DEFAULT_CONTENT_TYPE, start);

		boolean controlCreated = false;

		// create the controls,
		// determine their ranges,
		// add them to the StyledText
		IToken tok;
		while (!(tok = fScanner.nextToken()).isEOF()) {
			if (tok == ContainingControlScanner.EDITOR_TOKEN) {
				StyleRange[] ranges = createAndAddControl(fScanner
						.getTokenOffset(), fScanner.getTokenLength());
				TextPresentation singlePres = new TextPresentation();
				singlePres.addStyleRange(ranges[0]);
				singlePres.addStyleRange(ranges[1]);
				// this.containingEditor.internalGetSourceViewer().changeTextPresentation(singlePres,
				// true);
				fContainingEditor.getContainingViewer().changeTextPresentation(
						singlePres, true);

				controlCreated = true;
			}
		}

		return controlCreated;
	}

	/**
	 * Maps a position from the model; (complete) document to the projected
	 * document that may have some folded elements
	 * 
	 * Use modelToProjected or projectedToModel when doing translation from
	 * screen to the document. The following are specified in model coordinates
	 * <ul>
	 * <li>Document offsets
	 * <li>Style ranges
	 * </ul>
	 * The following are specified in projected coordinates
	 * <ul>
	 * <li>pixels on the screen (eg, all points, sizes, and locations)
	 * <li>lexical offsets into the styled text
	 * </ul>
	 * 
	 * @param modelPosition
	 * @return
	 */
	Position modelToProjected(Position modelPosition) {
		ISourceViewer viewer = fContainingEditor.getContainingViewer();
		if (viewer instanceof ProjectionViewer) {
			ProjectionViewer projViewer = (ProjectionViewer) viewer;
			IRegion region = projViewer.modelRange2WidgetRange(new Region(
					modelPosition.offset, modelPosition.length));

			if (region == null) {
				// region is hidden in a fold
				return null;
			} else {
				return new Position(region.getOffset(), region.getLength());
			}
		} else {
			return modelPosition;
		}
	}

	/**
	 * Maps a position from the underlying (complete) document to the projected
	 * document that may have some folded elements
	 * 
	 * @param projectedPosition
	 * @return
	 * @see ControlManager#modelToProjected(Position)
	 */
	Position projectedToModel(Position projectedPosition) {
		ISourceViewer viewer = fContainingEditor.getContainingViewer();
		if (viewer instanceof ProjectionViewer) {
			ProjectionViewer projViewer = (ProjectionViewer) viewer;
			IRegion region = projViewer.widgetRange2ModelRange(new Region(
					projectedPosition.offset, projectedPosition.length));
			if (region != null) {
				return new Position(region.getOffset(), region.getLength());
			} else {
				return null;
			}
		} else {
			return projectedPosition;
		}
	}

	/**
	 * triggers a repaint of the styled text of the containing editor whenever
	 * the text has changed.
	 * 
	 * The repaint will update the positions of all of the embedded controls.
	 * 
	 * XXX this method is being called too many times. It is being called more
	 * than once after each cursor change. I need to take a good look at this
	 * and determine exactly when and where it should be called
	 */
	public void paint(int reason) {
		if (reason != TEXT_CHANGE
				&& reason != ContainingControl.EMBEDDED_REPAINT) {
			return;
		}
		List<ContainedControl> toRemove = new LinkedList<ContainedControl>();

		for (final ContainedControl c : fContainedControlPositionMap.keySet()) {
			Position model = fContainedControlPositionMap.get(c);
			if (!model.isDeleted()) {
				// map from the model to the actual display (takes into account
				// folding)
				Position projected = modelToProjected(model);
				if (projected == null) {
					// position is hidden behind folding
					c.getControl().setVisible(false);
				} else {
					try {
						Point location = fStyledText
								.getLocationAtOffset(projected.offset);
						location.x += ContainingControl.MARGIN;
						location.y += ContainingControl.MARGIN;
						c.getControl().setVisible(true);
						c.getControl().setLocation(location);
					} catch (IllegalArgumentException e) {
						// ...
					}
				}
			} else {
				toRemove.add(c);
			}
		}
		for (final ContainedControl c : toRemove) {
			removeControl(c, true);
		}
		fStyledText.getParent().getParent().redraw();
	}

	/**
	 * disposes all of the controls and unremembers their positions
	 */
	public void dispose() {
		for (final ContainedControl c : fContainedControlPositionMap.keySet()) {
			removeControl(c, false);
		}
		fContainedControlPositionMap.clear();
	}

	/**
	 * Removes the control from this manager. Unmanages this position,
	 * 
	 * @param c
	 *            the control to remove
	 * @param doRemove
	 *            whether or not this control should be completely removed, or
	 *            just temporarily (eg- during a save)
	 * 
	 * @return the position of the removed control
	 */
	private Position removeControl(ContainedControl c, boolean doRemove) {
		Position p;
		if (doRemove) {
			p = fContainedControlPositionMap.remove(c);
		} else {
			p = fContainedControlPositionMap.get(c);
		}
		try {
			fPaintPositionManager.unmanagePosition(p);

			if (doRemove && !p.isDeleted()) {
				fStyledText.replaceStyleRanges(p.offset, p.length,
						new StyleRange[0]);
			}
		} catch (NullPointerException e) {
			// ...
		}
		/*
		 * TODO implement this c.removeListener(this); c.dispose();
		 */
		return p;
	}

	/*
	 * Adds a control at the given position
	 */
	private ContainedControl addControl(int offset, int length) {
		ContainedControl contained = new ContainedControl();

		contained.createControl(fStyledText, fContainingEditor);
		contained.initializeControlContents(fContainingEditor, "/tmp/test.jpg");
		
		// determine the location of the contained editor
		Position projected = modelToProjected(new Position(offset, 0));
		Point location = fStyledText.getLocationAtOffset(projected.offset);
		location.x += ContainingControl.MARGIN;
		location.y += ContainingControl.MARGIN;
		contained.setLocation(location);

		return contained;
	}
	/*
	 * Adds a control at the given position
	 */
	private ContainedControl addControl(int offset, int length, String path) {
		
		ContainedControl contained = new ContainedControl();
		//fStyledText.replaceTextRange(fStyledText.getCaretOffset(), 0, "\uFFFC");
		contained.createControl(fStyledText, fContainingEditor);
		
		contained.initializeControlContents(fContainingEditor, path);
		// determine the location of the contained editor
		Position projected = modelToProjected(new Position(offset, 0));
		Point location = fStyledText.getLocationAtOffset(projected.offset);
		location.x += ContainingControl.MARGIN;
		location.y += ContainingControl.MARGIN;
		contained.setLocation(location);

		return contained;
	}
	/**
	 * creates the style range of the StyledText for the range of the contained
	 * editor
	 * 
	 * XXX problem will occur if there is a newline in the position. Working on
	 * this! code folding.
	 */
	private StyleRange[] createStyleRange(ContainedControl c, Position p) {
		int offset = p.offset;
		int length = p.length;

		Rectangle rect = c.getControl().getBounds();
		rect = c.getBounds();
		
		// Use one style range
		StyleRange first = new StyleRange();
		first.start = offset;
		first.length = Math.min(1, length);
		first.data = c.getImage();
		first.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		
        StyleRange second = new StyleRange();
		second.start = offset + 1;
		second.length = length - 1;
//		second.data = c.getImage();
		second.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		
		return new StyleRange[] { first, second };
	}

	/**
	 * Creates and adds a single control at the given position
	 * 
	 * @param offset
	 * @param length
	 * @return pair of style ranges that covers this embedded editor
	 */
	public StyleRange[] createAndAddControl(int offset, int length) {
		StyleRange[] styles = null;
		Position pos = new Position(offset, length);
		if (!fContainedControlPositionMap.containsValue(pos)) {
			ContainedControl newContainedEditor = addControl(offset, length);
			newContainedEditor.addListener(this);
			styles = createStyleRange(newContainedEditor, pos);
			// newContainedEditor.registerActions(fContainingEditor);
			fContainedControlPositionMap.put(newContainedEditor, pos);
			// ppManager.managePosition(pos);

		} else {
			for (final ContainedControl c : fContainedControlPositionMap
					.keySet()) {
				if (fContainedControlPositionMap.get(c).equals(pos)) {
					styles = createStyleRange(c, pos);
					break;
				}
			}
		}
		return styles;
	}
	/**
	 * Creates and adds a single control at the given position
	 * 
	 * @param offset
	 * @param length
	 * @return pair of style ranges that covers this embedded editor
	 */
	public StyleRange[] createAndAddControl(int offset, int length, String path) {
		StyleRange[] styles = null;
		Position pos = new Position(offset, length);
		if (!fContainedControlPositionMap.containsValue(pos)) {
			ContainedControl newContainedEditor = addControl(offset, length, path);
			newContainedEditor.addListener(this);
			styles = createStyleRange(newContainedEditor, pos);
			// newContainedEditor.registerActions(fContainingEditor);
			fContainedControlPositionMap.put(newContainedEditor, pos);
			// ppManager.managePosition(pos);

		} else {
			for (final ContainedControl c : fContainedControlPositionMap
					.keySet()) {
				if (fContainedControlPositionMap.get(c).equals(pos)) {
					styles = createStyleRange(c, pos);
					break;
				}
			}
		}
		return styles;
	}
	/**
	 * Checks to see if a projected position is behind an embedded editor
	 * 
	 * @param embeddedOffset
	 * @param embeddedLength
	 * @return true if the given position overlaps with any contained editor.
	 *         false otherwise
	 */
	public boolean isPositionBehindEditor(int embeddedOffset, int embeddedLength) {
		// map from projected document (with folding) to the model
		// document (complete, without folding)
		Position model = projectedToModel(new Position(embeddedOffset,
				embeddedLength));
		for (final Position editorPos : fContainedControlPositionMap.values()) {
			if (model.offset + model.length > editorPos.offset
					&& model.offset < editorPos.offset + editorPos.length) {
				return true;
			}
		}
		return false;
	}

	/**
	 * finds a contained editor that is covered by this position
	 * 
	 * @param offset
	 * @param length
	 * @param overlapOK
	 *            whether or not the position passed in must be fully contained
	 *            by the position of the editor or if the positions need to
	 *            merely overlap (if <code>true</code> then overlap is OK. if
	 *            <code>false</code> then position must be completely contained
	 *            by the editor
	 * 
	 * @return the editor covered by the passed in position, or null if there is
	 *         none.
	 */
	public ContainedControl findEditor(int offset, int length, boolean overlapOK) {
		Position selectedModelPosition = new Position(offset, length);
		for (final ContainedControl editor : fContainedControlPositionMap
				.keySet()) {
			Position editorPosition = fContainedControlPositionMap.get(editor);
			if (overlapOK) {
				if (editorPosition.overlapsWith(selectedModelPosition.offset,
						selectedModelPosition.length)) {
					return editor;
				}
			} else {
				if (editorPosition.offset < selectedModelPosition.offset
						&& editorPosition.offset + editorPosition.length > selectedModelPosition.offset
								+ selectedModelPosition.length) {
					return editor;
				}
			}
		}
		return null;
	}

	/**
	 * finds a contained editor that is covered by this position The position is
	 * in projected coordinates (ie- that of the styled text, not of document
	 * coordinates)
	 * 
	 * @param offset
	 *            of projected position
	 * @param length
	 *            of projected position
	 * @param overlapOK
	 *            whether or not the position passed in must be fully contained
	 *            by the position of the editor or if the positions need to
	 *            merely overlap (if <code>true</code> then overlap is OK. if
	 *            <code>false</code> then position must be completely contained
	 *            by the editor
	 * 
	 * @return the editor covered by the passed in position, or null if there is
	 *         none.
	 */
	public ContainedControl findEditorProjected(int offset, int length,
			boolean overlapOK) {
		Position model = projectedToModel(new Position(offset, length));
		return findEditor(model.offset, model.length, overlapOK);
	}

	/**
	 * Extended to remove any style ranges that overlap with an embedded editor.
	 * 
	 * We don't want to change style ranges over the region where there is a
	 * contained editor, because thet will remove the GlyphMetrics that we
	 * created earlier
	 */
	public void applyTextPresentation(TextPresentation textPresentation) {
		// need to check if any of the ranges of the textPresentation overlaps
		// with an embedded editor region.

		// for now, we can assume that there won't be many store positions, so
		// we can
		// just go through them sequentially.
		// if this turns out to be expensive, we can be more intelligent later
		Collection<Position> values = fContainedControlPositionMap.values();
		for (Iterator<StyleRange> rangeIter = textPresentation
				.getAllStyleRangeIterator(); rangeIter.hasNext();) {
			StyleRange range = rangeIter.next();
			Position overlapPosition = null;
			for (final Position editorPosition : values) {
				if (editorPosition.overlapsWith(range.start, range.length)) {
					overlapPosition = editorPosition;
					break;
				}
			}

			if (overlapPosition != null) {
				textPresentation.replaceStyleRanges(createStyleRange(
						getEditor(overlapPosition), overlapPosition));
			}
		}
	}

	/**
	 * get the key from a given value. O(n), but we don't expect this map to be
	 * very large. can change this later if it is a bottleneck.
	 */
	private ContainedControl getEditor(Position p) {
		for (final ContainedControl editor : fContainedControlPositionMap
				.keySet()) {
			if (p.equals(fContainedControlPositionMap.get(editor))) {
				return editor;
			}
		}
		return null;
	}

	public Position getEditorPosition(ContainedControl editor) {
		return fContainedControlPositionMap.get(editor);
	}

	/**
	 * Copies the contents of the contained editor into the containing editor.
	 * 
	 * The contents of the contained editor are serialized (ie- converted to
	 * java text) and overwrites the old serialization.
	 * 
	 * @param editor
	 *            the editor to serialize
	 * @param props
	 *            the editor properties from which the serialization can be
	 *            obtained
	 */
	public void updateSerialization(ContainedControl editor,
			ContainedControlProperties props) {

		String serialization = props.serializeEmbeddedEditor(this);
		Position p = fContainedControlPositionMap.get(editor);
		try {
			fDoc.replace(p.offset, p.length, serialization);
		} catch (BadLocationException e) {
			// TODO use own logger
		}

		@SuppressWarnings("restriction")
		ICompilationUnit unit = ((org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider) fContainingEditor
				.getDocumentProvider()).getWorkingCopy(fContainingEditor
				.getEditorInput());

		//if (unit != null) { seems it' unuseful
		//	props.requiresImport(unit);
		//}
	}

	public void saveAllEditors() {
		String moduleName = getModuleName();
		if (moduleName != null) {
			for (final ContainedControl editor : fContainedControlPositionMap
					.keySet()) {
				// TODO We need this or not?
			}
		}
	}

	/**
	 * 
	 * @return the name of the module associated with this compilation unit
	 *         there should be only one per compilation unit. right now not
	 *         checking for that, but will. If no module is specified, then null
	 *         is returned
	 */
	public String getModuleName() {
		return null;
	}

	public String getModuleEditor() {
		return null;
	}

	/**
	 * Converts a bunch of actions on the ContainingEditor into
	 * SwitchableActions. This way, when appropriate, the relevant action on the
	 * ContainedEditor will be executed in place of the standard JDT action.
	 * 
	 * @param newContainedEditor
	 *            The contained editor on which to register the actions
	 */
	void registerActions() {
		IAction oldAction; // the original JDT editor action
		IAction newAction;

		// TODO We may implement this
		// navigation actions
//		oldAction = originalAction(fContainingEditor.getAction(LINE_START));
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(LINE_START, newAction);
//
//		oldAction = fContainingEditor.getAction(SELECT_LINE_START);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(SELECT_LINE_START, newAction);
//
//		oldAction = fContainingEditor.getAction(LINE_END);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(LINE_END, newAction);
//
//		oldAction = fContainingEditor.getAction(SELECT_LINE_END);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(SELECT_LINE_END, newAction);
//
//		oldAction = fContainingEditor.getAction(WORD_PREVIOUS);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(WORD_PREVIOUS, newAction);
//
//		oldAction = fContainingEditor.getAction(WORD_NEXT);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(WORD_NEXT, newAction);
//
//		oldAction = fContainingEditor.getAction(SELECT_WORD_PREVIOUS);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(SELECT_WORD_PREVIOUS, newAction);
//
//		oldAction = fContainingEditor.getAction(SELECT_WORD_NEXT);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(SELECT_WORD_NEXT, newAction);
//
//		oldAction = fContainingEditor.getAction(DELETE_PREVIOUS_WORD);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(DELETE_PREVIOUS_WORD, newAction);
//
//		oldAction = fContainingEditor.getAction(DELETE_NEXT_WORD);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(DELETE_NEXT_WORD, newAction);
//
//		oldAction = fContainingEditor.getAction(TEXT_START);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(TEXT_START, newAction);
//
//		oldAction = fContainingEditor.getAction(TEXT_END);
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction(TEXT_END, newAction);
//
		oldAction = fContainingEditor.getAction(ITextEditorActionConstants.SELECT_ALL);
		newAction = new SwitchableAction(oldAction, this);
		fContainingEditor.setAction(ITextEditorActionDefinitionIds.SELECT_ALL,newAction);
		fContainingEditor.setAction(ITextEditorActionConstants.SELECT_ALL,newAction);
		fContainingEditor.markAsStateDependentAction(ITextEditorActionConstants.SELECT_ALL,true);
		// Text actions

		// note- we use "paste", not PASTE since the former
		// is what the JavaEditor uses
		// the same goes for the other text operations

		oldAction = fContainingEditor.getAction("paste");
		newAction = new SwitchableAction(oldAction, this);
		fContainingEditor.setAction("paste", newAction);

		oldAction = fContainingEditor.getAction("copy");
		newAction = new SwitchableAction(oldAction, this);
		fContainingEditor.setAction("copy", newAction);

		oldAction = fContainingEditor.getAction("cut");
		newAction = new SwitchableAction(oldAction, this);
		fContainingEditor.setAction("cut", newAction);

//		// hover
		oldAction = fContainingEditor
				.getAction(ITextEditorActionConstants.SHOW_INFORMATION);
		newAction = new SwitchableAction(oldAction, this);
		fContainingEditor.setAction(
				ITextEditorActionDefinitionIds.SHOW_INFORMATION, newAction);
		fContainingEditor.setAction(
				ITextEditorActionConstants.SHOW_INFORMATION, newAction);
//
//		// toggle comment
//		oldAction = fContainingEditor.getAction("ToggleComment");
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction("ToggleComment", newAction);
//		fContainingEditor.setAction(
//				IJavaEditorActionDefinitionIds.TOGGLE_COMMENT, newAction);
//
//		// content assist
//		oldAction = fContainingEditor.getAction("ContentAssistProposal");
//		newAction = new SwitchableAction(oldAction, this);
//		fContainingEditor.setAction("ContentAssistProposal", newAction);
//
		// open declaration
		oldAction = fContainingEditor.getAction("OpenEditor");
		newAction = new SwitchableAction(oldAction, this);
		fContainingEditor.setAction("OpenEditor", newAction);
//
//		// oldAction =
//		// containingEditor.getAction(IJavaEditorActionDefinitionIds.FORMAT);
//		// newAction = new SwitchableAction(oldAction, this);
//		// containingEditor.setAction(IJavaEditorActionDefinitionIds.FORMAT,
//		// newAction);

	}

	/**
	 * accesses the original JavaEditorAction unwraps an action replaced by
	 * 
	 * @param oldAction
	 * @return
	 */
	private IAction originalAction(IAction oldAction) {
        return oldAction instanceof SwitchableAction ? ((SwitchableAction) oldAction).getJavaEditorAction() : oldAction;
    }
	/**
	 * Called whenever a contained editor is resized
	 */
	public void editorResized(ContainedControl editor,
			ContainedControlProperties props) {
		 updateSerialization(editor, props);
	}

	public void editorSaved(ContainedControl editor,
			ContainedControlProperties props) {
		updateSerialization(editor, props);
	}

	// XXX This method is still influx. trying to get the
	// screen to scroll if the cursor moves off it
	public void editorChanged(ContainedControl editor,
			ContainedControlProperties props) {
		fContainingEditor.setDirty();

		// ensure that if the cursor moves off the screen, then the control is
		// scrolled back into view
		if (editor == getCurrentlyActiveEditor()) {
			revealSelection(editor, editor.getSelection().offset);
		}

	}

	/**
	 * Scrolls the containing editor to the given offset of the contained editor
	 * 
	 * @param editor
	 * @param scrollTo
	 *            text offset in the embedded editor that should be revealed
	 */
	public void revealSelection(ContainedControl editor, int scrollTo) {
		StyledText containedStyledText = (StyledText) editor
				.getAdapter(StyledText.class);

		if (containedStyledText == null)
			return;
		
		// this progression determines the location of the offset in the
		// coordinate system
		// of the containing styledText
		Point containedLoc = containedStyledText.getLocationAtOffset(scrollTo);
		Point displayLoc = containedStyledText.toDisplay(containedLoc);
		Point containingLoc = fStyledText.toControl(displayLoc);

		// next, we determine if this location is in the visible area.
		Point containingSize = fStyledText.getSize();
		Rectangle containingBounds = new Rectangle(0, 0, containingSize.x,
				containingSize.y);
		if (!containingBounds.contains(containingLoc)) {
			// pad a little to the left and a little bit down
			containingLoc.x -= 50;
			containingLoc.y += 100;

			// if not, then perform a scroll.
			fStyledText.setTopPixel(fStyledText.getTopPixel() + containingLoc.y
					- containingSize.y);

			// do the same for horizontal
			fStyledText.setHorizontalPixel(fStyledText.getHorizontalPixel()
					+ containingLoc.x);
		}
		paint(ContainingControl.EMBEDDED_REPAINT);
	}

	public void editorDeleted(ContainedControl editor,
			ContainedControlProperties props) {

		IEditorStatusLine statusLine = (IEditorStatusLine) fContainingEditor
				.getAdapter(IEditorStatusLine.class);
		statusLine.setMessage(false, "Editor Deleted", null);

		Position p = removeControl(editor, true);
		if (p != null) {
			try {
				fContainingEditor.getSelectionProvider().setSelection(
						new TextSelection(fDoc, p.offset, 0));
				fDoc.replace(p.offset, p.length, "");
			} catch (BadLocationException e) {
				// it's OK to ignore this
				// shouldn't happen anyway
			}
		}
		
		// remove listener
		editor.removeListener(this);
	}

	public void editorFocusGained(ContainedControl editor,
			ContainedControlProperties props) {
		Position p = fContainedControlPositionMap.get(editor);
		currentlyActiveEditor = editor;
		if (p != null) {
			fContainingEditor.getContainingViewer().setSelectedRange(p.offset,
					p.length);
		}
		//fContainingEditor.updateSelectionDependentActions();
		//fContainingEditor.updateStateDependentActions();
	}

	public void editorFocusLost(ContainedControl editor,
			ContainedControlProperties props) {
		currentlyActiveEditor = null;

		// only change the text if there is a change from previous serialization
		if (props.isDirty()) {
			updateSerialization(editor, props);
			// ensure that all line heights are redrawn to their correct size
			//fContainingEditor.internalGetSourceViewer().invalidateTextPresentation();
		}

		// ensure that the actions are reset to correspond to the
		// ContainingEditor, not the ContainedEditor
		//fContainingEditor.updateSelectionDependentActions();
		//fContainingEditor.updateStateDependentActions();
		
	}

	public void exitingEditor(ContainedControl editor,
			ContainedControlProperties props, ExitDirection dir) {

		Position controlPosition = fContainedControlPositionMap.get(editor);
		switch (dir) {
		case UP:
			// not handled for now
		case DOWN:
			// not handled for now
			break;
		case LEFT:
			fContainingEditor.getSelectionProvider().setSelection(
					new TextSelection(controlPosition.offset, 0));
			break;
		case RIGHT:
			fContainingEditor.getSelectionProvider().setSelection(
					new TextSelection(controlPosition.offset
							+ controlPosition.length, 0));
			break;
		}
		// implicitly triggers a focus lost event on the contained editor
		// fContainingEditor.internalGetSourceViewer().getTextWidget().forceFocus();
		//fContainingEditor.getSourceViewer().getTextWidget().forceFocus();
	}
	
    public ContainedControl getCurrentlyActiveEditor() {
        return currentlyActiveEditor;
    }
    
	@Override
	public void deactivate(boolean redraw) {
		// TODO Auto-generated method stub

	}

	


}
