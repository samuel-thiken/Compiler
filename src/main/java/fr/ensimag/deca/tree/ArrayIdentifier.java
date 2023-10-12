package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ArrType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Defines an identifier for the array type (which is a linked list of array's
 * identifiers)
 * Each cell in the linked list has a size and a pointer to its unique child (an
 * array or the type content)
 * 
 * @author H & B
 */
public class ArrayIdentifier extends Identifier {

    private AbstractExpr size;
    private AbstractIdentifier child;

    public ArrayIdentifier(Symbol name, AbstractExpr size, AbstractIdentifier child) {
        super(name);
        this.size = size;
        this.child = child;
    }

    public AbstractExpr getSize() {
        return size;
    }

    public void setChild(AbstractIdentifier newChild) {
        this.child = newChild;
    }

    public AbstractIdentifier getChild() {
        return child;
    }

    @Override
    String prettyPrintNode() {
        return "ArrayIdentifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
        size.prettyPrint(s, prefix, false);
        child.prettyPrintType(s, prefix);
    }

    public void setSize(AbstractExpr size) {
        this.size = size;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getrType().decompile(s);
        TreeList<AbstractExpr> sizes = getSizes();
        for(AbstractExpr expr : sizes.getList()) {
            s.print("[");
            expr.decompile(s);
            s.print("]");
        }
    }

    private TreeList<AbstractExpr> getSizes() {
        ListExpr sizes = new ListExpr();
        sizes.add(size);
        if(child instanceof ArrayIdentifier) {
            sizes.addList(((ArrayIdentifier) child).getSizes());
        } 
        return (TreeList<AbstractExpr>) sizes;
    }

    private Identifier getrType() {
        if(child instanceof ArrayIdentifier) {
            return  ((ArrayIdentifier) child).getrType();
        }
        return (Identifier) child;
    }

    @Override
    public Type verifyType(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type arrType = verificationType(compiler, localEnv, currentClass, this);
        setDefinition(new TypeDefinition(arrType, getLocation()));
        return arrType;
    }

    public Type verificationType(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            AbstractIdentifier ident) throws ContextualError {
        if (ident instanceof ArrayIdentifier) {
            Type calculatedType = verificationType(compiler, localEnv, currentClass,
                    ((ArrayIdentifier) ident).getChild());

            Symbol symbolArray;
            if (calculatedType instanceof ArrType)
                symbolArray = compiler.symbolTable.create(((ArrType) calculatedType).getName().getName() + "[]");
            else
                symbolArray = compiler.symbolTable.create(calculatedType.getName().getName() + "[]");

            // verification of the array's size
            ((ArrayIdentifier) ident).getSize().verifyExpr(compiler, localEnv, currentClass);

            if (!((ArrayIdentifier) ident).getSize().getType().isInt())
                throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_WITH_INT,
                        ((ArrayIdentifier) ident).getSize().getType().getName()), getLocation());

            ident.setType(new ArrType(symbolArray, ((ArrayIdentifier) ident).getSize(), calculatedType));
            if (!compiler.environmentType.has(ident.getName()))
                compiler.environmentType.add(ident.getType().getName(),
                        new TypeDefinition(ident.getType(), Location.BUILTIN));

        } else {
            // Check if type exists
            if (compiler.environmentType.defOfType(ident.getName()) == null)
                throw new ContextualError(String.format(ContextualError.UNDEFINED_TYPE, ident.getName()),
                        getLocation());

            ident.setType(compiler.environmentType.defOfType(ident.getName()).getType());
            if (!compiler.environmentType.has(ident.getName()))
                compiler.environmentType.add(ident.getType().getName(),
                        new TypeDefinition(ident.getType(), Location.BUILTIN));

            // Check if the var type is in the EnvironmentType
            if (!compiler.environmentType.has(ident.getName()))
                throw new ContextualError(String.format(ContextualError.UNDEFINED_TYPE, ident.getName()),
                        getLocation());
        }

        return ident.getType();
    }
}
