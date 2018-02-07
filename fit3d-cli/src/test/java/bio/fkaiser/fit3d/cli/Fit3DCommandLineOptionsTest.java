package bio.fkaiser.fit3d.cli;

import de.bioforscher.singa.core.utility.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author fk
 */
public class Fit3DCommandLineOptionsTest {

    public static final String MOTIF_RESOURCE = "motif_HDS.pdb";
    public static final String TARGET_FILE_RESOURCE = "4cha.pdb";
    public static final String CHAIN_LIST_RESOURCE = "nrpdb_BLAST_10e80_100.txt";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void copyResource(String resourceName) throws IOException {
        String motifLocation = Resources.getResourceAsFileLocation(resourceName);
        Files.copy(Paths.get(motifLocation), folder.getRoot().toPath().resolve(resourceName));
    }

    @Before
    public void setUp() {

    }

    @Test
    public void failWithoutMotif() {
        String[] commandLineArguments = new String[]{"-t", CHAIN_LIST_RESOURCE};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void failWithoutTarget() {
        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void failWithoutInvalidRmsdCutoff() throws IOException {

        copyResource(MOTIF_RESOURCE);
        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE, "-l", CHAIN_LIST_RESOURCE, "-r", "2.11x"};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void failWithInvalidTargetIdentifier() throws IOException {

        copyResource(MOTIF_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE, "-t", "4444"};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void runWithMotifAndChainList() throws IOException {

        copyResource(MOTIF_RESOURCE);
        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE, "-l", CHAIN_LIST_RESOURCE};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void runWithTargetFile() throws IOException {

        copyResource(MOTIF_RESOURCE);
        copyResource(TARGET_FILE_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE, "-t", folder.getRoot().toString() + "/" + TARGET_FILE_RESOURCE};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void runWithTargetIdentifier() throws IOException {

        copyResource(MOTIF_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE, "-t", "4cha"};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void runWithTargetIdentifierWithChain() throws IOException {

        copyResource(MOTIF_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE, "-t", "4cha_B"};
        Fit3DCommandLine.main(commandLineArguments);
    }
}