package bio.fkaiser.fit3d.cli;

import bio.fkaiser.mmm.model.configurations.ItemsetMinerConfiguration;
import bio.fkaiser.mmm.model.configurations.metrics.ConsensusMetricConfiguration;
import bio.fkaiser.mmm.model.enrichment.IntraChainInteractionEnricher;
import bio.fkaiser.mmm.model.mapping.MappingRule;
import bio.fkaiser.mmm.model.mapping.rules.ChemicalGroupsMappingRule;
import bio.fkaiser.mmm.model.mapping.rules.FunctionalGroupsMappingRule;
import bio.fkaiser.mmm.model.plip.PlipPostRequest;
import de.bioforscher.singa.core.utility.Resources;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.representations.RepresentationSchemeType;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.statistics.FofanovEstimation;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.statistics.StarkEstimation;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.statistics.StatisticalModel;
import de.bioforscher.singa.structure.model.families.AminoAcidFamily;
import de.bioforscher.singa.structure.model.identifiers.LeafIdentifier;
import de.bioforscher.singa.structure.model.interfaces.Atom;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.interfaces.Structure;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter.AtomFilter;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import de.bioforscher.singa.structure.parser.pdb.rest.cluster.PDBSequenceCluster;
import de.bioforscher.singa.structure.parser.pdb.rest.cluster.PDBSequenceCluster.PDBSequenceClusterIdentity;
import de.bioforscher.singa.structure.parser.pdb.structures.SourceLocation;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk
 */
public class Fit3DCommandLine {

    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("([1-9a-zA-Z][0-9a-zA-Z]{3})(\\.[\\w]+)");

    public static final double DEFAULT_RMSD_CUTOFF = 2.0;
    public static final double DEFAULT_DISTANCE_TOLERANCE = 1.0;
    public static final Predicate<Atom> DEFAULT_ATOM_FILTER = StructuralEntityFilter.AtomFilter.isArbitrary();
    public static final PDBSequenceClusterIdentity DEFAULT_SEQUENCE_CLUSTER_IDENTITY = PDBSequenceClusterIdentity.IDENTITY_70;

    public static final int DEFAULT_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    private static final Logger logger = LoggerFactory.getLogger(Fit3DCommandLine.class);
    private final Fit3DMode mode;
    private final CommandLine commandLine;
    private StructuralMotif queryMotif;

    private Predicate<Atom> atomFilter;
    private RepresentationSchemeType representationSchemeType;
    private double rmsd = DEFAULT_RMSD_CUTOFF;
    private PDBSequenceClusterIdentity pdbSequenceClusterIdentity = DEFAULT_SEQUENCE_CLUSTER_IDENTITY;
    private MappingRule<String> mappingRule;
    private Double distanceTolerance = DEFAULT_DISTANCE_TOLERANCE;
    private Path resultFilePath;
    private Path targetListPath;
    private String target;
    private boolean ecMapping;
    private boolean pfamMapping;
    private boolean uniProtMapping;
    private StructureParser.LocalPDB localPdb;
    private boolean mmtf;
    private StatisticalModel statisticalModel;
    private int numberOfThreads = DEFAULT_NUMBER_OF_THREADS;
    private Path outputDirectoryPath;
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;

    public Fit3DCommandLine(Fit3DMode mode, CommandLine commandLine) throws ParseException {
        this.mode = mode;
        this.commandLine = commandLine;
        try {
            initializeParameters();
            initializeOutput();
            initializeRun();
        } catch (Fit3DCommandLineException e) {
            logger.error("Fit3D run failed.", e);
        }
    }

    public static void main(String[] args) {

        // set default localization
        Locale.setDefault(Locale.US);

        System.out.println("\n\n__/\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\_________________________/\\\\\\\\\\\\\\\\\\\\___/\\\\\\\\\\\\\\\\\\\\\\\\____"
                           + "\n_\\/\\\\\\///////////________________________/\\\\\\///////\\\\\\_\\/\\\\\\////////\\\\\\__"
                           + " \n_\\/\\\\\\______________/\\\\\\_____/\\\\\\_______\\///______/\\\\\\__\\/\\\\\\______\\//\\\\\\_"
                           + "\n  _\\/\\\\\\\\\\\\\\\\\\\\\\_____\\///___/\\\\\\\\\\\\\\\\\\\\\\_________/\\\\\\//___\\/\\\\\\_______\\/\\\\\\_"
                           + "\n   _\\/\\\\\\///////_______/\\\\\\_\\////\\\\\\////_________\\////\\\\\\__\\/\\\\\\_______\\/\\\\\\_"
                           + "\n    _\\/\\\\\\_____________\\/\\\\\\____\\/\\\\\\________________\\//\\\\\\_\\/\\\\\\_______\\/\\\\\\_"
                           + "\n     _\\/\\\\\\_____________\\/\\\\\\____\\/\\\\\\_/\\\\___/\\\\\\______/\\\\\\__\\/\\\\\\_______/\\\\\\__"
                           + "\n      _\\/\\\\\\_____________\\/\\\\\\____\\//\\\\\\\\\\___\\///\\\\\\\\\\\\\\\\\\/___\\/\\\\\\\\\\\\\\\\\\\\\\\\/___"
                           + "\n       _\\///______________\\///______\\/////______\\/////////_____\\////////////_____"
                           + "\n               Copyright (C) 2018 Florian Kaiser (contact@fkaiser.bio)\n");

        // decide whether template-based or template-free mode is used
        Fit3DMode mode = null;
        if (args.length == 0) {
            logger.error("Command line options cannot be empty.");
            System.out.println("usage: java -jar Fit3D.jar [template-based | template-free] [OPTIONS]\n");
            System.exit(0);
        } else {
            if (args[0].equals(Fit3DMode.TEMPLATE_BASED.getCommandLineFlag())) {
                mode = Fit3DMode.TEMPLATE_BASED;
            } else if (args[0].equals(Fit3DMode.TEMPLATE_FREE.getCommandLineFlag())) {
                mode = Fit3DMode.TEMPLATE_FREE;
            } else {
                logger.error("Invalid mode specified, must be either: \"template-based\" or \"template-free\"");
                System.exit(0);
            }
        }

        Options options = null;
        switch (mode) {
            case TEMPLATE_BASED:
                // create instance of command line options
                TemplateBasedCommandLineOptions templateBasedCommandLineOptions = TemplateBasedCommandLineOptions.create();
                // get Fit3D TB options
                options = templateBasedCommandLineOptions.getOptions();
                // add input option group
                options.addOptionGroup(templateBasedCommandLineOptions.getInputOptions());
                // add target option group
                options.addOptionGroup(templateBasedCommandLineOptions.getTargetOptions());
                // add representation option group
                options.addOptionGroup(templateBasedCommandLineOptions.getRepresentationOptions());
                break;
            case TEMPLATE_FREE:
                TemplateFreeCommandLineOptions templateFreeCommandLineOptions = TemplateFreeCommandLineOptions.create();
                //get Fit3D TF options
                options = templateFreeCommandLineOptions.getOptions();
                // add input option group
                options.addOptionGroup(templateFreeCommandLineOptions.getInputOptions());
                // add representation option group
                options.addOptionGroup(templateFreeCommandLineOptions.getRepresentationOptions());
                break;
        }

        DefaultParser defaultParser = new DefaultParser();
        try {
            CommandLine commandLine = defaultParser.parse(options, args);
            new Fit3DCommandLine(mode, commandLine);
        } catch (ParseException e) {
            logger.error("Command line options are invalid.", e);
            printHelp(mode, options);
        }
    }

    private static void printHelp(Fit3DMode mode, Options options) {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(256);
        help.setSyntaxPrefix("usage: ");
        switch (mode) {
            case TEMPLATE_BASED:
                help.printHelp("java -jar Fit3D.jar template-based -m <arg> [-t <arg> | -l <arg>] [OPTIONS]\n", options);
                break;
            case TEMPLATE_FREE:
                help.printHelp("java -jar Fit3D.jar template-free [-t <arg> | -l <arg> | -d <arg>] -o <arg> [OPTIONS]\n", options);
                break;
        }
        System.out.println("\n* = required");
        System.out.println("** = one of these required");
    }

    /**
     * Initializes the settings for output of results.
     */
    private void initializeOutput() {
        if (commandLine.hasOption('o')) {
            outputDirectoryPath = Paths.get(commandLine.getOptionValue('o'));
        }
        // store path for result file if specified
        if (commandLine.hasOption('f')) {
            resultFilePath = Paths.get(commandLine.getOptionValue('f'));
        }
    }

    /**
     * Runs an instance of Fit3D with the specified parameters.
     */
    private void initializeRun() throws Fit3DCommandLineException {
        switch (mode) {
            case TEMPLATE_BASED:
                logger.info("running Fit3D in template-based mode");
                new Fit3DTemplateBasedCommandLineRunner(this);
                break;
            case TEMPLATE_FREE:
                logger.info("running Fit3D in template-free mode");
                new Fit3DTemplateFreeCommandLineRunner(this);
                break;
        }
    }

    private void initializeParameters() throws ParseException, Fit3DCommandLineException {

        // set custom atom filter (default: all non-hydrogen atoms)
        atomFilter = AtomFilter.isHydrogen().negate();
        try {
            createRepresentation();
        } catch (Fit3DCommandLineException e) {
            logger.error("failed to define custom atoms", e);
            throw new Fit3DCommandLineException("Initialization of parameters failed.");
        }

        // check if MMTF should be used
        if (commandLine.hasOption('F')) {
            mmtf = true;
        }

        // check if local PDB was provided an use MMTF if specified
        if (commandLine.hasOption('p')) {
            if (mmtf) {
                localPdb = new StructureParser.LocalPDB(commandLine.getOptionValue('p'), SourceLocation.OFFLINE_MMTF);
            } else {
                localPdb = new StructureParser.LocalPDB(commandLine.getOptionValue('p'), SourceLocation.OFFLINE_PDB);
            }
        }

        switch (mode) {
            case TEMPLATE_BASED:
                initializeTemplateBasedParameters();
                break;
            case TEMPLATE_FREE:
                initializeTemplateFreeParameters();
                break;
        }
    }

    private void initializeTemplateFreeParameters() throws Fit3DCommandLineException {

        try {

            // check if config is provided
            if (commandLine.hasOption('c')) {
                Path configurationPath = Paths.get(commandLine.getOptionValue('c'));
                itemsetMinerConfiguration = ItemsetMinerConfiguration.from(configurationPath);
                logger.info("using predefined configuration file {}", configurationPath);
                return;
            } else {
                // read template config
                itemsetMinerConfiguration = ItemsetMinerConfiguration.from(Resources.getResourceAsStream("configuration.json"));
            }

            // determine input
            if (commandLine.hasOption('t')) {
                String inputChain = commandLine.getOptionValue('t');
                Matcher matcher = IDENTIFIER_PATTERN.matcher(inputChain);
                if (!matcher.matches()) {
                    throw new Fit3DCommandLineException("Input chain specification is not valid, must be [PDB-ID].[chain-ID].");
                }
                itemsetMinerConfiguration.setInputChain(inputChain);
                itemsetMinerConfiguration.setInputDirectoryLocation(null);
                itemsetMinerConfiguration.setInputListLocation(null);
                logger.info("using single chain input {}", inputChain);
            } else if (commandLine.hasOption('l')) {
                String inputListLocation = commandLine.getOptionValue('l');
                itemsetMinerConfiguration.getDataPointReaderConfiguration().setChainListSeparator("\\.");
                itemsetMinerConfiguration.setInputListLocation(inputListLocation);
                logger.info("using single chain list input {}", inputListLocation);
                String defaultReferenceChain = Files.lines(Paths.get(inputListLocation))
                                                    .filter(IDENTIFIER_PATTERN.asPredicate())
                                                    .findFirst()
                                                    .orElseThrow(() -> new Fit3DCommandLineException("Input chain list does not contain any valid structures in the format [PDB-ID].[chain-ID]."));
                itemsetMinerConfiguration.setReferenceChain(defaultReferenceChain);
                itemsetMinerConfiguration.setInputChain(null);
                itemsetMinerConfiguration.setInputDirectoryLocation(null);
            } else if (commandLine.hasOption('d')) {
                String inputDirectoryLocation = commandLine.getOptionValue('d');
                itemsetMinerConfiguration.setInputDirectoryLocation(inputDirectoryLocation);
                logger.info("using directory input {}", inputDirectoryLocation);
                itemsetMinerConfiguration.setInputChain(null);
                itemsetMinerConfiguration.setInputListLocation(null);
            }

            // representation for consensus metric
            Optional<ConsensusMetricConfiguration> consensusMetricConfigurationOptional = itemsetMinerConfiguration.getExtractionDependentMetricConfigurations().stream()
                                                                                                                   .filter(ConsensusMetricConfiguration.class::isInstance)
                                                                                                                   .map(ConsensusMetricConfiguration.class::cast)
                                                                                                                   .findFirst();
            if (consensusMetricConfigurationOptional.isPresent()) {
                if (representationSchemeType != null) {
                    consensusMetricConfigurationOptional.get().setRepresentationSchemeType(representationSchemeType);
                    logger.info("using representation scheme {}", representationSchemeType);
                } else {
                    logger.info("using atom filter");
                    consensusMetricConfigurationOptional.get().setAtomFilter(atomFilter);
                }
            }

            // overwrite reference chain
            if (commandLine.hasOption('n')) {
                String referenceChain = commandLine.getOptionValue('n');
                Matcher matcher = IDENTIFIER_PATTERN.matcher(referenceChain);
                if (!matcher.matches()) {
                    throw new Fit3DCommandLineException("Reference chain specification is not valid, must be [PDB-ID].[chain-ID].");
                }
                itemsetMinerConfiguration.setReferenceChain(referenceChain);
                logger.info("reference chain used will be {}", itemsetMinerConfiguration.getReferenceChain());
            }
            if (itemsetMinerConfiguration.getInputDirectoryLocation() != null && !commandLine.hasOption('n')) {
                throw new Fit3DCommandLineException("Reference chain specification is mandatory if using structures in input directory.");
            }

            // set representative level
            if (commandLine.hasOption('r')) {
                try {
                    Integer representativeLevel = Integer.valueOf(commandLine.getOptionValue('r'));
                    Optional<PDBSequenceClusterIdentity> clusterIdentityOptional = Stream.of(PDBSequenceClusterIdentity.values())
                                                                                         .filter(pdbSequenceClusterIdentity -> pdbSequenceClusterIdentity.getIdentity() == representativeLevel)
                                                                                         .findFirst();
                    clusterIdentityOptional.ifPresent(clusterIdentity -> pdbSequenceClusterIdentity = clusterIdentity);

                    logger.info("using representative level {}", pdbSequenceClusterIdentity);
                } catch (NumberFormatException e) {
                    logger.error("invalid representative level");
                    throw new Fit3DCommandLineException("Representative level must be one of the following: " + Stream.of(PDBSequenceCluster.PDBSequenceClusterIdentity.values())
                                                                                                                      .map(PDBSequenceCluster.PDBSequenceClusterIdentity::getIdentity)
                                                                                                                      .map(String::valueOf)
                                                                                                                      .collect(Collectors.joining(",", "[", "]")));
                }
            }
            itemsetMinerConfiguration.getDataPointReaderConfiguration().setPdbSequenceCluster(pdbSequenceClusterIdentity);


            // set output directory
            String outputLocation = commandLine.getOptionValue('o');
            itemsetMinerConfiguration.setOutputLocation(outputLocation);
            logger.info("results will be written to {}", outputLocation);

            // set local PDB
            if (localPdb != null) {
                itemsetMinerConfiguration.getDataPointReaderConfiguration().setLocalPDB(localPdb);
                itemsetMinerConfiguration.getDataPointReaderConfiguration().setMmtf(mmtf);
                logger.info("using {}", localPdb);
            }

            // set mapping rule
            if (commandLine.hasOption('m')) {
                switch (commandLine.getOptionValue('m')) {
                    case "C":
                        mappingRule = new ChemicalGroupsMappingRule();
                        break;
                    case "F":
                        mappingRule = new FunctionalGroupsMappingRule();
                        break;
                    default:
                        throw new Fit3DCommandLineException("Mapping rule specification is invalid.");
                }
                itemsetMinerConfiguration.setMappingRules(Stream.of(mappingRule).collect(Collectors.toList()));
                logger.info("using mapping rule {}", mappingRule);
            }

            // add interaction enrichment
            if (commandLine.hasOption('i')) {
                // trigger check for credentials
                new PlipPostRequest(null, null, null);

                if (commandLine.hasOption('F')) {
                    throw new Fit3DCommandLineException("MMTF format cannot be used when enriching interactions.");
                }
                itemsetMinerConfiguration.setDataPointEnricher(new IntraChainInteractionEnricher());
                logger.info("noncovalent interactions will be added as pseudoatoms");
            }


        } catch (IOException e) {
            logger.error("failed to initialize from given parameters", e);
            throw new Fit3DCommandLineException("Initialization of parameters failed.");
        }
    }

    private void initializeTemplateBasedParameters() throws Fit3DCommandLineException, ParseException {
        // parse motif
        Path motifPath = Paths.get(commandLine.getOptionValue('m'));
        queryMotif = null;
        try {
            Structure motifStructure = StructureParser.local()
                                                      .path(motifPath)
                                                      .parse();
            queryMotif = StructuralMotif.fromLeafSubstructures(motifStructure.getAllLeafSubstructures());
        } catch (StructureParserException | UncheckedIOException e) {
            logger.error("failed to load query motif from path {}", motifPath, e);
            throw new Fit3DCommandLineException("Initialization of parameters failed.");
        }

        // only use a defined subset of the input structure (extract motif functionality)
        if (commandLine.hasOption('X')) {
            try {
                extractResiduesFromQuery();
            } catch (Fit3DCommandLineException e) {
                logger.error("failed to extract specified residues from query structure", e);
                throw new Fit3DCommandLineException("Initialization of parameters failed.");
            }
        }

        logger.info("query motif contains {} residues", queryMotif.getAllLeafSubstructures().size());

        // assign exchanges to query motif
        try {
            assignExchanges();
        } catch (Fit3DCommandLineException e) {
            logger.error("failed to define exchanges", e);
            throw new Fit3DCommandLineException("Initialization of parameters failed.");
        }

        // check for mappings of annotations
        ecMapping = commandLine.hasOption('E');
        uniProtMapping = commandLine.hasOption('U');
        pfamMapping = commandLine.hasOption('M');

        // number of threads
        if (commandLine.hasOption('n')) {
            try {
                numberOfThreads = Integer.valueOf(commandLine.getOptionValue('n'));
            } catch (NumberFormatException e) {
                logger.error("failed to parse number of threads", e);
                throw new Fit3DCommandLineException("Initialization of parameters failed.");
            }
        }

        // show help dialog
        if (commandLine.hasOption('h')) {
            throw new ParseException("");
        }

        // set RMSD value
        if (commandLine.hasOption('r')) {
            try {
                rmsd = Double.valueOf(commandLine.getOptionValue('r'));
            } catch (NumberFormatException e) {
                logger.error("failed to parse RMSD cutoff", e);
                throw new Fit3DCommandLineException("Initialization of parameters failed.");
            }
        }

        // set distance tolerance value
        if (commandLine.hasOption('d')) {
            try {
                distanceTolerance = Double.valueOf(commandLine.getOptionValue('d'));
            } catch (NumberFormatException e) {
                logger.error("failed to parse distance tolerance", e);
                throw new Fit3DCommandLineException("Initialization of parameters failed.");
            }
        }


        // check if list was given
        if (commandLine.hasOption('l')) {
            targetListPath = Paths.get(commandLine.getOptionValue('l'));
        }

        // check if single target was given
        if (commandLine.hasOption('t')) {
            target = commandLine.getOptionValue('t');
        }

        // check if statistical model was specified
        if (commandLine.hasOption('P')) {
            createStatisticalModel();
        }
    }

    private void createStatisticalModel() throws ParseException {
        String modelString = commandLine.getOptionValue('P');
        switch (modelString) {
            case "S":
                statisticalModel = new StarkEstimation();
                break;
            case "F":
                statisticalModel = new FofanovEstimation(rmsd);
                break;
            default:
                throw new ParseException("Statistical model must be either 'F' or 'S'.");
        }
    }

    /**
     * Extracts only the specified residues from the query {@link StructuralMotif}.
     *
     * @throws Fit3DCommandLineException If the identifiers of residues that should be extracted are malformed.
     */
    private void extractResiduesFromQuery() throws Fit3DCommandLineException {
        List<LeafIdentifier> leafIdentifiers = new ArrayList<>();
        for (String identifierString : commandLine.getOptionValue('X').split("_")) {
            try {
                leafIdentifiers.add(LeafIdentifier.fromSimpleString(identifierString));
            } catch (NumberFormatException e) {
                throw new Fit3DCommandLineException("Failed to parse residue identifier " + identifierString + " that should be extracted.");
            }
        }
        // reduce query structure to residues specified for extraction
        List<LeafSubstructure<?>> leafSubstructuresToRemove = new ArrayList<>();
        for (LeafSubstructure<?> leafSubstructure : queryMotif.getAllLeafSubstructures()) {
            // check whether the current leaf substructure is contained in the specified residues for extraction
            boolean match = leafIdentifiers.stream()
                                           .anyMatch(leafIdentifier -> leafIdentifier.getChainIdentifier().equals(leafSubstructure.getChainIdentifier())
                                                                       && leafIdentifier.getSerial() == leafSubstructure.getSerial()
                                                                       && leafIdentifier.getInsertionCode() == leafIdentifier.getInsertionCode());
            if (match) {
                leafSubstructuresToRemove.add(leafSubstructure);
            }
        }
        logger.info("residues that will be extracted from query structure are {}", leafIdentifiers);
        leafSubstructuresToRemove.forEach(queryMotif::removeLeafSubstructure);
    }

    /**
     * Creates a {@link Predicate} used to filter certain atoms for alignment.
     *
     * @throws Fit3DCommandLineException If invalid atom names are specified, which are not part of the query {@link StructuralMotif}.
     */
    private void createRepresentation() throws Fit3DCommandLineException {
        if (commandLine.hasOption('a')) {
            // extract all atom names from given motif
            Set<String> uniqueAtomNames = queryMotif.getAllAtoms().stream()
                                                    .map(Atom::getAtomName)
                                                    .collect(Collectors.toSet());
            // extract all given atoms and check if atoms are valid characters and contained in motif
            List<String> commandLineAtoms = new ArrayList<>();
            for (String atomName : commandLine.getOptionValue('a').toUpperCase().split(",")) {
                if (!uniqueAtomNames.contains(atomName)) {
                    throw new Fit3DCommandLineException("One or more specified atom is not contained in motif.");
                } else {
                    commandLineAtoms.add(atomName);
                }
            }
            atomFilter = AtomFilter.hasAtomNames(commandLineAtoms.toArray(new String[0]));
        } else if (commandLine.hasOption('R')) {
            switch (commandLine.getOptionValue('R')) {
                case "CA":
                    representationSchemeType = RepresentationSchemeType.ALPHA_CARBON;
                    break;
                case "CB":
                    representationSchemeType = RepresentationSchemeType.BETA_CARBON;
                    break;
                case "CO":
                    representationSchemeType = RepresentationSchemeType.CENTROID;
                    break;
                case "LH":
                    representationSchemeType = RepresentationSchemeType.LAST_HEAVY_SIDE_CHAIN;
                    break;
                case "SC":
                    representationSchemeType = RepresentationSchemeType.SIDE_CHAIN_CENTROID;
                    break;
                default:
                    throw new Fit3DCommandLineException("Representation scheme specification is invalid.");
            }
        }
    }

    /**
     * Assigns the defined position-specific exchanges to the query {@link StructuralMotif}.
     *
     * @throws Fit3DCommandLineException If exchange definition fails.
     */
    private void assignExchanges() throws Fit3DCommandLineException {
        if (commandLine.hasOption('e')) {
            // check each exchange definition for validity
            for (String exchangeDefinition : commandLine.getOptionValue('e').split(",")) {
                String[] splittedExchangeDefinition = exchangeDefinition.split(":");
                if (splittedExchangeDefinition.length != 2) {
                    throw new Fit3DCommandLineException("Exchange definition " + exchangeDefinition + " is malformed.");
                }
                LeafIdentifier leafIdentifier;
                try {
                    leafIdentifier = LeafIdentifier.fromSimpleString(splittedExchangeDefinition[0]);
                } catch (NumberFormatException e) {
                    throw new Fit3DCommandLineException("Exchange definition " + exchangeDefinition + " is malformed.");
                }
                char[] residueOneLetterCodes = splittedExchangeDefinition[1].toCharArray();
                for (char residueOneLetterCode : residueOneLetterCodes) {
                    Optional<AminoAcidFamily> exchangeableFamilyOptional = AminoAcidFamily.getAminoAcidTypeByOneLetterCode(String.valueOf(residueOneLetterCode));
                    if (exchangeableFamilyOptional.isPresent()) {
                        // add exchangeable type to motif residue
                        try {
                            queryMotif.addExchangeableFamily(leafIdentifier, exchangeableFamilyOptional.get());
                            logger.debug("added exchange definition {}", exchangeDefinition);
                        } catch (NoSuchElementException e) {
                            throw new Fit3DCommandLineException("Exchange definition " + exchangeDefinition + " is not a valid amino acid that occurs in the motif.");
                        }
                    } else {
                        throw new Fit3DCommandLineException("Exchange definition " + exchangeDefinition + " is not a valid amino acid one-letter code.");
                    }
                }
            }
        }
    }

    public Predicate<Atom> getAtomFilter() {
        return atomFilter;
    }

    public Double getDistanceTolerance() {
        return distanceTolerance;
    }

    public ItemsetMinerConfiguration<String> getItemsetMinerConfiguration() {
        return itemsetMinerConfiguration;
    }

    public StructureParser.LocalPDB getLocalPdb() {
        return localPdb;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public Path getOutputDirectoryPath() {
        return outputDirectoryPath;
    }

    public StructuralMotif getQueryMotif() {
        return queryMotif;
    }

    public RepresentationSchemeType getRepresentationSchemeType() {
        return representationSchemeType;
    }

    public Path getResultFilePath() {
        return resultFilePath;
    }

    public double getRmsd() {
        return rmsd;
    }

    public StatisticalModel getStatisticalModel() {
        return statisticalModel;
    }

    public String getTarget() {
        return target;
    }

    public Path getTargetListPath() {
        return targetListPath;
    }

    public boolean isEcMapping() {
        return ecMapping;
    }

    public boolean isMmtf() {
        return mmtf;
    }

    public boolean isPfamMapping() {
        return pfamMapping;
    }

    public boolean isUniProtMapping() {
        return uniProtMapping;
    }

    private enum Fit3DMode {

        TEMPLATE_BASED("template-based"),
        TEMPLATE_FREE("template-free");

        private final String commandLineFlag;

        Fit3DMode(String commandLineFlag) {
            this.commandLineFlag = commandLineFlag;
        }

        public String getCommandLineFlag() {
            return commandLineFlag;
        }
    }
}
