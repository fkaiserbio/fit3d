package bio.fkaiser.fit3d.cli;

import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3D;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DBuilder;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructureContainer;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserOptions;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fk
 */
public class Fit3DTemplateBasedCommandLineRunner {

    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("([1-9a-zA-Z][0-9a-zA-Z]{3})(_[\\w]+)*");

    private static final Logger logger = LoggerFactory.getLogger(Fit3DTemplateBasedCommandLineRunner.class);
    private static final StructureParserOptions STRUCTURE_PARSER_SETTINGS = StructureParserOptions.withSettings(StructureParserOptions.Setting.OMIT_HYDROGENS,
                                                                                                                StructureParserOptions.Setting.OMIT_LIGAND_INFORMATION);
    private final Fit3DCommandLine commandLine;
    private Fit3D fit3d;
    private StructureParser.MultiParser multiParser;

    public Fit3DTemplateBasedCommandLineRunner(Fit3DCommandLine commandLine) throws Fit3DCommandLineException {
        this.commandLine = commandLine;
        run();
    }

    private void run() throws Fit3DCommandLineException {

        Fit3DBuilder.AtomStep atomStep;
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

        } else {

            // target list is used
            try {

                // setup local PDB if provided
                if (commandLine.getLocalPdb() != null) {
                    multiParser = StructureParser.local()
                                                 .localPDB(commandLine.getLocalPdb())
                                                 .chainList(commandLine.getTargetListPath())
                                                 .setOptions(STRUCTURE_PARSER_SETTINGS);
                } else {
                    // otherwise use online MMTF
                    StructureParser.IdentifierStep identifierStep;
                    if (commandLine.isMmtf()) {
                        identifierStep = StructureParser.mmtf();
                    } else {
                        identifierStep = StructureParser.pdb();
                    }
                    multiParser = identifierStep
                            .chainList(commandLine.getTargetListPath())
                            .setOptions(STRUCTURE_PARSER_SETTINGS);
                }
                atomStep = Fit3DBuilder.create()
                                       .query(commandLine.getQueryMotif())
                                       .targets(multiParser)
                                       .limitedParallelism(commandLine.getNumberOfThreads());

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

        // define mappings
        if (commandLine.isEcMapping()) {
            parameterStep.mapECNumbers();
        }
        if (commandLine.isPfamMapping()) {
            parameterStep.mapPfamIdentifiers();
        }
        if (commandLine.isUniProtMapping()) {
            parameterStep.mapUniProtIdentifiers();
        }

        Timer timer = null;
        ProgressTask progressTask = null;
        if (multiParser != null) {
            timer = new Timer(true);
            progressTask = new ProgressTask();
            timer.schedule(progressTask, 0, 100);
            logger.info("running Fit3D against {} target structures\n", multiParser.getNumberOfQueuedStructures());
        } else {
            logger.info("running against single target\n");
        }

        fit3d = parameterStep.rmsdCutoff(commandLine.getRmsd())
                             .distanceTolerance(commandLine.getDistanceTolerance())
                             .run();

        // ensure graceful termination of progress monitor
        if (timer != null) {
            timer.cancel();
            progressTask.run();
        }

        List<Fit3DMatch> matches = fit3d.getMatches();
        logger.info("found {} matches: ", matches.size());

        if (!matches.isEmpty()) {

            System.out.print("\n" + Fit3DMatch.CSV_HEADER);
            matches.stream()
                   .map(Fit3DMatch::toCsvLine)
                   .forEach(System.out::println);

            // write result file
            if (commandLine.getResultFilePath() != null) {
                logger.info("writing result file {}", commandLine.getResultFilePath());
                try {
                    fit3d.writeSummaryFile(commandLine.getResultFilePath());
                } catch (IOException e) {
                    throw new Fit3DCommandLineException("Failed to write result file to: '" + commandLine.getResultFilePath() + "' " + e.getMessage());
                }
            }

            // write result structures
            if (commandLine.getOutputDirectoryPath() != null) {
                logger.info("writing structures to {}", commandLine.getOutputDirectoryPath());
                fit3d.writeMatches(commandLine.getOutputDirectoryPath());
            }
        }
    }

    private class ProgressTask extends TimerTask {

        ProgressBar progressBar = new ProgressBar("Progress", multiParser.getNumberOfQueuedStructures(), ProgressBarStyle.ASCII);

        public ProgressTask() {
            progressBar.start();
        }

        @Override
        public void run() {
            if (multiParser != null) {
                progressBar.stepTo(multiParser.getNumberOfQueuedStructures() - multiParser.getNumberOfRemainingStructures());
                if (!multiParser.hasNext()) {
                    progressBar.stepTo(multiParser.getNumberOfQueuedStructures());
                    progressBar.stop();
                }
            }
        }
    }
}
