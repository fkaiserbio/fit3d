package bio.fkaiser.fit3d.cli;

import de.bioforscher.singa.core.utility.Resources;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author fk
 */
public class TemplateFreeCommandLineOptionsTest {

    public static final String CONFIGURATION_RESOURCE = "configuration.json";
    public static final String CHAIN_LIST_RESOURCE = "craven2016_WSXWS_motif.txt";
    public static final String STRUCTURE_DIRECTORY = "PF00127";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void copyResourceDirectory(String resourceName) {
        Path resourcePath = Paths.get(Resources.getResourceAsFileLocation(resourceName));
        try {
            Files.walk(resourcePath)
                 .forEach(s ->
                          {
                              try {
                                  Path directory = folder.getRoot().toPath().resolve(resourceName).resolve(resourcePath.relativize(s));
                                  if (Files.isDirectory(s)) {
                                      if (!Files.exists(directory))
                                          Files.createDirectory(directory);
                                      return;
                                  }
                                  Files.copy(s, directory);
                              } catch (Exception e) {
                                  e.printStackTrace();
                              }
                          });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void copyResource(String resourceName) throws IOException {
        String motifLocation = Resources.getResourceAsFileLocation(resourceName);
        Files.copy(Paths.get(motifLocation), folder.getRoot().toPath().resolve(resourceName));
    }

    @Test
    public void failWithoutOutputDirectory() {
        String[] commandLineArguments = new String[]{"template-free",
                                                     "-t", "1ten.A"};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    @Ignore("only works if plip_credentials.txt file is present in classpath")
    public void failWithMmtfAndInteractions() {
        String[] commandLineArguments = new String[]{"template-free",
                                                     "-t", "1ten.A",
                                                     "-o", folder.getRoot().toString(),
                                                     "-i",
                                                     "-F"};
        Fit3DCommandLine.main(commandLineArguments);
    }

    @Test
    public void runWithSingleChainInput() {
        String[] commandLineArguments = new String[]{"template-free",
                                                     "-t", "1ten.A",
                                                     "-o", folder.getRoot().toString(),
                                                     "-r", "40"};
        Fit3DCommandLine.main(commandLineArguments);
        // FIXME does not work on Travis CI
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    public void runWithChainListInput() throws IOException {

        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"template-free",
                                                     "-l", folder.getRoot().toString() + "/" + CHAIN_LIST_RESOURCE,
                                                     "-o", folder.getRoot().toString(),
                                                     "-r", "40"};
        Fit3DCommandLine.main(commandLineArguments);
        // FIXME does not work on Travis CI
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    @Ignore("does only run when PLIP ")
    public void runWithChainListInputAndInteractions() throws IOException {

        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"template-free",
                                                     "-l", folder.getRoot().toString() + "/" + CHAIN_LIST_RESOURCE,
                                                     "-o", folder.getRoot().toString(),
                                                     "-r", "40",
                                                     "-i"};
        Fit3DCommandLine.main(commandLineArguments);
        // FIXME does not work on Travis CI
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    @Ignore("only works with local PDB installation in PDB format")
    public void runWithChainListInputAndLocalPDB() throws IOException {

        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"template-free",
                                                     "-l", folder.getRoot().toString() + "/" + CHAIN_LIST_RESOURCE,
                                                     "-o", folder.getRoot().toString(),
                                                     "-r", "40",
                                                     "-p", "/srv/pdb"};
        Fit3DCommandLine.main(commandLineArguments);
        // FIXME does not work on Travis CI
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    @Ignore("only works with local PDB installation in MMTF format")
    public void runWithChainListInputAndLocalMMTF() throws IOException {

        copyResource(CHAIN_LIST_RESOURCE);

        String[] commandLineArguments = new String[]{"template-free",
                                                     "-l", folder.getRoot().toString() + "/" + CHAIN_LIST_RESOURCE,
                                                     "-o", folder.getRoot().toString(),
                                                     "-r", "40",
                                                     "-p", "/srv/pdb",
                                                     "-F"};
        Fit3DCommandLine.main(commandLineArguments);
        // FIXME does not work on Travis CI
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }


    @Test
    public void runWithStructureDirectory() {

        copyResourceDirectory(STRUCTURE_DIRECTORY);

        String[] commandLineArguments = new String[]{"template-free",
                                                     "-d", folder.getRoot().toString() + "/" + STRUCTURE_DIRECTORY,
                                                     "-o", folder.getRoot().toString(),
                                                     "-r", "40",
                                                     "-n", "1gy2.A"};
        Fit3DCommandLine.main(commandLineArguments);
        // FIXME does not work on Travis CI
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    public void runWithStructureDirectoryAndMapping() {

        copyResourceDirectory(STRUCTURE_DIRECTORY);

        String[] commandLineArguments = new String[]{"template-free",
                                                     "-d", folder.getRoot().toString() + "/" + STRUCTURE_DIRECTORY,
                                                     "-o", folder.getRoot().toString(),
                                                     "-r", "40",
                                                     "-n", "1gy2.A",
                                                     "-m", "CG"};
        Fit3DCommandLine.main(commandLineArguments);
        // FIXME does not work on Travis CI
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());
    }

    @Test
    public void runWithConfigurationFile() {

        copyResourceDirectory(CONFIGURATION_RESOURCE);

        String[] commandLineArguments = new String[]{"template-free",
                                                     "-c", folder.getRoot().toString() + "/configuration.json",
                                                     "-o", folder.getRoot().toString()};
        Fit3DCommandLine.main(commandLineArguments);
//        assertTrue(new File(folder.getRoot().toString() + "/summary.csv").exists());

    }
}