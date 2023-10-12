package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ArrType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * @author gl20
 * @date 01/01/2023
 */
public class DeclVar extends AbstractDeclVar {

    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {

        type.verifyType(compiler, localEnv, currentClass);

        // Check if the var type is in the EnvironmentType
        if (!compiler.environmentType.has(type.getName()))
            throw new ContextualError(String.format(ContextualError.UNDEFINED_TYPE, type.getName()), getLocation());

        // Checking that var type is not void
        Type varType = compiler.environmentType.defOfType(type.getName()).getType();
        if (varType.sameType(compiler.environmentType.VOID))
            throw new ContextualError(String.format(ContextualError.VOID_VARIABLE_NOT_ALLOWED, varName.getName()),
                    getLocation());
        else if ((varType instanceof ArrType)
                && ((ArrType) varType).elementType().sameType(compiler.environmentType.VOID))
            throw new ContextualError(String.format(ContextualError.VOID_VARIABLE_NOT_ALLOWED, varName.getName()),
                    getLocation());

        // Set var type
        varName.setDefinition(new VariableDefinition(varType, type.getLocation()));
        varName.setType(varType);

        // Returned environment, env_exp(r)
        try {
            localEnv.declare(varName.getName(), varName.getExpDefinition());
            ;

        } catch (DoubleDefException e) {
            throw new ContextualError(String.format(ContextualError.ALREADY_DEFINED_VAR, varName.getName()),
                    getLocation());
        }
        initialization.verifyInitialization(compiler, type.getType(), localEnv, currentClass);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        varName.decompile(s);
        // s.print(type.getName() + " " + varName.getName());
        if (initialization instanceof Initialization) {
            s.print(" = ");
            initialization.decompile(s);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenDeclVar(DecacCompiler compiler) {
        compiler.addComment(String.format("Instantiation de la valeur de %s", varName.getName()));

        if (!(type instanceof ArrayIdentifier)) { // simple variable
            RegisterOffset addr = new RegisterOffset(compiler.getMemoryManager().getNewStackAddress(), Register.GB);
            compiler.getMemoryManager().setVarExpression(varName.getName(), varName);
            MemoryLocation.Address addrRegister = MemoryLocation.Address.create(varName, addr);
            varName.setMemoryLocation(addrRegister);

            try (MemoryLocation.Register register = initialization.codeGenInitialization(compiler, addrRegister, null,
                    false);) {
                if (register != null)
                    register.changeReference(varName.getMemoryLocation(compiler));
            }
        } else {
            ArrType typeArr = ((ArrType) type.getType());
            // if the array doesn't possess a delimited size
            // storing the variable's address in the stack
            RegisterOffset regOffset = new RegisterOffset(compiler.getMemoryManager().getNewStackAddress(),
                    Register.GB);
            MemoryLocation.Address addr = MemoryLocation.Address.create(varName, regOffset);
            compiler.getMemoryManager().setVarExpression(varName.getName(), varName);
            // setting the address of the variable to that of the register containing the
            // heap's adress
            varName.setMemoryLocation(addr);

            // getting the address of the first element in the heap and loading in the
            // variable's register
            try (MemoryLocation.Register register = MemoryLocation.Register.create(compiler);) {

                if (!((typeArr.getSize()) instanceof EmptyExpr)) {
                    typeArr.codeGenEvaluateTotalSize(compiler, getLocation(), register);
                    compiler.addInstruction(new NEW(register.getDVal(compiler), register.getRegister(compiler)));
                    // storing the address inside 'register' in the stack at the address of the
                    // variable
                    compiler.addInstruction(
                            new STORE(register.getRegister(compiler), addr.getRegisterOffset(compiler)));

                    compiler.addComment("Stockage des données du tableau");
                    try (MemoryLocation.Register offset = MemoryLocation.Immediate.create(null, new ImmediateInteger(0))
                            .toRegister(compiler)) {
                        // typeArr.codeGenInitRecSize(compiler, register, offset);
                    }

                    if (initialization instanceof Initialization) {
                        compiler.addComment("Stockage des valeurs du tableau");
                        ((ArrayExpr) ((Initialization) initialization).getExpression()).codeGenVerifySizes(compiler,
                                typeArr);
                    }

                    initialization.codeGenInitialization(compiler, addr, null, false);
                } else {
                    // Type initType = ((Initialization) initialization).getExpression().getType();
                    // if (initType instanceof ArrType && !(((ArrType) initType).getSize()
                    // instanceof EmptyExpr)) {
                    // typeArr = ((ArrType) ((Initialization)
                    // initialization).getExpression().getType());

                    // compiler.addComment("Stockage des données du tableau (from expr type)");
                    // try (MemoryLocation.Register offset = MemoryLocation.Immediate.create(null,
                    // new ImmediateInteger(0))
                    // .toRegister(compiler)) {
                    // typeArr.codeGenInitRecSize(compiler, register, offset);
                    // }
                    // } else
                    if (initialization instanceof Initialization && ((Initialization) initialization).getExpression() instanceof ArrayExpr) {
                        ArrayExpr arrayExpr = ((ArrayExpr) ((Initialization) initialization).getExpression());

                        int totalSize = arrayExpr.getTotalSize();
                        compiler.addInstruction(
                                new NEW(new ImmediateInteger(totalSize), register.getRegister(compiler)));
                        // storing the address inside 'register' in the stack at the address of the
                        // variable
                        compiler.addInstruction(
                                new STORE(register.getRegister(compiler), addr.getRegisterOffset(compiler)));

                        compiler.addComment("Stockage des données du tableau (from expr)");
                        arrayExpr.codeGenInitRecSize(compiler, register, 0);
                    }
                    compiler.addComment("Stockage des valeurs du tableau");
                    initialization.codeGenInitialization(compiler, addr, null, false);
                }
            }

        }
    }
}
