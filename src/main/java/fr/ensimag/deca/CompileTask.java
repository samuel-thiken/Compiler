package fr.ensimag.deca;

import java.util.concurrent.Callable;

public class CompileTask implements Callable<Boolean> {

    private DecacCompiler compiler;

    public CompileTask(DecacCompiler compiler) {
        this.compiler = compiler;
    }
    
    @Override
    public Boolean call() throws Exception {
        return Boolean.valueOf(compiler.compile());
    }
    
}
