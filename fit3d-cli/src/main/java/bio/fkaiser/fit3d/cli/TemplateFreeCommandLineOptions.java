package bio.fkaiser.fit3d.cli;

import de.bioforscher.singa.structure.parser.pdb.rest.cluster.PDBSequenceCluster;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk
 */
public class TemplateFreeCommandLineOptions extends AbstractCommandLineOptions {

    private final OptionGroup inputOptions = new OptionGroup();

    public TemplateFreeCommandLineOptions() {
        super();
        generateOptions();
        generateInputOptions();
    }

    public static TemplateFreeCommandLineOptions create() {
        return new TemplateFreeCommandLineOptions();
    }

    private void generateOptions() {
        // target chain
        Option referenceChain = new Option("t", "reference chain");
        referenceChain.setLongOpt("reference-chain");
        referenceChain.setArgs(1);
        referenceChain.setRequired(false);
        options.addOption(referenceChain);

        // use user-provided JSON config
        Option configLocation = new Option("c", "user-defined config file");
        referenceChain.setLongOpt("config");
        referenceChain.setArgs(1);
        referenceChain.setRequired(false);
        options.addOption(configLocation);

        // representative level
        Option representativeLevel = new Option("r", "redundancy level used for automatic retrieval of representative structures (one of: "
                                                     + Stream.of(PDBSequenceCluster.PDBSequenceClusterIdentity.values())
                                                             .map(PDBSequenceCluster.PDBSequenceClusterIdentity::getIdentity)
                                                             .map(String::valueOf)
                                                             .collect(Collectors.joining(",", "[", "}")) + "; default: 70)");
        referenceChain.setLongOpt("representative-level");
        referenceChain.setArgs(1);
        referenceChain.setRequired(false);
        options.addOption(representativeLevel);
    }

    /**
     * Generates command line options for input definition.
     */
    private void generateInputOptions() {

        // target chain
        Option targetChain = new Option("t", "target chain*");
        targetChain.setLongOpt("target-chain**");
        targetChain.setArgs(1);
        targetChain.setRequired(true);
        inputOptions.addOption(targetChain);

        // target list
        Option targetChainList = new Option("l", "target list of chains*");
        targetChainList.setLongOpt("target-chain-list**");
        targetChainList.setArgs(1);
        targetChainList.setRequired(true);
        inputOptions.addOption(targetChainList);


        // target structure directory
        Option targetStructureDirectory = new Option("d", "directory with target structures*");
        targetChainList.setLongOpt("target-structures**");
        targetChainList.setArgs(1);
        targetChainList.setRequired(true);
        inputOptions.addOption(targetStructureDirectory);
    }

    public OptionGroup getInputOptions() {
        return inputOptions;
    }
}
