package fr.ensimag.ima.pseudocode;

/**
 * Operand representing a register indirection with offset, e.g. 42(R3).
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class RegisterOffset extends DAddr {
    public int getOffset() {
        return offset;
    }
    public Register getRegister() {
        return register;
    }
    private final int offset;
    private final Register register;
    private final Register registerOffset;
    public RegisterOffset(int offset, Register register) {
        this(offset, register, null);
    }
    public RegisterOffset(int offset, Register register, Register registerOffset) {
        super();
        this.offset = offset;
        this.register = register;
        this.registerOffset = registerOffset;
    }
    @Override
    public String toString() {
        if (registerOffset != null) return offset + "(" + register + ", " + registerOffset + ")";
        return offset + "(" + register + ")";
    }
}
