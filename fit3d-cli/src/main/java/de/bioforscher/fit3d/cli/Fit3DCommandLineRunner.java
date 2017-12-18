package de.bioforscher.fit3d.cli;

import de.bioforscher.singa.structure.model.interfaces.LeafSubstructureContainer;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fk
 */
public class Fit3DCommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Fit3DCommandLineRunner.class);

    private final Fit3DCommandLine commandLine;

    public Fit3DCommandLineRunner(Fit3DCommandLine commandLine) throws Fit3DCommandLineException {
        this.commandLine = commandLine;
        run();
    }

    private void run() throws Fit3DCommandLineException {

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
                Pattern identifierPattern = Pattern.compile("([1-9a-zA-Z][0-9a-zA-Z]{3})(_[\\w]+)*");
                Matcher matcher = identifierPattern.matcher(commandLine.getTarget());
                matcher.find();
//                if (!matcher.matches()) {
//                    throw new Fit3DCommandLineException("Target is not a valid PDB identifier.");
//                }
                String pdbIdentifier = matcher.group(1);
                Optional<String> chainIdentifierOptional = Optional.empty();
                try {
                    String group = matcher.group(2);
                    if(group != null){
                        group = group.substring(1);
                    }
                    chainIdentifierOptional = Optional.ofNullable(group);
                    if (chainIdentifierOptional.isPresent()) {
                        targetStructure = StructureParser.online()
                                                         .pdbIdentifier(pdbIdentifier)
                                                         .chainIdentifier(chainIdentifierOptional.get())
                                                         .parse().getFirstChain();
                    } else {
                        targetStructure = StructureParser.online()
                                                         .pdbIdentifier(pdbIdentifier)
                                                         .parse()
                                                         .getFirstModel();
                    }
                } catch (UncheckedIOException e) {
                    throw new Fit3DCommandLineException("Failed to parse target structure.");
                }
            }
            logger.info("target structure contains {} residues", targetStructure.getAllLeafSubstructures().size());
        }
    }
}
