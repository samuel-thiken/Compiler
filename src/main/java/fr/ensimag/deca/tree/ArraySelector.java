package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.context.ArrType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.SUB;

public class ArraySelector extends AbstractIdentifier {

    private AbstractIdentifier ident;
    private ListExpr indices;

    public ArraySelector(AbstractIdentifier ident, ListExpr indices) {
        this.ident = ident;
        this.indices = indices;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // On vérifie que la variable existe dans l'environnement
        Type varType = ident.verifyExpr(compiler, localEnv, currentClass);
        // On restore son type et sa définition
        for (AbstractExpr e : indices.getModifiableList()) {
            e.verifyExpr(compiler, localEnv, currentClass);
            if (!(varType instanceof ArrType)) {
                throw new ContextualError(
                        String.format(ContextualError.NON_INDEXABLE_VARIABLE, ident.getType().getName()),
                        getLocation());
            } else {
                varType = ((ArrType) varType).getChildType();
            }
        }
        setType(varType);
        setDefinition(new VariableDefinition(varType, getLocation()));
        return varType;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        ident.decompile(s);
        for (AbstractExpr expr : indices.getList()) {
            s.print("[");
            expr.decompile(s);
            s.print("]");
        }
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        ident.prettyPrint(s, prefix, false);
        indices.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // TODO Auto-generated method stub

    }

    @Override
    public ClassDefinition getClassDefinition() {
        return ident.getClassDefinition();
    }

    @Override
    public FieldDefinition getFieldDefinition() {
        return ident.getFieldDefinition();
    }

    @Override
    public MethodDefinition getMethodDefinition() {
        return ident.getMethodDefinition();
    }

    @Override
    public Symbol getName() {
        return ident.getName();
    }

    @Override
    public ExpDefinition getExpDefinition() {
        return ident.getExpDefinition();
    }

    @Override
    public VariableDefinition getVariableDefinition() {
        return ident.getVariableDefinition();
    }

    @Override
    public Type verifyType(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        return getType();
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        // MemoryLocation.Address register;
        // Type identType = ident.getType();

        // try (MemoryLocation.Register offset = MemoryLocation.Immediate.create(null,
        // new ImmediateInteger(0))
        // .toRegister(compiler);) {
        // for (AbstractExpr indice : indices.getList()) {
        // // try (MemoryLocation.Register checkIndex =
        // indice.codeGenEvaluate(compiler).toRegister(compiler);) {
        // // compiler.addInstruction(
        // // new SUB(((ArrType)
        // identType).getSize().codeGenEvaluate(compiler).getDVal(compiler),
        // // checkIndex.getRegister(compiler)));
        // // ExecutionError.check(compiler, checkIndex.toRegister(compiler),
        // Check.INDEX_OUT_OF_BOUNDS, getLocation());
        // // }
        // try (MemoryLocation.Register buffer =
        // indice.codeGenEvaluate(compiler).toRegister(compiler);) {
        // if (((ArrType) ident.getType()).getChildType() instanceof ArrType)
        // compiler.addInstruction(
        // new MUL(((ArrType) ((ArrType)
        // ident.getType()).getChildType()).getHeapSizeMemory(),
        // buffer.getRegister(compiler)));
        // else
        // compiler.addInstruction(new MUL(new ImmediateInteger(1),
        // buffer.getRegister(compiler)));

        // compiler.addInstruction(new ADD(new ImmediateInteger(1),
        // offset.getRegister(compiler)));
        // compiler.addInstruction(new ADD(buffer.getDVal(compiler),
        // offset.getRegister(compiler)));
        // }
        // }

        // // getting the address of the variable in the stack (memory location)
        // // MemoryLocation identAddr = ident.codeGenEvaluate(compiler);
        // register = MemoryLocation.Address.create(this, ident, offset);
        // }
        // return register;

        MemoryLocation register;
        MemoryLocation.Register addr = ident.codeGenEvaluate(compiler).toRegister(compiler);

        try (MemoryLocation.Register offset = MemoryLocation.Immediate.create(null, new ImmediateInteger(0))
                .toRegister(compiler);) {
            int i = 1;
            for (AbstractExpr indice : indices.getList()) {
                compiler.addComment(String.format("Calcul de l'index n°%d", i++));
                try (MemoryLocation indexRegister = indice.codeGenEvaluate(compiler)) {
                    // Index out of bound exception
                    if (compiler.getCompilerOptions().shouldErrorCheck()) {
                        compiler.addComment("Gestion des erreurs");
                        // Check 1 : index < 0
                        ExecutionError.check(compiler, indexRegister, Check.INDEX_OUT_OF_BOUNDS_NEGATIVE,
                                getLocation());
                        // Check 2 : index >= size
                        try (MemoryLocation.Register indexCheck = MemoryLocation.Register.create(compiler);) {
                            compiler.addInstruction(new LOAD(
                                    new RegisterOffset(0, addr.getRegister(compiler), offset.getRegister(compiler)),
                                    indexCheck.getRegister(compiler)));
                            compiler.addInstruction(
                                    new SUB(indexRegister.getDVal(compiler), indexCheck.getRegister(compiler)));
                            ExecutionError.check(compiler, indexCheck, Check.INDEX_OUT_OF_BOUNDS_GREATER,
                                    getLocation());
                        }
                    }
                    
                    compiler.addComment("Déplacement de l'offset");
                    // Move the offset
                    try (MemoryLocation.Register contentSizeRegister = MemoryLocation.Register.create(compiler)) {
                        compiler.addInstruction(new LOAD(
                                    new RegisterOffset(1, addr.getRegister(compiler), offset.getRegister(compiler)),
                                    contentSizeRegister.getRegister(compiler)));
                        compiler.addInstruction(new MUL(indexRegister.getDVal(compiler), contentSizeRegister.getRegister(compiler)));
                        compiler.addInstruction(new ADD(contentSizeRegister.getDVal(compiler), offset.getRegister(compiler)));
                        compiler.addInstruction(new ADD(new ImmediateInteger(2), offset.getRegister(compiler)));
                    }
                }
            }
            register = MemoryLocation.Address.create(this, ident, offset);
        }

        return register;
    }

    @Override
    public Definition getDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefinition(Definition definition) {
        // TODO Auto-generated method stub

    }

}
