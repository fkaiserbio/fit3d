package bio.fkaiser.fit3d.cli;

import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3D;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DBuilder;
import de.bioforscher.singa.structure.model.identifiers.PDBIdentifier;
import de.bioforscher.singa.structure.model.interfaces.Atom;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructureContainer;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class Fit3DCommandLineRunner {

    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("([1-9a-zA-Z][0-9a-zA-Z]{3})(_[\\w]+)*");


    private static final Logger logger = LoggerFactory.getLogger(Fit3DCommandLineRunner.class);
    private static final StructureParserOptions STRUCTURE_PARSER_SETTINGS = StructureParserOptions.withSettings(StructureParserOptions.Setting.OMIT_HYDROGENS,
                                                                                                                StructureParserOptions.Setting.OMIT_LIGAND_INFORMATION);
    private final Fit3DCommandLine commandLine;
    private Fit3D fit3d;

    public Fit3DCommandLineRunner(Fit3DCommandLine commandLine) throws Fit3DCommandLineException {
        this.commandLine = commandLine;
        run();
    }

    private void run() throws Fit3DCommandLineException {

        Fit3DBuilder.AtomStep atomStep = null;
        if (commandLine.getTarget() != null) {

            Path targetPath = Paths.get(commandLine.getTarget());

            LeafSubstructureContainer targetStructure;
            if (targetPath.toFile().exists()) {
                logger.info("specified target {} is file", targetPath);
                targetStructure = StructureParser.local()
                                                 .path(targetPath)
                                                 .everything()
                                                 .parse();
            } else {
                logger.info("specified target {} is identifier", commandLine.getTarget());
                Matcher matcher = IDENTIFIER_PATTERN.matcher(commandLine.getTarget());
                if (!matcher.matches()) {
                    throw new Fit3DCommandLineException("Target is not a valid PDB identifier.");
                }
                String pdbIdentifier = matcher.group(1);
                Optional<String> chainIdentifierOptional;
                try {
                    String group = matcher.group(2);
                    if (group != null) {
                        group = group.substring(1);
                    }
                    chainIdentifierOptional = Optional.ofNullable(group);
                    if (chainIdentifierOptional.isPresent()) {
                        targetStructure = StructureParser.pdb()
                                                         .pdbIdentifier(pdbIdentifier)
                                                         .chainIdentifier(chainIdentifierOptional.get())
                                                         .parse().getFirstChain();
                    } else {
                        targetStructure = StructureParser.pdb()
                                                         .pdbIdentifier(pdbIdentifier)
                                                         .parse()
                                                         .getFirstModel();
                    }
                } catch (UncheckedIOException e) {
                    throw new Fit3DCommandLineException("Failed to parse target structure.");
                }
            }

            atomStep = Fit3DBuilder.create()
                                   .query(commandLine.getQueryMotif())
                                   .target(targetStructure);

            logger.info("target structure contains {} residues", targetStructure.getAllLeafSubstructures().size());

        } else if (commandLine.getTargetListPath() != null) {

            try {

                StructureParser.MultiParser multiParser;

                // setup local PDB if provided
                if (commandLine.getLocalPdb() != null) {
                    multiParser = StructureParser.local()
                                                 .localPDB(commandLine.getLocalPdb())
                                                 .chainList(commandLine.getTargetListPath())
                                                 .setOptions(STRUCTURE_PARSER_SETTINGS);
                } else {
                    // otherwise use online MMTF
                    multiParser = StructureParser.mmtf()
                                                 .chainList(commandLine.getTargetListPath())
                                                 .setOptions(STRUCTURE_PARSER_SETTINGS);
                }

                atomStep = Fit3DBuilder.create()
                                       .query(commandLine.getQueryMotif())
                                       .targets(multiParser)
                                       .maximalParallelism();

            } catch (UncheckedIOException e) {
                throw new Fit3DCommandLineException("Failed to read provided target list.");
            }
        }

        Fit3DBuilder.ParameterStep parameterStep;

        // setup atom step
        if (commandLine.getAtomFilter() != null) {
            parameterStep = atomStep.atomFilter(commandLine.getAtomFilter());
        } else if (commandLine.getRepresentationSchemeType() != null) {
            parameterStep = atomStep.representationScheme(commandLine.getRepresentationSchemeType());
        } else {
            parameterStep = atomStep.atomFilter(Fit3DCommandLine.DEFAULT_ATOM_FILTER);
        }

        // set statistical model
        if (commandLine.getStatisticalModel() != null) {
            parameterStep.statisticalModel(commandLine.getStatisticalModel());
        }

        fit3d = parameterStep.rmsdCutoff(commandLine.getRmsd())
                             .run();
    }
}
