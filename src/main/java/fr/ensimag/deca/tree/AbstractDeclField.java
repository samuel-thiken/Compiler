package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public abstract class AbstractDeclField extends Tree {

    /**
     * Implements non-terminal "decl_field" of [SyntaxeContextuelle] in pass 2
     * 
     * @param compiler   contains "env_types" attribute
     * @param className
     *                   corresponds to the class in which the field is defined
     * @param superClass
     *                   corresponds to the super class of parameter "class"
     *                   attribute.
     */
    protected abstract void verifyDeclField(DecacCompiler compiler,
            EnvironmentExp envClass, AbstractIdentifier className, AbstractIdentifier superClass)
            throws ContextualError;

    /**
         * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 3
         * 
         * @param compiler   contains "env_types" attribute
         * @param superClass
         *                   corresponds to the "superclass" of a method.
         */
    protected abstract void verifyDeclFieldInit(DecacCompiler compiler,
                        EnvironmentExp envClass, AbstractIdentifier currentClass,
                        AbstractIdentifier superClass)
                        throws ContextualError;

    protected abstract void codeGenDeclField(DecacCompiler compiler, boolean disableStore);

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    public abstract AbstractIdentifier getFieldName();

}
