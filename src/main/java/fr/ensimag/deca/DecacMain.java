package fr.ensimag.deca;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl20
 * @date 01/01/2023
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);
    
    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();

        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
        if (options.getPrintBanner()) {
            System.out.println(DecacCompiler.TEAM_BANNER);
        }
        if (options.getSourceFiles().isEmpty() && !options.getPrintBanner()) {
            // La commande sans arguments affiche les options disponibles
            options.displayUsage();
        }
        if (options.getParallel()) {

            ExecutorService executor = Executors.newFixedThreadPool(DecacCompiler.MAX_THREADS);
            ArrayList<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
            for(File source : options.getSourceFiles()) {
                Callable<Boolean> compiler = new CompileTask(new DecacCompiler(options, source));
                futures.add(executor.submit(compiler));
            }

            // executor lance et termine tous les threads
            executor.shutdown();

            // on regarde si il y a une erreur 
            for(Future<Boolean> f : futures) {
                try {
                    if((boolean) f.get()) {
                        error = true;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error = true;
                }
            }
        }
        System.exit(error ? 1 : 0);
    }
}
