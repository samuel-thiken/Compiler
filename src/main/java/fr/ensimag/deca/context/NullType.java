package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class NullType extends Type {

    public NullType(SymbolTable.Symbol name) {
        super(name);
    }
    @Override
    public boolean isComparableToExact(Type t) {
        return t.isClassOrNull();
    }
    @Override
    public boolean sameType(Type otherType) {
        return otherType.isNull();
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        if (t instanceof ClassType)
            return true;
        return false;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    @Override
    public boolean isCastCompatible(Type t) {
        if (t.isClassOrNull())
            return true;
        return false;
    }
    @Override
    public boolean isAssignCompatible(Type t) {
        return false;
    }

}
