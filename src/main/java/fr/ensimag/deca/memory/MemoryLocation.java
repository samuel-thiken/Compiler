package fr.ensimag.deca.memory;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.AbstractLiteral;
import fr.ensimag.deca.tree.ArraySelector;
import fr.ensimag.deca.tree.Identifier;
import fr.ensimag.deca.tree.This;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public abstract class MemoryLocation implements AutoCloseable {

    private AbstractExpr expression;
    private MemoryManager memoryManager;

    public MemoryLocation(MemoryManager memoryManager, AbstractExpr expression) {
        this.memoryManager = memoryManager;
        this.expression = expression;
    }

    protected AbstractExpr getExpression() {
        return expression;
    }

    protected MemoryManager getMemoryManager() {
        return memoryManager;
    }

    protected void setExpression(AbstractExpr expression) {
        this.expression = expression;
    }

    public abstract void close();

    public abstract MemoryLocation.Register toRegister(DecacCompiler compiler);

    public abstract MemoryLocation.Register toRegister(DecacCompiler compiler, int registerIndex);

    public abstract DVal getDVal(DecacCompiler compiler);

    public abstract boolean isDirect();

    public abstract MemoryLocation.Address getAddress();

    protected abstract MemoryLocation getBase();

    public MemoryLocation use() {
        return this;
    }

    public static class Register extends MemoryLocation {

        private Integer registerIndex;
        private MemoryLocation references = null;
        private boolean discarded = false;

        public boolean isDiscarded() {
            return discarded;
        }

        private int usedBy = 0;

        @Override
        public MemoryLocation.Register use() {
            usedBy++;
            discarded = false;
            getMemoryManager().notifyUse(registerIndex);
            return this;
        }

        public static Register create(DecacCompiler compiler) {
            return new Register(null, compiler, null, null);
        }

        public static Register create(DecacCompiler compiler, int registerIndex) {
            return new Register(null, compiler, null, registerIndex);
        }

        public static MemoryLocation[] getUnused(DecacCompiler compiler, MemoryLocation memLeft,
                MemoryLocation memRight) {
            return getUnused(compiler, memLeft, memRight, false);
        }

        public static MemoryLocation[] getUnused(DecacCompiler compiler, MemoryLocation memLeft,
                MemoryLocation memRight, boolean keepOrder) {
            MemoryLocation[] result = new MemoryLocation[2];

            if (memLeft != null && !memLeft.isDirect() && !memLeft.getBase().isDirect()) {
                result[0] = memLeft.toRegister(compiler).use();
                result[1] = memRight;
            } else if (!keepOrder && memRight != null && !memRight.isDirect() && !memRight.getBase().isDirect()) {
                result[0] = memRight.toRegister(compiler).use();
                result[1] = memLeft;
            } else {
                // On regarde si on a récupéré le registre de memLeft
                if (memLeft instanceof Register) {
                    Register memLeftRegister = (Register) memLeft;
                    Integer leftRegisterIndex = memLeftRegister.registerIndex;
                    memLeftRegister.close();

                    Register register = create(compiler);
                    result[0] = register;
                    result[1] = memRight;

                    if (register.registerIndex != leftRegisterIndex) {
                        DVal dval = memLeft.getBase().getDVal(compiler);
                        compiler.addInstruction(new LOAD(dval, ((Register) result[0]).getRegister(compiler)));
                    }

                    memLeftRegister.use();
                } else {
                    memLeft.close();
                    result[0] = create(compiler);
                    result[1] = memRight;
                    DVal dval = memLeft.getBase().getDVal(compiler);
                    compiler.addInstruction(new LOAD(dval, ((Register) result[0]).getRegister(compiler)));
                }
            }
            compiler.getMemoryManager().notifyUse(((Register) result[0]).registerIndex);

            return result;
        }

        public MemoryLocation.Register withNotify() {
            getMemoryManager().notifyUse(registerIndex);
            return this;
        }

        protected Register(AbstractExpr expr, DecacCompiler compiler, MemoryLocation memoryLocation,
                Integer registerIndex) {
            super(compiler.getMemoryManager(), expr);
            references = memoryLocation;
            // Réserver un registre
            if (registerIndex == null)
                compiler.getMemoryManager().getRegister(compiler, this);
            else
                compiler.getMemoryManager().requestRegister(compiler, this, registerIndex);
            // Charger la valeur dans le registre
            if (references != null) {
                if (references.getExpression() != null && references.getExpression().getType().isArray() && references.getExpression() instanceof ArraySelector)
                    compiler.addInstruction(new LEA(references.getAddress().getRegisterOffset(compiler),
                            fr.ensimag.ima.pseudocode.Register.getR(this.registerIndex)));
                else
                    compiler.addInstruction(new LOAD(references.getDVal(compiler),
                            fr.ensimag.ima.pseudocode.Register.getR(this.registerIndex)));
            }
            use();
        }

        public void overwrite() {
            MemoryLocation base = getBase();
            if (getExpression() != null)
                getExpression().setMemoryLocation(base);
        }

        public void discarded() {
            if (usedBy > 0) {
                throw new RuntimeException("Un registre a été jeté alors qu'il était encore en utilisant");
            }
            this.discarded = true;
        }

        public boolean needPush() {
            if (discarded)
                return false;
            return true;
        }

        public void changeReference(MemoryLocation memoryLocation) {
            // Unlink previous reference
            overwrite();
            // Link new reference
            memoryLocation = memoryLocation.getBase();
            this.references = memoryLocation;
            setExpression(memoryLocation.expression);
            getExpression().setMemoryLocation(this);
        }

        @Override
        public MemoryLocation getBase() {
            return references != null ? references.getBase() : this;
        }

        public void setRegisterIndex(Integer registerIndex) {
            this.registerIndex = registerIndex;
        }

        public int getRegisterIndex(DecacCompiler compiler) {
            if (registerIndex == null) {
                // On doit recharger la valeur
                throw new RuntimeException("Une valeur registre n'a pas de registre attribué");
            }
            // On notifie l'utilisation (LRU)
            compiler.getMemoryManager().notifyUse(registerIndex);

            return registerIndex;
        }

        public fr.ensimag.ima.pseudocode.GPRegister getRegister(DecacCompiler compiler) {
            int registerIndex = this.getRegisterIndex(compiler);
            return fr.ensimag.ima.pseudocode.Register.getR(registerIndex);
        }

        @Override
        public MemoryLocation.Register toRegister(DecacCompiler compiler) {
            // C'est déjà un registre, on ne fait rien
            return this;
        }

        @Override
        public MemoryLocation.Register toRegister(DecacCompiler compiler, int registerIndex) {
            // On doit changer le registre si ce n'est pas le bon
            if (this.registerIndex != registerIndex) {
                if (references != null) {
                    close();
                    return references.toRegister(compiler, registerIndex);
                } else
                    return new Register(getExpression(), compiler, this, registerIndex);
            } else {
                // use();
                return this;
            }
        }

        @Override
        public void close() {
            usedBy--;
            if (usedBy == 0) {
                getMemoryManager().discard(this, registerIndex);
                if (references != null && !references.isDirect())
                    references.close();
            } else if (usedBy < 0) {
                // System.out.println("Register closed more time than used");
                // throw new RuntimeException("Register closed more time than used");
            } else {
                getMemoryManager().notifyUse(registerIndex);
            }
        }

        @Override
        public DVal getDVal(DecacCompiler compiler) {
            if (registerIndex == null) {
                if (references != null)
                    return references.getDVal(compiler);
                else
                    throw new RuntimeException("Register error");
            }
            getMemoryManager().notifyUse(registerIndex);
            return fr.ensimag.ima.pseudocode.Register.getR(registerIndex);
        }

        @Override
        public boolean isDirect() {
            return false;
        }

        @Override
        public Address getAddress() {
            if (references != null && references instanceof Address) {
                return (Address) references;
            }
            return null;
        }

        @Override
        public String toString() {
            if (references != null)
                return references.toString();
            return String.format("Register");
        }

    }

    public static class Address extends MemoryLocation {

        private RegisterOffset registerOffset;
        private AbstractExpr dependsOn;
        private Integer offset;
        private Register offsetR;

        public static Address create(AbstractExpr expr, RegisterOffset registerOffset) {
            return new Address(expr, registerOffset);
        }

        public static Address create(AbstractExpr expr, AbstractExpr dependsOn, Integer offset) {
            return new Address(expr, dependsOn, offset);
        }

        public static Address create(AbstractExpr expr, AbstractExpr dependsOn, Register offsetR) {
            return new Address(expr, dependsOn, offsetR);
        }

        protected Address(AbstractExpr expr, AbstractExpr dependsOn, Register offsetR) {
            super(null, expr);
            this.registerOffset = null;
            this.dependsOn = dependsOn;
            this.offset = 0;
            this.offsetR = offsetR;
        }

        protected Address(AbstractExpr expr, AbstractExpr dependsOn, Integer offset) {
            super(null, expr);
            this.registerOffset = null;
            this.dependsOn = dependsOn;
            this.offset = offset;
        }

        protected Address(AbstractExpr expr, RegisterOffset registerOffset) {
            super(null, expr);
            this.registerOffset = registerOffset;
            this.dependsOn = null;
        }

        private MemoryLocation.Register dependsOnRegister = null;

        public RegisterOffset getRegisterOffset(DecacCompiler compiler) {
            if (registerOffset == null) {
                // TODO : version qui était dans la branche main
                // return new RegisterOffset(offset, dependsOnRegister.getRegister(compiler),
                //         offsetR != null ? offsetR.getRegister(compiler) : null);
                dependsOnRegister = dependsOn.codeGenEvaluate(compiler).toRegister(compiler);
                ExecutionError.check(compiler, dependsOnRegister, Check.IS_NULL, getExpression().getLocation());
                return new RegisterOffset(offset, dependsOnRegister.getRegister(compiler));
            } else {
                return registerOffset;
            }
        }

        @Override
        public MemoryLocation.Register toRegister(DecacCompiler compiler) {
            MemoryLocation.Register mem = new MemoryLocation.Register(getExpression(), compiler, this, null);
            getExpression().setMemoryLocation(mem);
            close();
            return mem;
        }

        @Override
        public MemoryLocation.Register toRegister(DecacCompiler compiler, int registerIndex) {
            MemoryLocation.Register mem = new MemoryLocation.Register(getExpression(), compiler, this, registerIndex);
            getExpression().setMemoryLocation(mem);
            close();
            return mem;
        }

        @Override
        public MemoryLocation getBase() {
            return this;
        }

        @Override
        public void close() {
            if (dependsOn != null && dependsOnRegister != null) {
                dependsOnRegister.close();
                dependsOnRegister = null;
            } else {
                // On n'utilise pas de registre, on ne fait rien
            }
        }

        @Override
        public DVal getDVal(DecacCompiler compiler) {
            return getRegisterOffset(compiler);
        }

        @Override
        public boolean isDirect() {
            return true;
        }

        @Override
        public Address getAddress() {
            return this;
        }

        @Override
        public String toString() {
            if (getExpression() instanceof This)
                return "Variable this";
            if (getExpression() instanceof Identifier)
                return String.format("Variable %s", ((Identifier) getExpression()).getName().getName());
            return "Variable";
        }

    }

    public static class Immediate extends MemoryLocation {

        private DVal dval;

        public static Immediate create(AbstractExpr expr, DVal dval) {
            return new Immediate(expr, dval);
        }

        protected Immediate(AbstractExpr expr, DVal dval) {
            super(null, expr);
            this.dval = dval;
        }

        @Override
        public MemoryLocation.Register toRegister(DecacCompiler compiler) {
            MemoryLocation.Register mem = new MemoryLocation.Register(getExpression(), compiler, this, null);
            if (getExpression() != null)
                getExpression().setMemoryLocation(mem);
            return mem;
        }

        @Override
        public MemoryLocation.Register toRegister(DecacCompiler compiler, int registerIndex) {
            MemoryLocation.Register mem = new MemoryLocation.Register(getExpression(), compiler, this, registerIndex);
            getExpression().setMemoryLocation(mem);
            return mem;
        }

        @Override
        public MemoryLocation getBase() {
            return this;
        }

        @Override
        public void close() {
            // On n'utilise pas de registre, on ne fait rien
        }

        @Override
        public DVal getDVal(DecacCompiler compiler) {
            return dval;
        }

        @Override
        public boolean isDirect() {
            return true;
        }

        @Override
        public Address getAddress() {
            return null;
        }

        @Override
        public String toString() {
            return String.format("Literal %s", ((AbstractLiteral) getExpression()).getStringRepresentation());
        }

    }

}
