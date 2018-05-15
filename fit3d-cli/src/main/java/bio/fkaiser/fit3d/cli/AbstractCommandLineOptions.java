package bio.fkaiser.fit3d.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * @author fk
 */
public abstract class AbstractCommandLineOptions {

    protected final Options options = new Options();
    private final OptionGroup representationOptions = new OptionGroup();
    public AbstractCommandLineOptions() {
        generateRepresentationOptions();
        generateBaseOptions();
    }

    protected void generateBaseOptions() {
        // help
        Option help = new Option("h", "show help dialog");
        help.setLongOpt("help");
        options.addOption(help);

        // PDB directory
        Option pdb = new Option("p", "path to local PDB directory");
        pdb.setLongOpt("pdb");
        pdb.setArgs(1);
        options.addOption(pdb);

        // PDB directory
        Option mmtf = new Option("F", "use MMTF format for local PDB and as online resource (default: false)");
        mmtf.setLongOpt("mmtf");
        options.addOption(mmtf);
    }

    /**
     * Generates command line options for structure representation.
     */
    private void generateRepresentationOptions() {

        // representation scheme
        Option representationScheme = new Option("R", "use one of the representation schemes to represent residues: " +
                                                      "alpha-carbon, beta carbon, centroid, last heavy side chain, side chain centroid");
        representationScheme.setLongOpt("scheme");
        representationScheme.setArgs(1);
        representationScheme.setArgName("CA|CB|CO|LH|SC");
        representationOptions.addOption(representationScheme);

        // atoms
        Option atoms = new Option("a", "PDB identifier of atoms used for alignment (default: all non-hydrogen motif atoms)");
        atoms.setLongOpt("atoms");
        atoms.setArgName("CA,CB,CG,CD,...");
        atoms.setArgs(1);
        representationOptions.addOption(atoms);
    }

    public Options getOptions() {
        return options;
    }

    public OptionGroup getRepresentationOptions() {
        return representationOptions;
    }
}
