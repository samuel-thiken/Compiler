package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;

public class Selection extends AbstractLValue {
    private AbstractExpr targetObject;
    private AbstractIdentifier field;

    public Selection(AbstractExpr targetObject, AbstractIdentifier field) {
        Validate.notNull(targetObject);
        Validate.notNull(field);
        this.targetObject = targetObject;
        this.field = field;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {

        Type typeExpr = targetObject.verifyExpr(compiler, localEnv, currentClass);

        // checking if the type of the expression of the selection is a class type
        if (!typeExpr.isClass())
            throw new ContextualError(String.format(ContextualError.NOT_A_TYPE_CLASS, typeExpr.getName()),
                    getLocation());

        ClassDefinition classExpr = (ClassDefinition) compiler.environmentType.defOfType(typeExpr.getName());

        // checking if the field is defined in the environment of the class type of the
        // expression
        if (!classExpr.getMembers().has(field.getName()))
            throw new ContextualError(
                    String.format(ContextualError.UNDEFINED_FIELD, field.getName(), typeExpr.getName()), getLocation());

        // checking if identifier 'field' does correspond to a field
        Definition defSelector = classExpr.getMembers().get(field.getName());
        if (!defSelector.isField())
            throw new ContextualError(String.format(ContextualError.UNDEFINED_FIELD, field.getName()), getLocation());

        FieldDefinition fieldDef = (FieldDefinition) defSelector;
        if (fieldDef.getVisibility() == Visibility.PUBLIC) {
            field.setDefinition(fieldDef);
            setType(fieldDef.getType());
            return fieldDef.getType();
        }

        else {
            if (currentClass == null)
                throw new ContextualError(
                        String.format(ContextualError.CANNOT_ACCESS_PROTECTED_FIELD_FROM_MAIN, field.getName()),
                        getLocation());
            ClassType typeClassField = fieldDef.getContainingClass().getType();

            // condition 1: the field has to be defined in the current class or its
            // superClasses
            if (!currentClass.getType().isSubTypeOf(typeClassField))
                throw new ContextualError(String.format(ContextualError.CANNOT_ACCESS_PROTECTED_FIELD, field.getName(),
                        currentClass.getType().getName(), currentClass.getType().getName(),
                        typeClassField.getName()), getLocation());

            // condition 2: the type of the expr has to be a subtype of the current class
            if (!typeExpr.isSubTypeOf(currentClass.getType()))
                throw new ContextualError(String.format(ContextualError.CANNOT_ACCESS_PROTECTED_FIELD, field.getName(),
                        currentClass.getType().getName(), typeExpr.getName(),
                        currentClass.getType().getName()), getLocation());

            field.setDefinition(fieldDef);
            setType(fieldDef.getType());
            return fieldDef.getType();
        }

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(targetObject.decompile() + "." + field.decompile());
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        targetObject.prettyPrint(s, prefix, false);
        field.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        targetObject.iter(f);
        field.iter(f);
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        // MemoryLocation.Register objectAddress = targetObject.codeGenEvaluate(compiler).toRegister(compiler);

        // ExecutionError.check(compiler, objectAddress, Check.IS_NULL, getLocation());

        MemoryLocation.Address varAddress = MemoryLocation.Address.create(field, targetObject, field.getFieldDefinition().getIndex());
                // new RegisterOffset(field.getFieldDefinition().getIndex(), objectAddress.getRegister(compiler)));

        // objectAddress.close();

        return varAddress;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // Nothing to do
    }

}
