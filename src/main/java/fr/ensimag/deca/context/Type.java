package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Deca Type (internal representation of the compiler)
 *
 * @author gl20
 * @date 01/01/2023
 */

public abstract class Type {

    /**
     * True if this and otherType represent the same type (in the case of
     * classes, this means they represent the same class).
     */
    public abstract boolean sameType(Type otherType);

    private final Symbol name;

    public Type(Symbol name) {
        this.name = name;
    }

    public Symbol getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName().toString();
    }

    /**
     * Returns true if the type t is comparable to the current type (regarding
     * AsbtractExprExactCmp)
     * 
     * @param t
     * @return
     */

    public boolean isComparableToExact(Type t) {
        return false;
    }

    /**
     * Method that indicates if the type of parameter 't' can be
     * cast to the one of 'this'.
     * 
     * @author H
     * @param t
     * @return
     */
    public boolean isSubTypeOf(Type t) {
        return sameType(t);
    }

    /**
     * Indicates if the type t can be assigned to the current type
     * 
     * @author H
     * @param t
     * @return
     */
    public boolean isAssignCompatible(Type typeExpr) {
        return sameType(typeExpr);
    }

    /**
     * Indicates if the type t can be cast to the current type or inversely
     * 
     * @author H
     * @param t
     * @return
     */
    public boolean isCastCompatible(Type t) {
        return sameType(t);
    }

    public boolean isClass() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public boolean isClassOrNull() {
        return false;
    }

    /**
     * Returns true if the type is an Int or a Float
     * 
     * @author H
     */
    public boolean isIntOrFloat() {
        return false;
    }

    /**
     * Returns the same object, as type ClassType, if possible. Throws
     * ContextualError(errorMessage, l) otherwise.
     *
     * Can be seen as a cast, but throws an explicit contextual error when the
     * cast fails.
     */
    public ClassType asClassType(String errorMessage, Location l)
            throws ContextualError {
        throw new ContextualError(errorMessage, l);
    }

}