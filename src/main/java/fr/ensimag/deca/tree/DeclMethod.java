package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

/**
 * Declaration of a method.
 * 
 * @author H
 */
public class DeclMethod extends AbstractDeclMethod {

    final private AbstractIdentifier type;
    final private AbstractIdentifier methodName;
    final private ListDeclParam params;
    final private AbstractMethodBody methodBody;

    public DeclMethod(AbstractIdentifier type, AbstractIdentifier methodName, ListDeclParam params,
            AbstractMethodBody methodBody) {
        Validate.notNull(type);
        Validate.notNull(methodName);
        this.type = type;
        this.methodName = methodName;
        this.params = params;
        this.methodBody = methodBody;
    }

    public AbstractIdentifier getMethodName() {
        return methodName;
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        methodBody.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        methodName.iter(f);
        params.iter(f);
        methodBody.iter(f);
    }

    @Override
    protected void verifyDeclMethodBody(DecacCompiler compiler, EnvironmentExp envClass,
            AbstractIdentifier currentClass, AbstractIdentifier superClass) throws ContextualError {

        MethodDefinition methodDef = methodName.getMethodDefinition();
        methodBody.verifyMethodBody(compiler, envClass, methodDef.getEnvParams(), currentClass.getClassDefinition(),
                type.getType());
    }

    @Override
    protected void verifyDeclMethod(DecacCompiler compiler, EnvironmentExp envClass, AbstractIdentifier currentClass,
            AbstractIdentifier superClass)
            throws ContextualError {

        type.verifyType(compiler, null, null);

        // get the superclass' environment
        ClassDefinition superClassDef = superClass != null
                ? ((ClassDefinition) compiler.environmentType.defOfType(superClass.getName()))
                : null;
        EnvironmentExp envExpSuper = superClass != null ? superClassDef.getMembers() : null;

        // getting the signature of the method & creating the environment of the
        // parameters of the method
        EnvironmentExp envParams = new EnvironmentExp(null); // PASS 3
        Signature sigInClass = params.verifyListDeclParam(compiler, envParams);

        // verify if envExpSuper(fieldName) is defined
        boolean isOverrideMethodCorrect = false;

        if (envExpSuper != null && envExpSuper.has(methodName.getName())) { // the current method name exists in the
                                                                            // super environment
            MethodDefinition methodDefSuper = (MethodDefinition) envExpSuper.get(methodName.getName());

            Type typeInSuperClass = methodDefSuper.getType();
            Type typeInCurrentClass = type.getType();

            // checking if the type of return is a subtype of the inherited method
            boolean returnSubType = typeInCurrentClass.isSubTypeOf(typeInSuperClass);

            if (!returnSubType)
                throw new ContextualError(String.format(ContextualError.NOT_A_SUBTYPE_RETURN, methodName.getName(),
                        currentClass.getName(), methodDefSuper.getType().getName()), getLocation());

            // checking if the signature is the same as the inherited method
            boolean isCorrectSig = sigInClass.equals(methodDefSuper.getSignature());
            if (!isCorrectSig)
                throw new ContextualError(String.format(ContextualError.INVALID_METHOD_SIGNATURE_OVERRIDE,
                        methodName.getName(), currentClass.getName()), getLocation());

            isOverrideMethodCorrect = isCorrectSig && returnSubType;
        }
        try {
            MethodDefinition methodDef;
            if (!isOverrideMethodCorrect) { // new method definition (not an override)
                int indexMethod = (superClassDef != null ? superClassDef.getTotalNumberOfMethods() : 0) + currentClass.getClassDefinition().incNumberOfMethods();
                methodDef = new MethodDefinition(type.getType(), type.getLocation(), sigInClass, indexMethod);
            } else {
                MethodDefinition methodDefInSuperclass = (MethodDefinition) envExpSuper.get(methodName.getName());
                methodDef = new MethodDefinition(type.getType(), methodDefInSuperclass.getLocation(), sigInClass,
                        methodDefInSuperclass.getIndex());
            }
            methodDef.setEnvParams(envParams);
            envClass.declare(methodName.getName(), methodDef);

            // setting definitions & type of the identifier of the method (methodName)
            methodName.setDefinition(methodDef);
            methodName.setType(type.getType());
        } catch (DoubleDefException e) {
            throw new ContextualError(String.format(ContextualError.INVALID_METHOD_NAME, methodName.getName()),
                    getLocation());
        }
    }

    @Override
    protected void codeGenDeclMethod(DecacCompiler compiler, ClassDefinition currentClass) {
        compiler.getMemoryManager().flush();
        compiler.getMemoryManager().enterMethod();

        Label label = new Label(String.format("code.%s.%s", currentClass.getType().getName(), methodName.getName()));
        // methodName.getMethodDefinition().setLabel(label);
        compiler.addLabel(label);

        // Environnement des classes
        

        // Adresse de l'object (this)
        This thisObj = new This(false);
        MemoryLocation objectAddr = MemoryLocation.Address.create(thisObj, new RegisterOffset(-2, Register.LB))
                .toRegister(compiler, 1);
        thisObj.setMemoryLocation(objectAddr);
        compiler.getMemoryManager().setVarExpression(compiler.createSymbol("this"), thisObj);

        // Adresses des paramètres
        int paramAddr = -3;
        for (AbstractDeclParam param : params.getList()) {
            param.getParamName().setMemoryLocation(
                    MemoryLocation.Address.create(param.getParamName(), new RegisterOffset(paramAddr--, Register.LB)));
            compiler.getMemoryManager().setVarExpression(param.getParamName().getName(), param.getParamName());
        }
        // Adresses des attributs
        Set<Entry<Symbol, ExpDefinition>> members = currentClass.getAllMembers().getDefinitions();
        for (Entry<Symbol, ExpDefinition> entry : members) {
            if (entry.getValue().isField()) {
                FieldDefinition fieldDef = (FieldDefinition) entry.getValue();
                AbstractExpr fieldExpr = fieldDef.getExpression();
                fieldExpr.setMemoryLocation(MemoryLocation.Address.create(fieldExpr, thisObj, fieldDef.getIndex()));
                Symbol s = compiler.symbolTable.create(String.format("this.%s", entry.getKey()));
                compiler.getMemoryManager().setVarExpression(s, fieldExpr);
            }
        }

        objectAddr.close();

        // Test débordement de la pile

        // Sauvegarde des registres
        // compiler.getMemoryManager().save();

        // Code
        // compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB),
        // Register.R1));
        methodBody.codeGenMethodBody(compiler);

        if (!methodName.getType().sameType(compiler.environmentType.VOID)) {
            compiler.addLabel(new Label(
                    String.format("fin_no_return.%s.%s", currentClass.getType().getName(), methodName.getName())));
            compiler.addInstruction(new WSTR(String.format("%s: Erreur : Sortie de la méthode %s.%s sans return",
                    getLocation().toPrettyString(), currentClass.getType().getName(), methodName.getName())));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        compiler.addLabel(
                new Label(String.format("fin.%s.%s", currentClass.getType().getName(), methodName.getName())));

        // Restoration des registres
        // compiler.getMemoryManager().restore();
        // Return
        compiler.addInstruction(new RTS());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
        methodBody.decompile(s);
    }

}
