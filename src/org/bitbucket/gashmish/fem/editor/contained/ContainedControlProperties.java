package org.bitbucket.gashmish.fem.editor.contained;

import java.util.Hashtable;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.graphics.Point;
import org.bitbucket.gashmish.fem.editor.containing.ControlManager;


@SuppressWarnings("unchecked")
public abstract class ContainedControlProperties {

    private static final int MIN_HEIGHT = 10;
    private static final int MIN_WIDTH = 10;
    public final static int DEFAULT_WIDTH = 200;
    public final static int DEFAULT_HEIGHT = 20;

    /** the text that is displayed in the embedded editor */
    private String contents;

    /** the height of the embedded editor */
    private int height;

    /** the width of the embedded editor */
    private int width;

    /**
     * is the text in the editor valid (ie- syntactically correct) CAL code?
     * no type checking is occurring yet.
     */
    private boolean valid;

    /**
     * true if the contents of this properties has changes compared to the
     * underlying document
     */
    private boolean dirty;

    /**
     * true if this embedded editor is showing all of its fields (ie- expanded
     * form), or if it is only showing the text (ie- compact form)
     */
    private boolean showAll;


    /**
     * creates an empty properties when the annotation is not available
     */
    public ContainedControlProperties() {
        contents = "";
        height = DEFAULT_HEIGHT;
        width = DEFAULT_WIDTH;
        valid = false;
        dirty = false;
        showAll = false;
    }



    /**
     * @return the text contents of the embedded editor.  This is not the text that is serialized.
     */
    public String getCalContents() {
        return contents;
    }

    /**
     * @param calContents this is the text that is displayed in the embedded editor
     */
    public void setCalContents(String calContents) {
        this.contents = calContents;
        dirty = true;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width != this.width) {
            this.width = Math.max(width, MIN_WIDTH);
            dirty = true;
        }

    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height != this.height) {
            this.height = Math.max(height, MIN_HEIGHT);
            dirty = true;
        }
    }

    /**
     * Regenerates the java text from the current properties
     *
     * Uses the code analyzer to determine what the input arguments are.
     * @param cm The control manager for the compilation unit
     * @return the serialization of this embedded editor (what gets saved to disk)
     */
    public abstract String serializeEmbeddedEditor(ControlManager cm);

    /* *****************************************************************
     * tools for creating the initial expressions
     */
    private static ASTParser parser = ASTParser.newParser(AST.JLS3);
    private final static Hashtable<String, String> options =
        JavaCore.getOptions();
    static {
        options.put(JavaCore.COMPILER_COMPLIANCE, "1.5");
        options.put(JavaCore.COMPILER_SOURCE, "1.5");
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.5");
    }


    /**
     *
     * @param methodInvocationText this is the serialized form of the contents of the
     * embedded editor
     * @return a parsed form of the serialized form
     */
    public static ASTNode toASTNode(String methodInvocationText) {
    	// TODO Delete me
    	return null;
    }

    public void setSize(Point size) {
        if (height != size.y || width != size.x) {
            height = size.y;
            width = size.x;
            dirty = true;
        }
    }

    public boolean isValid() {
        return valid;
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isShowingAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        dirty = true;
        this.showAll = showAll;
    }



    protected void createImport(ICompilationUnit unit, String requiredImport) {
        try {
            unit.createImport(requiredImport, null, null);
        } catch (JavaModelException e) {
            // TODO Use own log error
        }
    }
    protected void createStaticImport(ICompilationUnit unit, String requiredImport) {
        try {
            unit.createImport(requiredImport, null, Flags.AccStatic, null);
        } catch (JavaModelException e) {
            // TODO Use own log error
        }
    }
}