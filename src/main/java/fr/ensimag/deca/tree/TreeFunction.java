package fr.ensimag.deca.tree;

/**
 * Function that takes a tree as argument.
 * 
 * @see fr.ensimag.deca.tree.Tree#iter(TreeFunction)
 * 
 * @author gl20
 * @date 01/01/2023
 */
public interface TreeFunction {
    void apply(Tree t);
}
