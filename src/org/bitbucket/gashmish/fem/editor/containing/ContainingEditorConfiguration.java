package org.bitbucket.gashmish.fem.editor.containing;

import java.lang.reflect.Field;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

public class ContainingEditorConfiguration extends JavaSourceViewerConfiguration {


    public ContainingEditorConfiguration(JavaSourceViewerConfiguration config, ITextEditor editor) {
        super(getColorManager(config), getPreferenceStore(config), editor, IJavaPartitions.JAVA_PARTITIONING);
    }
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
                IDocument.DEFAULT_CONTENT_TYPE,
                IJavaPartitions.JAVA_DOC,
                IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
                IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
                IJavaPartitions.JAVA_STRING,
                IJavaPartitions.JAVA_CHARACTER
        };
    }

    /**
     * The super constructor requires a color manager, but it is a private field
     * in JavaSourceViewerConfiguration.
     * This method uses reflection to get it.
     * @param config
     * @return
     */
    private static IColorManager getColorManager(JavaSourceViewerConfiguration config) {
        try {
            Field fColorManager = JavaSourceViewerConfiguration.class.getDeclaredField("fColorManager");
            fColorManager.setAccessible(true);
            return (IColorManager) fColorManager.get(config);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * The super constructor requires a preference store, but it is a private field
     * in JavaSourceViewerConfiguration.
     * This method uses reflection to get it.
     * @param config
     * @return
     */
    private static IPreferenceStore getPreferenceStore(TextSourceViewerConfiguration config) {
        try {
            Field fPreferenceStore = TextSourceViewerConfiguration.class.getDeclaredField("fPreferenceStore");
            fPreferenceStore.setAccessible(true);
            return (IPreferenceStore) fPreferenceStore.get(config);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}