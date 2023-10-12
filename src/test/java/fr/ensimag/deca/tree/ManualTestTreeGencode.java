package fr.ensimag.deca.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;

public class ManualTestTreeGencode {

    public static void main(String[] args) throws IOException, ContextualError, InterruptedException {
        final CompilerOptions options = new CompilerOptions();

        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        for (File source : options.getSourceFiles()) {
            compile(source, options);
        }
    }

    public static void compile(File file, CompilerOptions options) throws IOException, ContextualError, InterruptedException {
        
        File codeFile = null;
        if (file.getName() != null) {
            codeFile = File.createTempFile(file.getName() + "-", "-assembly");
            codeFile.deleteOnExit();
        }
        final DecacCompiler decacCompiler = new DecacCompiler(options, file);

        // Lexer
        DecaLexer lexer = new DecaLexer(CharStreams.fromFileName(file.getAbsolutePath()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Parser
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(decacCompiler);
        AbstractProgram prog = parser.parseProgramAndManageErrors(System.err);
        
        // Verification
        prog.verifyProgram(decacCompiler);
       // if(!prog.checkAllDecorations()) throw new RuntimeException("Missing decorations");
        System.out.println("---- From the following Abstract Syntax Tree ----");
        prog.prettyPrint(System.out);

        // Codegen
        System.out.println("\n---- We generate the following assembly code ----");
        String code = gencodeSource(decacCompiler, prog, codeFile);
        System.out.println(code);
        
        // Execution
        System.out.println("\n---- And we get the following execution ----");
        System.out.println(execCode(codeFile));
    }

    public static String gencodeSource(DecacCompiler compiler, AbstractProgram source, File codeFile) throws FileNotFoundException {
        RuntimeException e = null;
        try {
            source.codeGenProgram(compiler);
        } catch (RuntimeException er) {
            e = er;
        }
        String prog = compiler.displayIMAProgram();
        String[] lines = prog.split("\n");
        String result = "";
        for (int i = 0; i < lines.length; i++) {
            result += String.format("%03d | %s\n", i + 1, lines[i]);
        }
        if (e != null) {
            System.out.println(result);
            throw e;
        } 
        writeCodeToFile(prog, codeFile);
        return result;
    }

    public static void writeCodeToFile(String code, File file) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(file);
        out.print(code);
        out.close();
    }

    public static String execCode(File file) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("ima", file.getAbsolutePath()).start();
        
        // Process input
        // Scanner sc = new Scanner(System.in);
        // (new Thread(() -> {
        //     try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
        //         String line;
        //         do {
        //             line = sc.nextLine();
        //             if (line != null) {
        //                 out.append(line + "\n");
        //                 out.flush();
        //             }
        //         } while (line != null);
        //     } catch (IOException exception) {
        //     }
        //     sc.close();
        // })).start();

        // Process output
        String result = "";
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            do {
                line = in.readLine();
                if (line != null)
                    result += String.format("%s\n", line);
            } while (line != null);
        }

        return result;
    }
}