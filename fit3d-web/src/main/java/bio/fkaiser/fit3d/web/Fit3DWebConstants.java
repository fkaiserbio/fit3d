package bio.fkaiser.fit3d.web;

import bio.fkaiser.fit3d.web.model.constant.StatisticalModelType;
import de.bioforscher.singa.core.utility.Resources;
import de.bioforscher.singa.structure.parser.pdb.structures.SourceLocation;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public final class Fit3DWebConstants {

    public static int CORES = Runtime.getRuntime().availableProcessors();
    public static int THREAD_POOL_SIZE = 2;

    public static int JOB_LIMIT = 5;
    public static int MAXIMAL_UPLOAD_SIZE = 1000000;
    public static double STRUCTURE_OUTPUT_RMSD_LIMIT = 4.0;
    public static int ALL_AGAINST_ONE_LIMIT = 1000;


    public static StructureParser.LocalPDB LOCAL_PDB = new StructureParser.LocalPDB("/srv/pdb/", SourceLocation.OFFLINE_PDB);

    public static String TMP_DIR = System.getProperty("java.io.tmpdir");

    // public static final String FIT3D_LOCATION =
    // "/home/fkaiser/Workspace/git/fit3d/de.bioforscher.fit3d.webserver/WebContent/WEB-INF/lib/Fit3D.jar";
    public static String FIT3D_LOCATION = "/var/lib/tomcat7/webapps/fit3d/WEB-INF/lib/Fit3D.jar";


    public static int EXCHANGE_LIMIT = 3;

    public final static class DefaultJobParameters {
        public static double DEFAULT_RMSD_LIMIT = 2.0;
        public static StatisticalModelType DEFAULT_STATISTICAL_MODEL_TYPE = StatisticalModelType.FOFANOV;
    }

    public final static class Database {
        public static String DB_HOST = "localhost";
        public static int DB_PORT = 27017;
        public static String DB_NAME = "fit3d";
        public static String DB_COLLECTION_NAME = "jobs";
        public static boolean DROP_DB_ON_RESTART = true;
    }

    public final static class Mail {
        public static String SMTP_PASS;
        public static String SMTP_USER;
        {
            try {
                String credentialContent = Files.lines(Paths.get(Resources.getResourceAsFileLocation("mail_credentials.txt"))).collect(Collectors.joining(""));
                SMTP_USER = credentialContent.split(":")[0];
                SMTP_PASS = credentialContent.split(":")[1];
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
