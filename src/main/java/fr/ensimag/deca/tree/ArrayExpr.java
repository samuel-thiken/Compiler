package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.context.ArrType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.memory.MemoryLocation.Register;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * Defines the expression of an array.
 * 
 * @author H
 */
public class ArrayExpr extends AbstractExpr {

    private ListExpr children;

    public ArrayExpr(ListExpr children) {
        this.children = children;
    }

    public int getNbOfEl() {
        return children.size();
    }

    public ListExpr getChildren() {
        return children;
    }

    public void setChildren(ListExpr newChildren) {
        children = newChildren;
    }

    /**
     * @author B
     */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeChild = null;
        AbstractExpr child = children.getList().get(0);
        child.verifyExpr(compiler, localEnv, currentClass);
        Type typeFirst = child.getType();
        int sizeFirst = 0;
        if (child instanceof ArrayExpr)
            sizeFirst = ((ArrayExpr) child).getNbOfEl();

        // Verification that all the children have the same type
        for (AbstractExpr elem : children.getList()) {
            Type type;
            if (elem instanceof ArrayExpr) {
                type = ((ArrayExpr) elem).verifyExpr(compiler, localEnv, currentClass);
                if (sizeFirst != 0 && ((ArrayExpr) elem).getNbOfEl() != sizeFirst)
                    throw new ContextualError(
                            String.format(ContextualError.INCOMPATIBLE_SIZE_OF_ARRAY, Integer.toString(sizeFirst),
                                    Integer.toString(((ArrayExpr) elem).getNbOfEl())),
                            getLocation());
            } else
                type = elem.verifyExpr(compiler, localEnv, currentClass);

            if ((!type.sameType(typeFirst)) && type.isIntOrFloat() && typeFirst.isIntOrFloat()) {
                if (type.isInt())
                    typeChild = type;
                else
                    typeChild = typeFirst;
            } else if ((!type.sameType(typeFirst)) && (type instanceof ArrType) && (typeFirst instanceof ArrType)
                    && ((ArrType) type).isIntOrFloatArray() && ((ArrType) typeFirst).isIntOrFloatArray()) {
                if (((ArrType) type).isIntArray())
                    typeChild = type;
                else
                    typeChild = typeFirst;
            }

            if ((!type.sameType(typeFirst)) && typeChild == null)
                throw new ContextualError(
                        String.format(ContextualError.INCOMPATIBLE_TYPE_ASSIGN, type.getName(),
                                typeFirst.getName()),
                        getLocation());
        }
        if (typeChild == null)
            typeChild = typeFirst;

        setType(new ArrType(compiler.symbolTable.create(typeChild.getName().getName() + "[]"),
                new IntLiteral(getNbOfEl()), typeChild));
        return getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("{");
        children.decompile(s);
        s.print("}");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        children.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // TODO Auto-generated method stub

    }

    public int getTotalSize() {
        int size = 2;
        for (AbstractExpr expr : children.getList()) {
            if (expr instanceof ArrayExpr) size += ((ArrayExpr) expr).getTotalSize();
            else size++;
        }
        return size;
    }

    public int codeGenInitRecSize(DecacCompiler compiler, MemoryLocation.Register addr, int offset) {
        int size = children.size();
        int elemSize = 0;

        // Données de ce tableau
        try (MemoryLocation.Register register = MemoryLocation.Register.create(compiler)) {
            // On stocke le nombre d'éléments
            RegisterOffset registerOffset = new RegisterOffset(offset, addr.getRegister(compiler));
            compiler.addInstruction(new LOAD(new ImmediateInteger(size), register.getRegister(compiler)));
            compiler.addInstruction(new STORE(register.getRegister(compiler), registerOffset));
        }
        offset++;
        int childOffset = offset + 1;
        for (AbstractExpr child : children.getList()) {
            if (child instanceof ArrayExpr) {
                childOffset = ((ArrayExpr) child).codeGenInitRecSize(compiler, addr, childOffset);
            } else {
                // On skip les éléments (pas une initialisation des valeurs)
                childOffset++;
            }
            // Calcul de la taille d'un élément
            if (elemSize == 0) elemSize = childOffset - (offset + 1);
        }

        // Suite des données du tableau
        try (MemoryLocation.Register register = MemoryLocation.Register.create(compiler)) {
            // On stocke la taille
            RegisterOffset registerOffset = new RegisterOffset(offset, addr.getRegister(compiler));
            compiler.addInstruction(new LOAD(new ImmediateInteger(elemSize), register.getRegister(compiler)));
            compiler.addInstruction(new STORE(register.getRegister(compiler), registerOffset));
        }

        // On devrait avoir : childOffset = offset + size * elemSize
        return childOffset;
    }

    public void codeGenVerifySizes(DecacCompiler compiler, ArrType type) {
        ArrayExpr expr = this;
        
        while (expr != null && type != null) {
            try (MemoryLocation.Register size = type.getSize().codeGenEvaluate(compiler).toRegister(compiler)) {
                compiler.addInstruction(new SUB(new ImmediateInteger(expr.children.size()), size.getRegister(compiler)));
                ExecutionError.check(compiler, size, Check.INCOMPATIBLE_ARRAY_ASSIGNATION, getLocation());
            }
            if (expr.children.getList().get(0) instanceof ArrayExpr) expr = (ArrayExpr) expr.children.getList().get(0);
            else expr = null;
            if (type.getChildType() instanceof ArrType) type = (ArrType) type.getChildType();
            else type = null;
        }
    }

    private int codeGenInitRecValues(DecacCompiler compiler, MemoryLocation.Register addr, int offset) {
        offset += 2;

        for (AbstractExpr child : children.getList()) {
            if (child instanceof ArrayExpr) {
                offset = ((ArrayExpr) child).codeGenInitRecValues(compiler, addr, offset);
            } else {
                try (MemoryLocation.Register element = child.codeGenEvaluate(compiler).toRegister(compiler)) {
                    RegisterOffset registerOffset = new RegisterOffset(offset, addr.getRegister(compiler));
                    compiler.addInstruction(new STORE(element.getRegister(compiler), registerOffset));
                }
                offset++;
            }
        }

        return offset;
    }

    /**
     * Initialize the array situated at the address 'addr'
     * 
     * @param compiler
     * @param addr
     * @param tempRegisterIndex
     * @return
     */
    public Register codeGenInit(DecacCompiler compiler, MemoryLocation.Address addr, Integer tempRegisterIndex) {
        codeGenInitRecValues(compiler, addr.toRegister(compiler), 0);
        return null;
    }

}
