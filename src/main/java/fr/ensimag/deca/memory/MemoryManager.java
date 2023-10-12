package fr.ensimag.deca.memory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

public class MemoryManager {

    private final static boolean DISABLE_FIRST_REGISTERS = true;

    private int[] registerLRUstorage;
    private Map<Integer, MemoryLocation.Register> currentlyUsedBy = new HashMap<>();
    private LinkedList<MemoryLocation.Register> pushedValues = new LinkedList<>();
    private Map<SymbolTable.Symbol, AbstractExpr> varsExpressions = new HashMap<>();

    private DecacCompiler compiler;

    private static class Save {
        public Map<Integer, MemoryLocation.Register> currentlyUsedBy = new HashMap<>();
        public LinkedList<MemoryLocation.Register> pushedValues = new LinkedList<>();
    }

    private LinkedList<Save> saves = new LinkedList<>();

    public void save() {
        Save save = new Save();
        save.pushedValues = pushedValues;
        save.currentlyUsedBy = new HashMap<>();
        saves.addFirst(save);

        for (Entry<Integer, MemoryLocation.Register> entry : currentlyUsedBy.entrySet()) {
            if (entry.getValue().needPush()) {
                currentPush++;
                checkMaxStackSize();
                compiler.addInstruction(new PUSH(Register.getR(entry.getKey())));
                save.currentlyUsedBy.put(entry.getKey(), entry.getValue());
            } else {
                entry.getValue().overwrite();
            }
        }

        currentlyUsedBy = new HashMap<>();
        pushedValues = new LinkedList<>();
    }

    public void restore(List<Integer> rebind) {
        if (saves.size() == 0)
            return;

        invalidateAll();

        Save save = saves.removeFirst();
        currentlyUsedBy = new HashMap<>();
        pushedValues = save.pushedValues;

        for (Entry<Integer, MemoryLocation.Register> entry : save.currentlyUsedBy.entrySet()) {
            currentPush--;
            AbstractExpr expr = entry.getValue().getExpression();
            if (expr != null)
            expr.setMemoryLocation(entry.getValue());
            if (rebind != null && rebind.contains(entry.getKey())) {
                Integer unusedReg = findFirstAvailableRegister(false);
                entry.getValue().setRegisterIndex(unusedReg);
                currentlyUsedBy.remove(entry.getKey());
                currentlyUsedBy.put(unusedReg, entry.getValue());
                compiler.addInstruction(new POP(Register.getR(unusedReg)));
            } else {
                currentlyUsedBy.put(entry.getKey(), entry.getValue());
                compiler.addInstruction(new POP(Register.getR(entry.getKey())));
            }
        }
    }

    private int isInConditionalBlock = 0;
    private List<MemoryLocation.Register> registersInConditionalBlock = new LinkedList<>();

    public void startConditionalBlock() {
        isInConditionalBlock++;
    }

    public void endConditionalBlock() {
        isInConditionalBlock--;
        for (MemoryLocation.Register register : registersInConditionalBlock) {
            register.overwrite();
        }
        registersInConditionalBlock = new LinkedList<>();
    }

    public void flush() {
        currentlyUsedBy = new HashMap<>();
        pushedValues = new LinkedList<>();
        Map<Symbol, AbstractExpr> newExpr = new HashMap<>();
        for (Entry<Symbol, AbstractExpr> entry : varsExpressions.entrySet()) {
            if (entry.getValue().getType() != null && entry.getValue().getType().isClass()) {
                if (entry.getValue() == ((ClassType) entry.getValue().getType()).getDefinition().getExpression())
                    newExpr.put(entry.getKey(), entry.getValue());
            }
        }
        varsExpressions = newExpr;
    }

    public void setVarExpression(SymbolTable.Symbol s, AbstractExpr expr) {
        varsExpressions.put(s, expr);
    }

    public AbstractExpr getVarExpression(SymbolTable.Symbol s) {
        return varsExpressions.getOrDefault(s, null);
    }

    public MemoryManager(DecacCompiler compiler, int registerCount) {
        this.compiler = compiler;

        registerLRUstorage = new int[registerCount];
        for (int i = 0; i < registerCount; i++)
            registerLRUstorage[i] = i;
    }

    public void invalidateAll() {
        for (MemoryLocation.Register register : currentlyUsedBy.values()) {
            register.overwrite();
        }
        currentlyUsedBy.clear();
    }

    private int nextStackAddress = 1;

    public int getNewStackAddress() {
        return nextStackAddress++;
    }
    private boolean isInMethod = false;

    public void enterMethod() {
        isInMethod = true;
        nextStackAddress = 1;
    }

    public RegisterOffset getNewStackRegisterOffset() {
        if (isInMethod) {
            compiler.addInstruction(new ADDSP(new ImmediateInteger(1)));
            return new RegisterOffset(getNewStackAddress(), Register.LB);
        } 
        return new RegisterOffset(getNewStackAddress(), Register.GB);
    }

    private int maxPush = 0;
    private int currentPush = 0;

    private void checkMaxStackSize() {
        if (currentPush > maxPush)
            maxPush = currentPush;
    }

    public int getVariableCount() {
        return nextStackAddress - 1;
    }

    public int getMaxStackSize() {
        return getVariableCount() + maxPush;
    }

    public void requestRegister(DecacCompiler compiler, MemoryLocation.Register reg, int registerIndex) {
        MemoryLocation.Register registerInUse = currentlyUsedBy.getOrDefault(registerIndex, null);
        if (registerInUse != null) {
            Integer moveRegister = findFirstAvailableRegister(false);
            if (moveRegister != null) {
                // On peut simplement déplacer la valeur de registre
                registerInUse.toRegister(compiler, moveRegister);
            } else {
                // Renvoie false si la valeur ne doit pas être enregistrée
                if (registerInUse.needPush()) {
                    // On met le registre sur la pile
                    pushedValues.addFirst(registerInUse);
                    compiler.addComment(String.format("PUSH de %s", registerInUse));
                    registerInUse.setRegisterIndex(null);
                    currentPush++;
                    checkMaxStackSize();
                    compiler.addInstruction(new PUSH(Register.getR(registerIndex)));
                }
            }
            registerInUse.overwrite();
        }
        if (isInConditionalBlock > 0)
            registersInConditionalBlock.add(reg);
        reg.setRegisterIndex(registerIndex);
        currentlyUsedBy.put(registerIndex, reg);
        notifyUse(registerIndex);
    }

    public void getRegister(DecacCompiler compiler, MemoryLocation.Register reg) {
        int lruReg = findFirstAvailableRegister(true);
        if (currentlyUsedBy.getOrDefault(lruReg, null) == null) {
            // Le registre est vide, on retourne directement
            reg.setRegisterIndex(lruReg);
            currentlyUsedBy.put(lruReg, reg);
            if (isInConditionalBlock > 0)
                registersInConditionalBlock.add(reg);
        } else {
            // On doit mettre la valeur dans la pile
            requestRegister(compiler, reg, lruReg);
        }
        notifyUse(lruReg);
    }

    private Integer findFirstAvailableRegister(boolean withPush) {
        int registerIndex;
        MemoryLocation.Register registerInUse;
        for (int i = 0; i < registerLRUstorage.length; i++) {
            registerIndex = registerLRUstorage[i];
            if (DISABLE_FIRST_REGISTERS && registerIndex < 2)
                continue;
            registerInUse = currentlyUsedBy.getOrDefault(registerIndex, null);
            if (registerInUse != null && registerInUse.needPush())
                continue;
            else
                return registerIndex;
        }
        if (!withPush) return null;
        // No free register found : push required
        for (int i = 0; i < registerLRUstorage.length; i++) {
            registerIndex = registerLRUstorage[i];
            if (DISABLE_FIRST_REGISTERS && registerIndex < 2)
                continue;
            return registerIndex;
        }
        throw new RuntimeException("No register found");
    }

    public void discard(MemoryLocation.Register registerInUse, Integer registerIndex) {
        // MemoryLocation.Register registerInUse =
        // currentlyUsedBy.getOrDefault(registerIndex, null);
        registerInUse.discarded();
        // On recharge la dernière valeur PUSHed
        if (registerIndex != null && pushedValues.size() > 0) {
            registerInUse.overwrite();
            MemoryLocation.Register mRegister;
            do {
                mRegister = pushedValues.removeFirst();
                compiler.addComment(String.format("POP de %s", mRegister));
                currentPush--;
                compiler.addInstruction(new POP(Register.getR(registerIndex)));
                mRegister.setRegisterIndex(registerIndex);
                currentlyUsedBy.put(registerIndex, mRegister);
            } while (pushedValues.size() > 0 && mRegister.isDiscarded());
        }
    }

    public void notifyUse(Integer registerIndex) {
        if (registerIndex == null)
            return;
        boolean found = false;
        for (int i = 0; i < registerLRUstorage.length; i++) {
            if (registerLRUstorage[i] == registerIndex)
                found = true;
            if (found && i < registerLRUstorage.length - 1)
                registerLRUstorage[i] = registerLRUstorage[i + 1];
        }
        registerLRUstorage[registerLRUstorage.length - 1] = registerIndex;
    }

}
