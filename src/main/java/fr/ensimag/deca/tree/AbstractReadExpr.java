package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;

/**
 * read...() statement.
 *
 * @author gl20
 * @date 01/01/2023
 */
public abstract class AbstractReadExpr extends AbstractExpr {

    public AbstractReadExpr() {
        super();
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // Nothing to do
    }

}
