package de.bioforscher.fit3d.cli;

import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.statistics.FofanovEstimation;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * @author fk
 */
public class Fit3DCommandLineOptions {

    private final Options options = new Options();
    private final OptionGroup inputOptions = new OptionGroup();
    private final OptionGroup targetOptions = new OptionGroup();
    private final OptionGroup representationOptions = new OptionGroup();
    private final OptionGroup verbosityOptions = new OptionGroup();
    private Fit3DCommandLineOptions() {
        generateOptions();
        generateInputOptions();
        generateTargetOptions();
        generateRepresentationOptions();
        generateVerbosityOptions();
    }

    public static Fit3DCommandLineOptions create() {
        return new Fit3DCommandLineOptions();
    }


    public Options getOptions() {
        return options;
    }

    public OptionGroup getInputOptions() {
        return inputOptions;
    }

    public OptionGroup getTargetOptions() {
        return targetOptions;
    }

    public OptionGroup getRepresentationOptions() {
        return representationOptions;
    }

    public OptionGroup getVerbosityOptions() {
        return verbosityOptions;
    }

    /**
     * generates command line options
     */
    private void generateOptions() {

        // align output
        Option alignOutputStructures = new Option("g", "align output structures (default: false)");
        alignOutputStructures.setLongOpt("align-output");
        this.options.addOption(alignOutputStructures);

        // conserve atoms
        Option conserve = new Option("c", "conserve atoms for structure output");
        conserve.setLongOpt("conserve");
        this.options.addOption(conserve);

        // distance tolerance
        Option distanceTolerance = new Option("d", "allowed tolerance of query motif spatial extent (default: 1.0 \u212B)\n" +
                                                   "WARNING: performance decrease if raised, set lower value for larger motifs");
        distanceTolerance.setLongOpt("distance-tolerance");
        distanceTolerance.setArgs(1);
        this.options.addOption(distanceTolerance);

        // exchange residues
        Option exchangeResidues = new Option("e", "allowed residue exchanges for input motif (default: none)\n" +
                                                  " syntax: [motif residue number]:[allowed residues],...\n" +
                                                  " e.g. 12:ASHPW,43:PR");
        exchangeResidues.setLongOpt("exchange-residues");
        exchangeResidues.setArgs(1);
        this.options.addOption(exchangeResidues);

        // EC mapping
        Option ecMapping = new Option("E", "map EC numbers by getting RESTful data from RCSB\n" +
                                           " WARNING: requires internet connection and decreases performance");
        ecMapping.setLongOpt("ec-mapping");
        this.options.addOption(ecMapping);

        // result file
        Option resultFile = new Option("f", "result file");
        resultFile.setLongOpt("result-file");
        resultFile.setArgs(1);
        this.options.addOption(resultFile);

        // help
        Option help = new Option("h", "show help dialog");
        help.setLongOpt("help");
        this.options.addOption(help);

        // number of threads
        Option numThreads = new Option("n", "number of threads used for calculation (default: all available)");
        numThreads.setLongOpt("num-threads");
        numThreads.setArgs(1);
        this.options.addOption(numThreads);

        // output structures directory
        Option outputFile = new Option("o", "output structures directory");
        outputFile.setLongOpt("output-structures");
        outputFile.setArgs(1);
        this.options.addOption(outputFile);

        // PDB directory
        Option pdb = new Option("p", "path to local PDB directory");
        pdb.setLongOpt("pdb");
        pdb.setArgs(1);
        this.options.addOption(pdb);

        // p-value calculation
        Option pvalueCalculation = new Option("P", "calculate p-values for matches according to Fafoanov et al. 2008 (F) or Stark et al. 2003 (S) (default: false)\n" +
                                                   "WARNING: F needs R in path with package sfsmisc installed");
        pvalueCalculation.setLongOpt("pvalues");
        pvalueCalculation.setArgs(1);
        pvalueCalculation.setArgName("F|S");
        this.options.addOption(pvalueCalculation);

        // Pfam annotation mapping
        Option pfamMapping = new Option("M", "map Pfam annotation by getting RESTful data from RCSB\n" +
                                             " WARNING: requires internet connection and decreases performance");
        pfamMapping.setLongOpt("pfam-mapping");
        this.options.addOption(pfamMapping);

        // reference population size for Fofanov et al.
        Option refSize = new Option("N", "size of reference population for p-value calculation to estimate point-weight correction according to Fofanov et al. 2008 (default: " +
                                         FofanovEstimation.DEFAULT_REFERENCE_SIZE + ")\n" +
                                         " WARNING: changing this parameter requires knowledge of the underlying method");
        refSize.setLongOpt("ref-size");
        refSize.setArgs(1);
        this.options.addOption(refSize);

        // maximal RMSD
        Option rmsd = new Option("r", "maximal allowed LRMSD for hits  (default: 2.0 \u212B)");
        rmsd.setLongOpt("rmsd");
        rmsd.setArgs(1);
        this.options.addOption(rmsd);

        // PDB directory split
        Option pdbSplit = new Option("s", "disable PDB directory split (default: false)");
        pdbSplit.setLongOpt("no-pdb-split");
        this.options.addOption(pdbSplit);

        // title mapping
        Option titleMapping = new Option("T", "map structure titles assigned by PDB");
        titleMapping.setLongOpt("title-mapping");
        this.options.addOption(titleMapping);

        // motif extraction
        Option motifExtraction = new Option("X",
                                            "extract motif from structure input (-m) following the syntax [chain]-[residue type][residue number]_... (e.g. A-E651_A-D649_A-T177)\n" +
                                            "INFO: a subsequent search is performed and the extracted motif is written in PDB format");
        motifExtraction.setLongOpt("extract");
        motifExtraction.setArgs(1);
        this.options.addOption(motifExtraction);
    }

    /**
     * generate command line options for input definition
     */
    private void generateInputOptions() {

        // motif
        Option motif = new Option("m", "motif PDB structure file*");
        motif.setLongOpt("motif");
        motif.setArgs(1);
        motif.setRequired(true);
        this.inputOptions.addOption(motif);

        // set input options required
        this.inputOptions.setRequired(true);
    }

    /**
     * generate command line options for target definition
     */
    private void generateTargetOptions() {

        // single target
        Option target = new Option("t", "target PDB-ID or file**");
        target.setLongOpt("target");
        target.setArgs(1);
        target.setRequired(true);
        this.targetOptions.addOption(target);

        // target list
        Option list = new Option("l", "target list of PDB-IDs or files**");
        list.setLongOpt("target-list");
        list.setArgs(1);
        list.setRequired(true);
        this.targetOptions.addOption(list);

        // set target options required
        this.targetOptions.setRequired(true);
    }

    /**
     * generated command line options for structure representation
     */
    private void generateRepresentationOptions() {

        // representation scheme
        Option representationScheme = new Option("R", "use one of the representation schemes to represent residues: " +
                                                      "alpha-carbon, beta carbon, centroid, last heavy side chain, side chain centroid");
        representationScheme.setLongOpt("scheme");
        representationScheme.setArgs(1);
        representationScheme.setArgName("CA|CB|CO|LH|SC");
        this.representationOptions.addOption(representationScheme);

        // atoms
        Option atoms = new Option("a", "PDB identifier of atoms used for alignment (default: all non-hydrogen motif atoms)");
        atoms.setLongOpt("atoms");
        atoms.setArgName("CA,CB,CG,CD,...");
        atoms.setArgs(1);
        this.representationOptions.addOption(atoms);
    }

    /**
     * generate command line options for verbosity
     */
    private void generateVerbosityOptions() {

        Option quiet = new Option("q", "show only results");
        quiet.setLongOpt("quiet");
        this.verbosityOptions.addOption(quiet);

        Option verbose = new Option("v", "verbose output");
        verbose.setLongOpt("verbose");
        this.verbosityOptions.addOption(verbose);

        Option extraVerbose = new Option("x", "extra verbose output");
        extraVerbose.setLongOpt("vverbose");
        this.verbosityOptions.addOption(extraVerbose);
    }
}
