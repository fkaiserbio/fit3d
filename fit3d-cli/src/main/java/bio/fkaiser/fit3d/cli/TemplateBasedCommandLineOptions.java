package bio.fkaiser.fit3d.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * @author fk
 */
public class TemplateBasedCommandLineOptions extends AbstractCommandLineOptions {

    private final OptionGroup inputOptions = new OptionGroup();
    private final OptionGroup targetOptions = new OptionGroup();

    private TemplateBasedCommandLineOptions() {
        super();
        generateOptions();
        generateInputOptions();
        generateTargetOptions();
    }

    public static TemplateBasedCommandLineOptions create() {
        return new TemplateBasedCommandLineOptions();
    }

    /**
     * generates command line options
     */
    private void generateOptions() {

        // distance tolerance
        Option distanceTolerance = new Option("d", "allowed tolerance of template motif spatial extent (default: 1.0 \u212B)\n" +
                                                   "WARNING: performance decrease if raised, set lower value for larger motifs");
        distanceTolerance.setLongOpt("distance-tolerance");
        distanceTolerance.setArgs(1);
        options.addOption(distanceTolerance);

        // exchange residues
        Option exchangeResidues = new Option("e", "allowed residue exchanges for input motif (default: none)\n" +
                                                  " syntax: [motif residue number]:[allowed residues],...\n" +
                                                  " e.g. 12:ASHPW,43:PR");
        exchangeResidues.setLongOpt("exchange-residues");
        exchangeResidues.setArgs(1);
        options.addOption(exchangeResidues);

        // result file
        Option resultFile = new Option("f", "result file");
        resultFile.setLongOpt("result-file");
        resultFile.setArgs(1);
        options.addOption(resultFile);

        // number of threads
        Option numThreads = new Option("n", "number of threads used for calculation (default: all available)");
        numThreads.setLongOpt("num-threads");
        numThreads.setArgs(1);
        options.addOption(numThreads);

        // output structures directory
        Option outputFile = new Option("o", "output structures directory");
        outputFile.setLongOpt("output-structures");
        outputFile.setArgs(1);
        options.addOption(outputFile);

        // p-value calculation
        Option pvalueCalculation = new Option("P", "calculate p-values for matches according to Fafoanov et al. 2008 (F) or Stark et al. 2003 (S) (default: none)\n" +
                                                   "WARNING: F needs R in path with package sfsmisc installed");
        pvalueCalculation.setLongOpt("p-values");
        pvalueCalculation.setArgs(1);
        pvalueCalculation.setArgName("F|S");
        options.addOption(pvalueCalculation);

        // Pfam annotation mapping
        Option pfamMapping = new Option("M", "map Pfam annotation using SIFTS\n" +
                                             " WARNING: requires internet access");
        pfamMapping.setLongOpt("pfam-mapping");
        options.addOption(pfamMapping);

        // UniProt annotation mapping
        Option uniProtMapping = new Option("U", "map UniProt annotation using SIFTS\n" +
                                                " WARNING: requires internet access");
        uniProtMapping.setLongOpt("uniprot-mapping");
        options.addOption(uniProtMapping);

        // EC annotation mapping
        Option ecMapping = new Option("E", "map Enzyme Commission numbers using SIFTS\n" +
                                           " WARNING: requires internet access");
        ecMapping.setLongOpt("ec-mapping");
        options.addOption(ecMapping);

        // maximal RMSD
        Option rmsd = new Option("r", "maximal allowed RMSD for hits  (default: 2.0 \u212B)");
        rmsd.setLongOpt("rmsd");
        rmsd.setArgs(1);
        options.addOption(rmsd);

        Option distanceFiltering = new Option("y","filter local environments based on distance thresholds (default: disabled)");
        distanceFiltering.setLongOpt("distance-filtering");
        distanceFiltering.setArgs(1);
        options.addOption(distanceFiltering);

        // motif extraction
        Option motifExtraction = new Option("X",
                                            "extract motif from structure input (-m) following the syntax [chain]-[residue type][residue number]_... (e.g. A-E651_A-D649_A-T177)\n" +
                                            "INFO: a subsequent search is performed and the extracted motif is written in PDB format");
        motifExtraction.setLongOpt("extract");
        motifExtraction.setArgs(1);
        options.addOption(motifExtraction);
    }

    /**
     * Generates command line options for input definition.
     */
    private void generateInputOptions() {

        // motif
        Option motif = new Option("m", "motif PDB structure file*");
        motif.setLongOpt("motif");
        motif.setArgs(1);
        motif.setRequired(true);
        inputOptions.addOption(motif);

        // set input options required
        inputOptions.setRequired(true);
    }

    /**
     * Generates command line options for target definition.
     */
    private void generateTargetOptions() {

        // single target
        Option target = new Option("t", "target PDB-ID or file**");
        target.setLongOpt("target");
        target.setArgs(1);
        target.setRequired(true);
        targetOptions.addOption(target);

        // target list
        Option list = new Option("l", "target list of PDB-IDs or files**");
        list.setLongOpt("target-list");
        list.setArgs(1);
        list.setRequired(true);
        targetOptions.addOption(list);

        // set target options required
        targetOptions.setRequired(true);
    }

    public OptionGroup getInputOptions() {
        return inputOptions;
    }

    public OptionGroup getTargetOptions() {
        return targetOptions;
    }
}
