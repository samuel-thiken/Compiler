package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Declaration of a class's field (which visibility can be protected or public)
 * 
 * @author H
 */
public class DeclField extends AbstractDeclField {
    final private Visibility visibility;
    final private AbstractIdentifier type;
    final private AbstractIdentifier fieldName;
    final private AbstractInitialization initialization;

    public DeclField(Visibility visibility, AbstractIdentifier type, AbstractIdentifier fieldName,
            AbstractInitialization initialization) {
        Validate.notNull(visibility);
        Validate.notNull(type);
        Validate.notNull(fieldName);
        Validate.notNull(initialization);
        this.visibility = visibility;
        this.type = type;
        this.fieldName = fieldName;
        this.initialization = initialization;
    }

    public AbstractIdentifier getFieldName() {
        return fieldName;
    }

    @Override
    String prettyPrintNode() {
        return String.format("[visibility = %s] DeclField", visibility);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (visibility == Visibility.PROTECTED)
            s.print(visibility.name().toLowerCase() + " " + type.decompile() + " " + fieldName.decompile());
        else
            s.print(type.decompile() + " " + fieldName.decompile());

        if (initialization instanceof Initialization) {
            s.print(" = " + initialization.decompile());
        }
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
    
    @Override
    protected void verifyDeclFieldInit(DecacCompiler compiler, EnvironmentExp envClass,
            AbstractIdentifier currentClass, AbstractIdentifier superClass) throws ContextualError {
        initialization.verifyInitialization(compiler, type.getType(), envClass, currentClass.getClassDefinition());
        
    }

    @Override
    protected void verifyDeclField(DecacCompiler compiler, EnvironmentExp envClass,
            AbstractIdentifier className, AbstractIdentifier superClass) throws ContextualError {
        type.verifyType(compiler, null, null);

        // verify if the field's type isn't void type
        if (type.getType().isVoid())
            throw new ContextualError(String.format(ContextualError.VOID_FIELD_NOT_ALLOWED, fieldName.getName()),
                    getLocation());

        // get the superclass' environment
        ClassDefinition superClassDef = (ClassDefinition) compiler.environmentType.defOfType(superClass.getName());
        EnvironmentExp envExpSuper = superClassDef.getMembers();

        // cannot declare a field with the same name as method inherited
        if (envExpSuper.has(fieldName.getName()) && !envExpSuper.get(fieldName.getName()).isField())
            throw new ContextualError(String.format(ContextualError.INVALID_NAME_FIELD, fieldName.getName()),
                    fieldName.getLocation());

        // new definition in current class
        int indexField = superClassDef.getTotalNumberOfFields() + className.getClassDefinition().incNumberOfFields();
        FieldDefinition fieldDef = new FieldDefinition(type.getType(), fieldName.getLocation(), visibility,
                className.getClassDefinition(), indexField);

        // for assembly
        fieldDef.setExpression(fieldName);

        // setting definitions & type of the identifier of the method (methodName)
        fieldName.setDefinition(fieldDef);
        fieldName.setType(type.getType());

        try {
            envClass.declare(fieldName.getName(), fieldDef);
        } catch (DoubleDefException e) {
            throw new ContextualError(String.format(ContextualError.ALREADY_DEFINED_FIELD, fieldName.getName()),
                    fieldName.getLocation());
        }
    }

    @Override
    protected void codeGenDeclField(DecacCompiler compiler, boolean disableStore) {
        int fieldIndex = fieldName.getFieldDefinition().getIndex();
        compiler.addComment(String.format("Initialisation du champ %s", getFieldName().getName()));
        MemoryLocation.Address addr = MemoryLocation.Address.create(fieldName,
                new RegisterOffset(fieldIndex, Register.R1));
        MemoryLocation.Register m = initialization.codeGenInitialization(compiler, addr, 0, disableStore);
        if (m != null) {
            fieldName.setMemoryLocation(m);
            // m.close();
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        fieldName.iter(f);
        initialization.iter(f);
    }

    

}
