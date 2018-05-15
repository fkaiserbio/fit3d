package bio.fkaiser.fit3d.cli;


import bio.fkaiser.mmm.ItemsetMinerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            logger.info("running template-free detection");
            ItemsetMinerRunner itemsetMinerRunner = new ItemsetMinerRunner(commandLine.getItemsetMinerConfiguration());
            logger.info("finished, found {} significant itemsets backing structural motifs", itemsetMinerRunner.getItemsetMiner().getTotalItemsets().size());
        } catch (Exception e) {
            throw new Fit3DCommandLineException("Failed to run Fit3D in template-free mode: " + e.getMessage());
        }
    }
}
