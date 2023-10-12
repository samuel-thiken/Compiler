package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclParam extends TreeList<AbstractDeclParam> {

    @Override
    public void decompile(IndentPrintStream s) {
        int i = 0;
        for(AbstractDeclParam p : getList()) {
            if (i != 0) s.print(", ");
            p.decompile(s);
            i++;
        }
    }

    /**
     * Implements non-terminal "list_decl_param" of [SyntaxeContextuelle] in pass 2 & 3
     * 
     * @param compiler     contains the "env_types" attribute
     */
    public Signature verifyListDeclParam(DecacCompiler compiler, EnvironmentExp envParams) throws ContextualError {
        Signature sig = new Signature();
        for (AbstractDeclParam i : getList()) {
            sig.add(i.verifyDeclParam(compiler, envParams));
        }
        return sig;
    }

}
