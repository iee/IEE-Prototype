package org.bitbucket.gashmish.fem.editor.handlers;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.bitbucket.gashmish.fem.editor.containing.ContainingControl;
import org.bitbucket.gashmish.fem.editor.containing.ControlManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.EvalUtilities;
import org.matheclipse.core.eval.TeXUtilities;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.form.output.StringBufferWriter;
import org.matheclipse.core.interfaces.IExpr;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

public class EvaluateLineActionDelegate implements IEditorActionDelegate {

	private ContainingControl currentControl;
	int FONT_SIZE_TEX = 20;
	private static final String embeddedRegionMarker = "@image ";
	private static final String evaluateMarker = "@eval ";
	/* XXX: Rewrite it, all images need cached */
	private final String defaultImageName = "/tmp/test.jpeg";
	
	public EvaluateLineActionDelegate() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(IAction action) {
		
		if (currentControl == null) {
			return;
		}
		
		ISelectionProvider provider = currentControl.getSelectionProvider();
		IDocument doc = currentControl.getContainingViewer().getDocument();
		
		if (doc != null && provider != null
				&& provider.getSelection() instanceof TextSelection) {
			TextSelection sel = (TextSelection) provider.getSelection();
			
			IRegion lineInfo;
			String expression;
			
			try {
				/* Extracting line expression */
				lineInfo = doc.getLineInformation(doc.getLineOfOffset(sel.getOffset()));
				expression = doc.get(lineInfo.getOffset(), lineInfo.getLength());
				
				/* Check this expression */
				int eval = expression.indexOf(evaluateMarker);
				
				String output = "";
				if (eval != -1) {
					/* Evaluate this expression */
					String realExpression = expression.substring(eval + evaluateMarker.length(), lineInfo.getLength());
			
				    F.initSymbols(null);
				    EvalUtilities util = new EvalUtilities();

				    IExpr result;
					StringBufferWriter buf = new StringBufferWriter();
					result = util.evaluate(realExpression);
					OutputFormFactory.get().convert(buf, result);
					output = buf.toString();
					
					/* Convert to TeX and image */
					EvalEngine engine = new EvalEngine(); 
					TeXUtilities texUtil = new TeXUtilities(engine);

					StringWriter stw = new StringWriter();
					texUtil.toTeX(result, stw);
					output = stw.toString();
					
					TeXFormula formula = new TeXFormula(stw.toString());
					/* */
					formula.createJPEG(TeXConstants.STYLE_DISPLAY, FONT_SIZE_TEX, defaultImageName, Color.white, Color.black);
				}
				
				
				/* Print the evaluated expression if exists */
				if (output.length() > 0) {
					/* XXX: We use this hack with generate user keyboard event that activate editor 
					 * JavaDocAutoIndentStrategy for good formatting JavaDoc comment */
					Event event;
					Widget w = currentControl.getContainingViewer().getTextWidget();
					
					int eventType = SWT.KeyDown;
					event = new Event();
					event.keyCode = 13;
					event.doit = true;
					event.character = SWT.CR;	
					
					w.notifyListeners(eventType, event);
					
					/* After event invoked we need get new cursor position and insert evaluated data*/
					sel = (TextSelection) provider.getSelection();
					doc.replace(sel.getOffset(), 0, output);
					
					/* We set cursor to end of line */
					lineInfo = doc.getLineInformation(doc.getLineOfOffset(sel.getOffset()));
					
					TextSelection a = new TextSelection(doc, lineInfo.getOffset() + lineInfo.getLength(), 0);
					provider.setSelection(a);
					
					/* Insert generated image in editor */
					doc.replace(sel.getOffset(), sel.getLength(), embeddedRegionMarker);
	
					ControlManager cm = currentControl.getControlManager();
	
					InputStream in = null;
					in = new BufferedInputStream(new FileInputStream(defaultImageName));
					Image newImage = new Image(Display.getDefault(), in);
	
					cm.addImage(newImage, sel.getOffset(), embeddedRegionMarker.length());
				}
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		if (targetEditor instanceof ContainingControl) {
			currentControl = (ContainingControl) targetEditor;
		}

	}

}
