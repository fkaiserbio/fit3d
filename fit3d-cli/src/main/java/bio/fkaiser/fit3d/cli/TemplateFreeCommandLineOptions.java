package bio.fkaiser.fit3d.cli;

import bio.singa.structure.parser.pdb.rest.cluster.PDBSequenceCluster;
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
        Option referenceChain = new Option("n", "reference chain");
        referenceChain.setLongOpt("reference-chain");
        referenceChain.setArgs(1);
        referenceChain.setRequired(false);
        options.addOption(referenceChain);

        // use user-provided JSON config
        Option configLocation = new Option("c", "user-defined config file");
        configLocation.setLongOpt("config");
        configLocation.setArgs(1);
        configLocation.setRequired(false);
        options.addOption(configLocation);

        // representative level
        Option representativeLevel = new Option("r", "redundancy level used for automatic retrieval of representative structures via PDB REST API (one of: "
                                                     + Stream.of(PDBSequenceCluster.PDBSequenceClusterIdentity.values())
                                                             .map(PDBSequenceCluster.PDBSequenceClusterIdentity::getIdentity)
                                                             .map(String::valueOf)
                                                             .collect(Collectors.joining(",", "[", "]")) + "; default: 70)");
        representativeLevel.setLongOpt("representative-level");
        representativeLevel.setArgs(1);
        representativeLevel.setRequired(false);
        options.addOption(representativeLevel);

        // output directory
        Option outputLocation = new Option("o", "output directory for results*");
        outputLocation.setLongOpt("output-directory");
        outputLocation.setArgs(1);
        outputLocation.setRequired(true);
        options.addOption(outputLocation);

        // mapping rule
        Option mappingRule = new Option("m", "use one of the mapping schemes to group residues: " +
                                             "chemical groups (C), functional groups (F)");
        mappingRule.setLongOpt("mapping");
        mappingRule.setArgs(1);
        mappingRule.setArgName("C|F");
        options.addOption(mappingRule);

        // interaction enrichment
        Option interactionEnrichment = new Option("i", "annotate inter-residue interactions with PLIP\n" +
                                                       " WARNING: requires internet access");
        interactionEnrichment.setLongOpt("interactions");
        options.addOption(interactionEnrichment);
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
        Option targetChainList = new Option("l", "target list of chains**");
        targetChainList.setLongOpt("target-chain-list");
        targetChainList.setArgs(1);
        targetChainList.setRequired(true);
        inputOptions.addOption(targetChainList);


        // target structure directory
        Option targetStructureDirectory = new Option("d", "directory with target structures**");
        targetStructureDirectory.setLongOpt("target-structures");
        targetStructureDirectory.setArgs(1);
        targetStructureDirectory.setRequired(true);
        inputOptions.addOption(targetStructureDirectory);
    }

    public OptionGroup getInputOptions() {
        return inputOptions;
    }
}
