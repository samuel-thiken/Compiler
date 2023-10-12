package fr.ensimag.deca.codegen;

import java.util.function.BiConsumer;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BLT;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

public class ExecutionError extends LocationException {

    private Check check;
    private static int errorLabelIncrement = 0;

    public ExecutionError(Check check, Location location) {
        super(check.message, location);
        this.check = check;
    }

    public static String check(DecacCompiler compiler, MemoryLocation element, Check check, Location location) {
        if (compiler.getCompilerOptions().shouldErrorCheck()) {
            return new ExecutionError(check, location).check(compiler, element);
        }
        return null;
    }

    private String check(DecacCompiler compiler, MemoryLocation element) {
        MemoryLocation.Register register = null;
        if (element != null) {
            register = element.toRegister(compiler);
            if ((element instanceof MemoryLocation.Register))
                register.use();
        }

        String errorLabel = String.format("error_%d_%s_%d_%d", errorLabelIncrement++, check.name().toLowerCase(),
                location.getLine(),
                location.getPositionInLine());
        String continueLabel = String.format("continue_%d_%s_%d_%d", errorLabelIncrement++, check.name().toLowerCase(),
                location.getLine(),
                location.getPositionInLine());

        // Test
        check.test.accept(compiler, new CheckParameters(register, errorLabel));
        // Continue
        compiler.addInstruction(new BRA(new Label(continueLabel)));
        // Error handling
        compiler.addLabel(new Label(errorLabel));
        compiler.addInstruction(new WSTR(this.getMessageWithLocation()));
        compiler.addInstruction(new ERROR());
        // Jump label
        compiler.addLabel(new Label(continueLabel));

        if (register != null)
            register.close();

        return errorLabel;
    }

    public static enum Check {
        DIVISION_INT_BY_ZERO("Division by zero", (DecacCompiler compiler, CheckParameters params) -> {
            MemoryLocation zero = MemoryLocation.Immediate.create(null, new ImmediateInteger(0));
            compiler.addInstruction(new CMP(zero.getDVal(compiler), params.register.getRegister(compiler)));
            compiler.addInstruction(new BEQ(new Label(params.label)));
        }),
        DIVISION_FLOAT_BY_ZERO("Division by zero", (DecacCompiler compiler, CheckParameters params) -> {
            MemoryLocation zero = MemoryLocation.Immediate.create(null, new ImmediateFloat(0));
            compiler.addInstruction(new CMP(zero.getDVal(compiler), params.register.getRegister(compiler)));
            compiler.addInstruction(new BEQ(new Label(params.label)));
        }),
        MODULO_BY_ZERO("Modulo by zero", (DecacCompiler compiler, CheckParameters params) -> {
            MemoryLocation zero = MemoryLocation.Immediate.create(null, new ImmediateInteger(0));
            compiler.addInstruction(new CMP(zero.getDVal(compiler), params.register.getRegister(compiler)));
            compiler.addInstruction(new BEQ(new Label(params.label)));
        }),
        OVERFLOW("Number overflow", (DecacCompiler compiler, CheckParameters params) -> {
            compiler.addInstruction(new BOV(new Label(params.label)));
        }),
        STACK_OVERFLOW("Stack overflow", (DecacCompiler compiler, CheckParameters params) -> {
            compiler.addInstruction(new BOV(new Label(params.label)));
        }),
        INVALID_INPUT("Invalid input", (DecacCompiler compiler, CheckParameters params) -> {
            compiler.addInstruction(new BOV(new Label(params.label)));
        }),
        IS_NULL("Dereferencement null", (DecacCompiler compiler, CheckParameters params) -> {
            compiler.addInstruction(new CMP(new NullOperand(), params.register.getRegister(compiler)));
            compiler.addInstruction(new BEQ(new Label(params.label)));
        }),
        NEGATIVE_SIZE_ARRAY("Invalid non positive for array declaration", (DecacCompiler compiler, CheckParameters params) -> {
            MemoryLocation zero = MemoryLocation.Immediate.create(null, new ImmediateInteger(0));
            compiler.addInstruction(new CMP(zero.getDVal(compiler), params.register.getRegister(compiler)));
            compiler.addInstruction(new BLT(new Label(params.label)));
        }),
        INDEX_OUT_OF_BOUNDS_GREATER("Index out of bounds (index > max index)", (DecacCompiler compiler, CheckParameters params) -> {
            MemoryLocation zero = MemoryLocation.Immediate.create(null, new ImmediateInteger(0));
            compiler.addInstruction(new CMP(zero.getDVal(compiler), params.register.getRegister(compiler)));
            compiler.addInstruction(new BLT(new Label(params.label)));
        }),
        INDEX_OUT_OF_BOUNDS_NEGATIVE("Index out of bounds (index < zero)", (DecacCompiler compiler, CheckParameters params) -> {
            MemoryLocation zero = MemoryLocation.Immediate.create(null, new ImmediateInteger(0));
            compiler.addInstruction(new CMP(zero.getDVal(compiler), params.register.getRegister(compiler)));
            compiler.addInstruction(new BLT(new Label(params.label)));
        }),
        INCOMPATIBLE_ARRAY_ASSIGNATION("Incompatible array assignation", (DecacCompiler compiler, CheckParameters params) -> {
            MemoryLocation zero = MemoryLocation.Immediate.create(null, new ImmediateInteger(0));
            compiler.addInstruction(new CMP(zero.getDVal(compiler), params.register.getRegister(compiler)));
            compiler.addInstruction(new BNE(new Label(params.label)));
        });

        public String message;
        public BiConsumer<DecacCompiler, CheckParameters> test;

        private Check(String message, BiConsumer<DecacCompiler, CheckParameters> test) {
            this.message = message;
            this.test = test;
        }
    }

    private static class CheckParameters {
        public MemoryLocation.Register register;
        public String label;

        public CheckParameters(MemoryLocation.Register register, String label) {
            this.register = register;
            this.label = label;
        }
    }

    public void throwError(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR(this.getLocalizedMessage()));
        compiler.addInstruction(new HALT());
    }
    
    @Override
    public String getMessageWithLocation() {
        Location loc = getLocation();
        String line;
        String column;
        if (loc == null) {
            line = "<unknown>";
            column = "";
        } else {
            line = Integer.toString(loc.getLine());
            column = ":" + loc.getPositionInLine();
        }
        return location.getFilename() + ":" + line + column + ": " + getMessage();
    }

    public static String DIVISION_BY_ZERO = "Division by zero";

}
