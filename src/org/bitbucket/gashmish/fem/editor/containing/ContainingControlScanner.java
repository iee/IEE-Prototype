package org.bitbucket.gashmish.fem.editor.containing;


import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class ContainingControlScanner
        extends RuleBasedPartitionScanner {

    public final static String CONTAINED_EDITOR = "__contained_editor";  // serves as the partition name
    public final static String NOT_CONTAINED_EDITOR = "__not_contained_editor";  // serves as the partition name
    public final static IToken EDITOR_TOKEN = new Token(CONTAINED_EDITOR);
    public final static IToken NON_EDITOR_TOKEN = new Token(NOT_CONTAINED_EDITOR);


    /**
     * the opening of an embedded region looks like this: /*<--*/  /**
     * The closing of an embedded region looks like this: /*-->*/  /**
     */
    public ContainingControlScanner() {

        IPredicateRule[] rules = new IPredicateRule[1];
        rules[0] = new MultiLineRule("/*", "*/", EDITOR_TOKEN, '\\', true);
        setPredicateRules(rules);

        setDefaultReturnToken(NON_EDITOR_TOKEN);
    }
}

