package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.MUL;

public class ArrType extends Type {

    private AbstractExpr size;
    private Type childType;
    private DVal heapSizeMemory;
    private DAddr startHeap;
    private DAddr endHeap;
    private MemoryLocation.Register sizeEvaluated;

    public ArrType(Symbol name, AbstractExpr size, Type childType) {
        super(name);
        this.size = size;
        this.childType = childType;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public AbstractExpr getSize() {
        return size;
    }

    public void setSize(AbstractExpr newSize) {
        size = newSize;
    }

    public Type getChildType() {
        return childType;
    }

    /**
     * author: B
     * @return Array's element type
     */
    public Type elementType() {
        Type buffer = childType;
        while (buffer instanceof ArrType)
            buffer = ((ArrType) buffer).getChildType();
        return buffer;
    }

    public boolean isFloatArray() {
        if (childType instanceof ArrType)
            return ((ArrType) childType).isFloatArray();
        else
            return childType.isFloat();
    }

    public boolean isIntArray() {
        if (childType instanceof ArrType)
            return ((ArrType) childType).isIntArray();
        else
            return childType.isInt();
    }

    public boolean isIntOrFloatArray() {
        return isIntArray() || isFloatArray();
    }

    @Override
    public boolean sameType(Type otherType) {
        if (!otherType.isArray()) {
            return false;
        } else {
            Type ChildOtherType = ((ArrType) otherType).getChildType();
            return childType.sameType(ChildOtherType);
        }
    }

    public void codeGenEvaluateTotalSize(DecacCompiler compiler, Location location, MemoryLocation.Register totalSize) {
        if (childType instanceof ArrType) {
            // On calcul la taille du fils
            ((ArrType) childType).codeGenEvaluateTotalSize(compiler, location, totalSize);
        } else {
            compiler.addInstruction(new LOAD(new ImmediateInteger(1), totalSize.getRegister(compiler)));
        }
        try (MemoryLocation.Register sizeRegister = getSize().codeGenEvaluate(compiler).toRegister(compiler)) {
            compiler.addInstruction(new MUL(sizeRegister.getDVal(compiler), totalSize.getRegister(compiler)));
        }
        compiler.addInstruction(new ADD(new ImmediateInteger(2), totalSize.getRegister(compiler)));
    }

}
