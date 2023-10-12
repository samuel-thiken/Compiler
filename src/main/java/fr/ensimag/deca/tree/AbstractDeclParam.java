package fr.ensimag.deca.tree;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

public abstract class AbstractDeclParam extends Tree {

    final private AbstractIdentifier type;
    final private AbstractIdentifier paramName;

    public AbstractDeclParam(AbstractIdentifier type, AbstractIdentifier paramName) {
        Validate.notNull(type);
        Validate.notNull(paramName);
        this.type = type;
        this.paramName = paramName;
    }

    public AbstractIdentifier getType() {
        return type;
    }

    public AbstractIdentifier getParamName() {
        return paramName;
    }

    /**
     * Implements non-terminal "decl_param" of [SyntaxeContextuelle] in pass 3
     * 
     * @param compiler contains "env_types" attribute
     * 
     */
    protected abstract Type verifyDeclParam(DecacCompiler compiler, EnvironmentExp envParams)
            throws ContextualError;

    protected abstract void codeGenDeclParam(DecacCompiler compiler);

}
