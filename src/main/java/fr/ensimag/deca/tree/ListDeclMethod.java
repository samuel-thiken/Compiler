package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod m : getList()) {
            m.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_method" of [SyntaxeContextuelle] in pass 2
     * 
     * @param compiler   contains the "env_types" attribute
     * @param superClass
     *                   corresponds to "class" attribute (null in the main bloc).
     */
    public void verifyListDeclMethod(DecacCompiler compiler, EnvironmentExp envClass,
            AbstractIdentifier currentClass, AbstractIdentifier superClass) throws ContextualError {
        for (AbstractDeclMethod i : getList()) {
            i.verifyDeclMethod(compiler, envClass, currentClass, superClass);
        }

    }

    /**
     * Implements non-terminal "list_decl_method" of [SyntaxeContextuelle] in pass 3
     * 
     * @param compiler   contains the "env_types" attribute
     * @param superClass
     *                   corresponds to "class" attribute (null in the main bloc).
     */
    public void verifyListDeclMethodBody(DecacCompiler compiler, EnvironmentExp envClass,
            AbstractIdentifier currentClass, AbstractIdentifier superClass) throws ContextualError {
        for (AbstractDeclMethod i : getList()) {
            i.verifyDeclMethodBody(compiler, envClass, currentClass, superClass);

        }
    }

    public void codeGenListDeclMethod(DecacCompiler compiler, ClassDefinition currentClass) {
        for (AbstractDeclMethod i : getList()) {
            i.codeGenDeclMethod(compiler, currentClass);
        }
    }
}
