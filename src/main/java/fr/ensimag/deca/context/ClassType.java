package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Type defined by a class.
 *
 * @author gl20
 * @date 01/01/2023
 */
public class ClassType extends Type {

    protected ClassDefinition definition;

    public ClassDefinition getDefinition() {
        return this.definition;
    }

    public void setDefinition(ClassDefinition definition) {
        this.definition = definition;
    }

    @Override
    public ClassType asClassType(String errorMessage, Location l) {
        return this;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    /**
     * Standard creation of a type class.
     */
    public ClassType(Symbol className, Location location, ClassDefinition superClass) {
        super(className);
    }

    /**
     * Creates a type representing a class className.
     * (To be used by subclasses only)
     */
    protected ClassType(Symbol className) {
        super(className);
    }

    @Override
    public boolean isComparableToExact(Type t) {
        return t.isClassOrNull();
    }
    @Override
    public boolean sameType(Type otherType) {
        return otherType.isClass();
    }

    /**
     * Return true if potentialSuperClass is a superclass of this class.
     */
    public boolean isSubClassOf(ClassType potentialSuperClass) {
        if (potentialSuperClass.getName().toString().equals(this.getName().toString()))
            return true;
        if (getName().toString().equals("Object"))
            return false;
        ClassType superClass = getDefinition().getSuperClass().getType();
        return superClass.isSubClassOf(potentialSuperClass);
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        if (!(t instanceof ClassType))
            return false;
        ClassType type = (ClassType) t;
        return isSubClassOf(type);
    }

    @Override
    public boolean isCastCompatible(Type t) {
        return isSubTypeOf(t) || t.isSubTypeOf(this);
    }

    @Override
    public boolean isAssignCompatible(Type typeExpr) {
        return typeExpr.isSubTypeOf(this);
    }

}
