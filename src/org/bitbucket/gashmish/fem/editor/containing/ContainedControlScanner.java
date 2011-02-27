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
 * ContainingEditorScanner.java
 * Created: Jun 18, 2007
 * By: Andrew Eisenberg
 */
package org.bitbucket.gashmish.fem.editor.containing;


import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.openquark.cal.eclipse.embedded.exported.IEmbeddedCalConstants;



public class ContainedControlScanner 
        extends RuleBasedPartitionScanner implements IEmbeddedCalConstants {

    public final static String CONTAINED_EDITOR = "__contained_editor";  // serves as the partition name
    public final static String NOT_CONTAINED_EDITOR = "__not_contained_editor";  // serves as the partition name
    public final static IToken EDITOR_TOKEN = new Token(CONTAINED_EDITOR);
    public final static IToken NON_EDITOR_TOKEN = new Token(NOT_CONTAINED_EDITOR);


    /**
     * the opening of an embedded region looks like this: /*<--*/  /**
     * The closing of an embedded region looks like this: /*-->*/  /**
     */
    public ContainedControlScanner() {

        IPredicateRule[] rules = new IPredicateRule[1];
        rules[0] = new MultiLineRule(EMBEDDED_REGION_START, EMBEDDED_REGION_END, EDITOR_TOKEN, '\\', true);
        setPredicateRules(rules);

        setDefaultReturnToken(NON_EDITOR_TOKEN);
    }
}

