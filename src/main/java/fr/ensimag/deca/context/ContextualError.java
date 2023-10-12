package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.LocationException;

/**
 * Exception raised when a contextual error is found.
 *
 * @author gl20
 * @date 01/01/2023
 */
public class ContextualError extends LocationException {
    private static final long serialVersionUID = -8122514996569278575L;

    public ContextualError(String message, Location location) {
        super(message, location);
    }

    public static String UNDEFINED_VAR = "UNDEFINED_VAR: Variable %s is undefined";
    public static String UNDEFINED_TYPE = "UNDEFINED_TYPE: Type %s is undefined";
    public static String NOT_A_BOOLEAN_CONDITION = "NOT_A_BOOLEAN_CONDITION: Expression %s is not a boolean expression";
    public static String VOID_VARIABLE_NOT_ALLOWED = "VOID_VARIABLE_NOT_ALLOWED: Variable %s cannot be of type void";
    public static String INCOMPATIBLE_TYPE_ASSIGN = "INCOMPATIBLE_TYPE_ASSIGN: Cannot assign type %s to variable of type %s";
    public static String INVALID_CAST = "INVALID_CAST: Type %s cannot be cast to type %s";
    public static String INCOMPATIBLE_TYPE_BINARY_OPERATOR = "INCOMPATIBLE_TYPE_OPERATOR: Type %s is incompatible with type %s for the operator %s";
    public static String INCOMPATIBLE_TYPE_UN_OPERATOR = "INCOMPATIBLE_TYPE_UN_OPERATOR: Type %s is incompatible with the operator %s";
    public static String ALREADY_DEFINED_VAR = "ALREADY_DEFINED_VAR: Variable %s is already defined";

    /* ---- object related exceptions ---- */

    public static String UNDEFINED_CLASS_FIELD = "UNDEFINED_CLASS_FIELD: Field %s of class %s is undefined";
    public static String ALREADY_DEFINED_CLASS = "ALREADY_DEFINED_CLASS: Class %s is already defined";
    public static String INCOMPATIBLE_TYPE_EXTENDS = "INCOMPATIBLE_TYPE_EXTENDS: Class %s cannot extends type %s";
    public static String NOT_A_TYPE_CLASS = "NOT_A_TYPE_CLASS: Type %s is not a class type";

    // selection, this, instance of
    public static String INCOMPATIBLE_INSTANCE_OF = "INCOMPATIBLE_INSTANCE_OF: Type %s is incompatible with 'instance of'";
    public static String CANNOT_USE_THIS_OUTSIDE_CLASS = "CANNOT_USE_THIS_OUTSIDE_CLASS: Keyword `this` invalid in non-class context";
    public static String INCOMPATIBLE_SELECTION = "INCOMPATIBLE_SELECTION: Field with name %s doesn't exist in environment of class %s";

    // methods
    public static String UNDEFINED_METHOD = "UNDEFINED_METHOD: Method %s is undefined";
    public static String INVALID_METHOD_SIGNATURE_OVERRIDE = "INVALID_METHOD_SIGNATURE_OVERRIDE: Override method %s in class %s doesn't have the same signature as in one of its superclasses";
    public static String NOT_A_SUBTYPE_RETURN = "NOT_A_SUBTYPE_RETURN: The return type of method %s in class %s must be the same type or a subtype of type %s";
    public static String ALREADY_DEFINED_METHOD = "ALREADY_DEFINED_METHOD: Method %s is already defined";
    public static String WRONG_RETURN_TYPE = "WRONG_RETURN_TYPE: The return type of this method must be of type %s";
    public static String NO_RETURN_IN_VOID_METHOD = "NO_RETURN_IN_VOID_METHOD: Cannot return in void method";
    public static String INVALID_METHOD_NAME = "INVALID_METHOD_NAME: A method cannot have the same name as an existing field or method in the same class";
    public static String INVALID_METHOD_CALL = "INVALID_METHOD_CALL: The method is being called on a non void, non class type variable or outside the method definition scope";

    // methods parameters
    public static String ALREADY_DEFINED_PARAM = "ALREADY_DEFINED_PARAM: Parameter %s is already defined";
    public static String VOID_PARAM_NOT_ALLOWED = "VOID_PARAM_NOT_ALLOWED: Parameter %s cannot be of type void";
    public static String INCOMPATIBLE_TYPE_PARAMETER = "INCOMPATIBLE_TYPE_PARAMETER: Cannot assign type %s as parameter of type %s";
    public static String INVALID_NB_PARAMETERS = "INVALID_NB_PARAMETERS: Invalid number of parameters for the call of method %s";

    // fields
    public static String VOID_FIELD_NOT_ALLOWED = "VOID_FIELD_NOT_ALLOWED: Field %s cannot be of type void";
    public static String ALREADY_DEFINED_FIELD = "ALREADY_DEFINED_FIELD: Field %s is already defined";
    public static String CANNOT_ACCESS_PROTECTED_FIELD = "CANNOT_ACCESS_PROTECTED_FIELD: The protected field %s cannot be accessed from class %s as %s doesn't extends %s";
    public static String CANNOT_ACCESS_PROTECTED_FIELD_FROM_MAIN = "CANNOT_ACCESS_PROTECTED_FIELD: The protected field %s cannot be accessed from main";
    public static String UNDEFINED_FIELD = "UNDEFINED_FIELD: Field with name %s doesn't exist";
    public static String INVALID_NAME_FIELD = "INVALID_NAME_FIELD: The field name cannot be the same as inherited method %s";
    
    //array
    public static String NON_INDEXABLE_VARIABLE = "NON_INDEXABLE_VARIABLE: Variable %s is not indexable";
    public static String INCOMPATIBLE_TYPE_WITH_INT = "INCOMPATIBLE_TYPE_WITH_INT: Cannot declare an array's size with type %s, the type need to be int";
    public static String INCOMPATIBLE_SIZE_OF_ARRAY = "INCOMPATIBLE_SIZE_IN_ARRAY: The first array's size is %s and an other array's size is %s";
    
}
