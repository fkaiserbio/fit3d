package bio.fkaiser.fit3d.cli;


import bio.fkaiser.mmm.ItemsetMinerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author fk
 */
public class Fit3DTemplateFreeCommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Fit3DTemplateFreeCommandLineRunner.class);

    private final Fit3DCommandLine commandLine;

    public Fit3DTemplateFreeCommandLineRunner(Fit3DCommandLine commandLine) throws Fit3DCommandLineException {
        this.commandLine = commandLine;
        run();
    }

    private void run() throws Fit3DCommandLineException {
        try {
            new ItemsetMinerRunner(commandLine.getItemsetMinerConfiguration());
        } catch (IOException | URISyntaxException e) {
            logger.error("an error occurred during template-free detection", e);
            throw new Fit3DCommandLineException("Failed to run Fit3D in template-free mode.");
        }
    }
}
