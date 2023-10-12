package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class IntType extends Type {

    public IntType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public boolean isIntOrFloat() {
        return true;
    }

    @Override
    public boolean isComparableToExact(Type t) {
        return t.isIntOrFloat();
    }

    @Override
    public boolean sameType(Type otherType) {
        return otherType.isInt();
    }

    @Override
    public boolean isCastCompatible(Type t) {
        return t.isIntOrFloat();
    }

}
