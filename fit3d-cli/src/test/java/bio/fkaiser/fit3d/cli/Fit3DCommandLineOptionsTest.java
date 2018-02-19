package bio.fkaiser.fit3d.cli;

import de.bioforscher.singa.core.utility.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

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
    public void failWithInvalidRmsdCutoff() throws IOException {

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

        String[] commandLineArguments = new String[]{"-m",
                                                     folder.getRoot().toString() + "/" + MOTIF_RESOURCE,
                                                     "-l",
                                                     folder.getRoot().toString() + "/" + CHAIN_LIST_RESOURCE,
                                                     "-f",
                                                     folder.getRoot().toString() + "/summary.csv"};
        Fit3DCommandLine.main(commandLineArguments);
        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    public void runWithTargetFile() throws IOException {

        copyResource(MOTIF_RESOURCE);
        copyResource(TARGET_FILE_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE,
                                                     "-t", folder.getRoot().toString() + "/" + TARGET_FILE_RESOURCE,
                                                     "-f", folder.getRoot().toString() + "/summary.csv"};
        Fit3DCommandLine.main(commandLineArguments);
        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    public void runWithTargetIdentifier() throws IOException {

        copyResource(MOTIF_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE,
                                                     "-t", "4cha",
                                                     "-f", folder.getRoot().toString() + "/summary.csv"};
        Fit3DCommandLine.main(commandLineArguments);
        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    public void runWithTargetIdentifierWithChain() throws IOException {

        copyResource(MOTIF_RESOURCE);

        String[] commandLineArguments = new String[]{"-m", folder.getRoot().toString() + "/" + MOTIF_RESOURCE,
                                                     "-t", "4cha_B",
                                                     "-f", folder.getRoot().toString() + "/summary.csv"};
        Fit3DCommandLine.main(commandLineArguments);
        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    public void runWithLocalPdb() throws IOException {

        copyResource(MOTIF_RESOURCE);
        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"-m",
                                                     folder.getRoot().toString() + "/" + MOTIF_RESOURCE,
                                                     "-l",
                                                     folder.getRoot().toString() + "/" + CHAIN_LIST_RESOURCE,
                                                     "-f",
                                                     folder.getRoot().toString() + "/summary.csv",
                                                     "-p",
                                                     "/srv/pdb"};
        Fit3DCommandLine.main(commandLineArguments);

    }

    @Test
    public void runWithLocalMmtf() throws IOException {

        copyResource(MOTIF_RESOURCE);
        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"-m",
                                                     folder.getRoot().toString() + "/" + MOTIF_RESOURCE,
                                                     "-l",
                                                     folder.getRoot().toString() + "/" + CHAIN_LIST_RESOURCE,
                                                     "-f",
                                                     folder.getRoot().toString() + "/summary.csv",
                                                     "-p",
                                                     "/srv/pdb",
                                                     "-F"};
        Fit3DCommandLine.main(commandLineArguments);

    }
}