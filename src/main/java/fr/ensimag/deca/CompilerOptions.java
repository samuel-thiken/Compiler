package fr.ensimag.deca;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl20 R
 * @date 01/01/2023
 */
public class CompilerOptions {

    public static final int QUIET = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;

    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }

    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    public boolean getStopAfterParse() {
        return stopAfterParse;
    }

    public boolean getStopAfterVerify() {
        return stopAfterVerify;
    }

    public int getAvailableRegisters() {
        return availableRegisters;
    }

    public boolean shouldErrorCheck() {
        return errorCheck;
    }

    public void setEnableTabs(boolean value) {
        enableTabs = value;
    }
    public boolean enableTabs() {
        return enableTabs;
    }

    // Niveau de debug (voir le haut du fichier)
    private int debug = 0;
    // Par défaut, les registres R0, R1, ..., R15 sont disponibles
    private int availableRegisters = 15;
    // Compilation des fichiers en parallèle
    private boolean parallel = false;
    // Affichage de la bannière de l'équipe
    private boolean printBanner = false;
    // Arrêt après construction de l'arbre
    private boolean stopAfterParse = false;
    // Arrêt après vérification de l'arbre
    private boolean stopAfterVerify = false;
    // Vérification des erreurs dans l'assembleur
    private boolean errorCheck = true;
    // Activation des tableaux
    private boolean enableTabs = false;

    private List<File> sourceFiles = new ArrayList<File>();

    public void parseArgs(String[] args) throws CLIException {

        if (args.length == 0) {
            return;
        }

        if (args[0].equals("-b")) {
            if (args.length == 1) {
                printBanner = true;
                return;
            } else {
                throw new CLIException("Impossible to call decac -b with other arguments");
            }
        }

        List<String> listArgs = Arrays.asList(args);

        if (listArgs.contains("-p")) {
            stopAfterParse = true;
        }

        if (listArgs.contains("-v")) {
            if (!stopAfterParse)
                stopAfterVerify = true;
            else
                throw new CLIException("Impossible to use -p and -v simultaneously");
        }

        if (listArgs.contains("-r")) {
            try {
                availableRegisters = Integer.parseInt(listArgs.get(listArgs.lastIndexOf("-r") + 1)) - 1;
            } catch (NumberFormatException e) {
                throw new CLIException("Invalid position of X in -r X option");
            }
            if (availableRegisters > 16) {
                throw new CLIException("Impossible to use more than 16 registers");
            }
        }

        // Source files
        for (String s : new LinkedHashSet<String>(listArgs)) {
            if (s.endsWith(".deca")) {
                sourceFiles.add(new File(s));
            }
        }

        // Debug level
        if (listArgs.contains("-d")) {
            int numberOfD = (int) listArgs.stream().filter(opt -> "-d".equalsIgnoreCase(opt)).count();
            debug = (numberOfD >= 3 ? 3 : numberOfD);
        }

        // Parallel
        if (listArgs.contains("-P")) {
            parallel = true;
        }

        // Erreurs
        if (listArgs.contains("-n")) {
            errorCheck = false;
        }

        // Extension tableaux
        if (listArgs.contains("-t")) {
            enableTabs = true;
        }

        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
            case QUIET:
                break; // keep default
            case INFO:
                logger.setLevel(Level.INFO);
                break;
            case DEBUG:
                logger.setLevel(Level.DEBUG);
                break;
            case TRACE:
                logger.setLevel(Level.TRACE);
                break;
            default:
                logger.setLevel(Level.ALL);
                break;
        }

        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }
    }

    protected void displayUsage() {
        System.out.println(DecacCompiler.DECAC_SYNTAX);
    }
}
