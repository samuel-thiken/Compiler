package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.Location;

/**
 * Definition of a class.
 *
 * @author gl20
 * @date 01/01/2023
 */
public class ClassDefinition extends TypeDefinition {

    private AbstractExpr expression;

    public AbstractExpr getExpression() {
        return expression;
    }

    public void setExpression(AbstractExpr expression) {
        this.expression = expression;
    }

    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public int getTotalNumberOfFields() {
        return numberOfFields + (superClass != null ? superClass.getTotalNumberOfFields() : 0);
    }

    public int incNumberOfFields() {
        numberOfFields++;
        return numberOfFields;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public int getTotalNumberOfMethods() {
        return numberOfMethods + (superClass != null ? superClass.getTotalNumberOfMethods() : 0);
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }

    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    private int numberOfFields = 0;
    private int numberOfMethods = 0;

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    };

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    private final EnvironmentExp members;
    private final ClassDefinition superClass;

    public EnvironmentExp getMembers() {
        return members;
    }

    public EnvironmentExp getAllMembers() {
        if (superClass == null) return getMembers();
        return EnvironmentExp.complete(superClass.getAllMembers(), members);
    }

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent);
        this.superClass = superClass;
    }

    public List<MethodRedefinition> getClassMethods(DecacCompiler compiler) {
        List<MethodRedefinition> myMethods;
        if (superClass != null)
            myMethods = superClass.getClassMethods(compiler);
        else
            myMethods = new ArrayList<>();

        for (Symbol method : getMembers().envData.keySet()) {
            if (!(getMembers().envData.get(method) instanceof MethodDefinition))
                continue;
            Optional<MethodRedefinition> redef = myMethods.stream().filter(r -> r.methodName.equals(method))
                    .findFirst();
            if (redef.isPresent()) {
                redef.get().className = getType().getName();
            } else {
                myMethods.add(new MethodRedefinition(getType().getName(), method, (MethodDefinition) getMembers().envData.get(method)));
            }
        }

        return myMethods;
    }

    public static class MethodRedefinition {
        public Symbol className;
        public Symbol methodName;
        public MethodDefinition methodDef;

        public MethodRedefinition(Symbol c, Symbol m, MethodDefinition md) {
            this.className = c;
            this.methodName = m;
            this.methodDef = md;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MethodRedefinition) {
                MethodRedefinition objR = (MethodRedefinition) obj;
                return this.className.equals(objR.className) && this.methodName.equals(objR.methodName);
            } else
                return false;
        }
    }

}
