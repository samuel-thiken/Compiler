package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public abstract class AbstractDeclMethod extends Tree {

        /**
         * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 2
         * 
         * @param compiler   contains "env_types" attribute
         * @param superClass
         *                   corresponds to the "superclass" of a method.
         */
        protected abstract void verifyDeclMethod(DecacCompiler compiler,
                        EnvironmentExp envClass, AbstractIdentifier currentClass,
                        AbstractIdentifier superClass)
                        throws ContextualError;

        /**
         * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 3
         * 
         * @param compiler   contains "env_types" attribute
         * @param superClass
         *                   corresponds to the "superclass" of a method.
         */
        protected abstract void verifyDeclMethodBody(DecacCompiler compiler,
                        EnvironmentExp envClass, AbstractIdentifier currentClass,
                        AbstractIdentifier superClass)
                        throws ContextualError;

        protected abstract void codeGenDeclMethod(DecacCompiler compiler, ClassDefinition currentClass);

}
