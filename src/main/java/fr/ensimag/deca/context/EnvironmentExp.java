package fr.ensimag.deca.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 * 
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 * 
 * The dictionary at the head of this list thus corresponds to the "current"
 * block (eg class).
 * 
 * Searching a definition (through method get) is done in the "current"
 * dictionary and in the parentEnvironment if it fails.
 * 
 * Insertion (through method declare) is always done in the "current"
 * dictionary.
 * 
 * @author gl20
 * @date 01/01/2023
 */
public class EnvironmentExp {

    EnvironmentExp parentEnvironment;
    Map<Symbol, ExpDefinition> envData;

    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
        this.envData = new HashMap<Symbol, ExpDefinition>();
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    public EnvironmentExp getParentEnvironment() {
        return parentEnvironment;
    }

    public void setParentEnvironment(EnvironmentExp parent) {
        this.parentEnvironment = parent;
    }

    public Set<Entry<Symbol, ExpDefinition>> getDefinitions() {
        return envData.entrySet();
    }

    /***
     * Return the stacking of the environments env1 and env2
     * 
     * @author R
     * @param env1
     * @param env2
     * @return
     * @throws DoubleDefException
     */
    public static EnvironmentExp complete(EnvironmentExp env1, EnvironmentExp env2) {
        if (env2 == null)
            return env1;
        EnvironmentExp stacked = new EnvironmentExp(env1.getParentEnvironment());

        for (Symbol s : env1.envData.keySet()) {
            try {
                stacked.declare(s, env1.get(s));
            } catch (DoubleDefException e) {
                // Ne devrait pas arriver
            }
        }
        for (Symbol s : env2.envData.keySet()) {
            if (!env1.has(s)) {
                try {
                    stacked.declare(s, env2.get(s));
                } catch (Exception e) {
                    // Ne devrait pas arriver
                }
            }
        }

        return stacked;
    }

    /**
     * Do the disjoinct union between the current environment and the environnement
     * env,
     * and store it in the current environment.
     * 
     * @author R
     * @param env
     * @throws DoubleDefException
     */
    public void distinctUnion(EnvironmentExp env) throws DoubleDefException {
        for (Symbol s : env.envData.keySet()) {
            if (this.hasSelf(s))
                throw new DoubleDefException();
            this.declare(s, env.get(s));
        }
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     * 
     * @author R
     */
    public ExpDefinition get(Symbol key) {
        if (envData.containsKey(key)) {
            return envData.get(key);
        }
        if (parentEnvironment != null) {
            return parentEnvironment.get(key);
        }
        return null;
    }

    /**
     * Return True if the symbol is in the environment or its parent environment
     * (recursively), else False.
     * 
     * @author R
     * @param key
     * @return
     */
    public boolean has(Symbol key) {
        return envData.containsKey(key) || (parentEnvironment != null && parentEnvironment.has(key));
    }

    public boolean hasSelf(Symbol key) {
        return envData.containsKey(key);
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary
     * - or, hides the previous declaration otherwise.
     * 
     * @author R
     * @param name
     *             Name of the symbol to define
     * @param def
     *             Definition of the symbol
     * @throws DoubleDefException
     *                            if the symbol is already defined at the "current"
     *                            dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        if (envData.containsKey(name)) {
            throw new DoubleDefException();
        } else {
            envData.put(name, def);
        }
    }

}
