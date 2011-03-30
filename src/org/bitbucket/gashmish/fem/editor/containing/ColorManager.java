package org.bitbucket.gashmish.fem.editor.containing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {
    private Display d = Display.getCurrent();

    protected Map<RGB,Color> fColorTable = new HashMap<RGB,Color>(10);

    public void dispose() {
        Iterator<Color> e = fColorTable.values().iterator();
        while (e.hasNext())
            e.next().dispose();
    }
    public Color getColor(RGB rgb) {
        Color color = fColorTable.get(rgb);
        if (color == null) {
            color = new Color(d, rgb);
            fColorTable.put(rgb, color);
        }
        return color;
    }
}
