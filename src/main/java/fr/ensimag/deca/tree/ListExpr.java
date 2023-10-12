package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl20
 * @date 01/01/2023
 */
public class ListExpr extends TreeList<AbstractExpr> {

    @Override
    public void decompile(IndentPrintStream s) {
        int i = 0;
        for (AbstractExpr t : getList()) {
            i++;
            t.decompile(s);
            if (i != getList().size())
                s.print(", ");
        }
    }
}
