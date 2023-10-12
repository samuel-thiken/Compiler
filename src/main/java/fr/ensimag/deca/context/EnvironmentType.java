package fr.ensimag.deca.context;

import java.util.HashMap;
import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl20
 * @date 01/01/2023
 */
public class EnvironmentType {
    public EnvironmentType(DecacCompiler compiler) {

        envTypes = new HashMap<Symbol, TypeDefinition>();

        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);

        Symbol nullSymb = compiler.createSymbol("null");
        NULL = new NullType(nullSymb);

        Symbol objectSymb = compiler.createSymbol("Object");
        OBJECT = new ClassType(objectSymb, Location.BUILTIN, null);
        // DANGER OBJECT a une définition ?
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    /**
     * Check if a symbol is contained in the environment type.
     * 
     * @author H
     * @param s
     * @return
     */
    public boolean has(Symbol s) {
        return envTypes.containsKey(s);
    }

    /**
     * Add a new entry <Symbol, TypeDefinition> to the environment type.
     * Pre-condition : the
     * 
     * @author H
     * 
     */
    public void add(Symbol s, TypeDefinition type) throws ContextualError {
        if (has(s))
            throw new ContextualError(String.format(ContextualError.ALREADY_DEFINED_CLASS, type.getType()),
                    type.getLocation());
        envTypes.put(s, type);
    }

    public final VoidType VOID;
    public final IntType INT;
    public final FloatType FLOAT;
    public final StringType STRING;
    public final BooleanType BOOLEAN;
    public final NullType NULL;
    public final ClassType OBJECT;
}
