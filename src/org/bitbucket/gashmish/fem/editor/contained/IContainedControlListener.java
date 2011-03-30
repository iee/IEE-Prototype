package org.bitbucket.gashmish.fem.editor.contained;

public interface IContainedControlListener {

    public static enum ExitDirection { UP, DOWN, RIGHT, LEFT };

    public void editorSaved(ContainedControl editor, ContainedControlProperties props);

    public void editorResized(ContainedControl editor, ContainedControlProperties props);

    public void editorChanged(ContainedControl editor, ContainedControlProperties props);

    public void editorDeleted(ContainedControl editor, ContainedControlProperties props);

    public void editorFocusGained(ContainedControl editor, ContainedControlProperties props);

    public void editorFocusLost(ContainedControl editor, ContainedControlProperties props);

    public void exitingEditor(ContainedControl editor, ContainedControlProperties props, ExitDirection dir);
}
