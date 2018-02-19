package bio.fkaiser.fit3d.cli;

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
import de.bioforscher.singa.structure.parser.pdb.structures.SourceLocation;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class Fit3DCommandLine {

    public static final double DEFAULT_RMSD_CUTOFF = 2.0;
    public static final double DEFAULT_DISTANCE_TOLERANCE = 1.0;
    public static final Predicate<Atom> DEFAULT_ATOM_FILTER = StructuralEntityFilter.AtomFilter.isArbitrary();

    public static final int DEFAULT_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    private static final Logger logger = LoggerFactory.getLogger(Fit3DCommandLine.class);
    private final CommandLine commandLine;
    private StructuralMotif queryMotif;

    private Predicate<Atom> atomFilter;
    private RepresentationSchemeType representationSchemeType;
    private double rmsd = DEFAULT_RMSD_CUTOFF;
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

    public Fit3DCommandLine(CommandLine commandLine) throws ParseException {
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

        // create instance of command line options
        Fit3DCommandLineOptions commandLineOptions = Fit3DCommandLineOptions.create();

        // get Fit3D options
        Options options = commandLineOptions.getOptions();
        // add input option group
        options.addOptionGroup(commandLineOptions.getInputOptions());
        // add target option group
        options.addOptionGroup(commandLineOptions.getTargetOptions());
        // add representation option group
        options.addOptionGroup(commandLineOptions.getRepresentationOptions());

        DefaultParser defaultParser = new DefaultParser();
        try {
            CommandLine commandLine = defaultParser.parse(options, args);
            new Fit3DCommandLine(commandLine);
        } catch (ParseException e) {
            logger.error("Command line options are invalid.", e);
            HelpFormatter help = new HelpFormatter();
            help.setWidth(256);
            help.setSyntaxPrefix("usage: ");
            help.printHelp("java -jar Fit3D.jar -m <arg> [-t <arg> | -l <arg>] [OPTIONS]\n", options);
            System.out.println("\n* = required");
            System.out.println("** = one of these required");
        }
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
        new Fit3DCommandLineRunner(this);
    }

    private void initializeParameters() throws ParseException, Fit3DCommandLineException {

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

        // set custom atom filter (default: all non-hydrogen atoms)
        atomFilter = AtomFilter.isHydrogen().negate();
        try {
            createRepresentation();
        } catch (Fit3DCommandLineException e) {
            logger.error("failed to define custom atoms", e);
            throw new Fit3DCommandLineException("Initialization of parameters failed.");
        }

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
                    throw new Fit3DCommandLineException("Representation scheme specification is invalid");
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
}
