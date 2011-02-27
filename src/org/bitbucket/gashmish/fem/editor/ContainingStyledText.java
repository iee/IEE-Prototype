/*
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
 * ContainingStyledText.java
 * Created: Jun 29, 2007
 * By: Andrew Eisenberg
 */
package org.bitbucket.gashmish.fem.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Warning! Not for the faint of heart!
 * 
 * Not supposed to extend styled text, but need to in order to control scrolling
 * behavior.
 * 
 * This class removes the flicker of embedded controls when there is a newline
 * entered in the containing editor.
 * 
 * 
 * @author aeisenberg
 * 
 */
public class ContainingStyledText extends StyledText {

    public ContainingStyledText(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    public void scroll(int destX, int destY, int x, int y, int width,
            int height, boolean all) {
        super.scroll(destX, destY, x, y, width, height, false);

        boolean normalScroll = false; // normal scroll or line feed
        try {
            throw new Exception();
        } catch (Exception e) {
            e.fillInStackTrace();
            normalScroll = !e.getStackTrace()[1].getMethodName().equals(
                    "scrollText");
        }

        if (all) {
            int caretPosition = this.getCaret().getLocation().y;
            int deltaX = destX - x, deltaY = destY - y;
            Control[] children = getChildren();
            for (int i = 0; i < children.length; i++) {
                Control child = children[i];
                Rectangle rect = child.getBounds();

                if (normalScroll || rect.y >= caretPosition) {
                    child.setLocation(rect.x + deltaX, rect.y + deltaY);
                }
            }
        }
    }

    /**
     * the super definition of this method always tries to give focus to the first child.
     * this is not the behavior we want.
     * 
     * focus should go to this StyledText and only afterwards decide if it should be propagated to a child
     */
    @Override
    public boolean setFocus() {
        checkWidget ();
        if ((getStyle() & SWT.NO_FOCUS) != 0) return false;
        
        // check to see if a child already has focus
        if (searchForFocusChild(this)) {
            return true;
        }
        
        return forceFocus ();
    }
    
    private boolean searchForFocusChild(Composite control) {
        // check to see if a child already has focus
        for (Control child : control.getChildren()) {
            if (child.isFocusControl()) {
                return true;
            }
            if (child instanceof Composite) {
                Composite childComposite = (Composite) child;
                if (searchForFocusChild(childComposite)) {
                    return true;
                }
            }
        }
        return false;
    }
}
