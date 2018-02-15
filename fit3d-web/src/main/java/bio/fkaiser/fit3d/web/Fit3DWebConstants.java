package bio.fkaiser.fit3d.web;

import bio.fkaiser.fit3d.web.model.constant.StatisticalModelType;
import de.bioforscher.singa.core.utility.Resources;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter;
import de.bioforscher.singa.structure.parser.pdb.structures.SourceLocation;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public final class Fit3DWebConstants {

    public static final int CORES = Runtime.getRuntime().availableProcessors();
    public static final int THREAD_POOL_SIZE = 2;
    public static final int JOB_LIMIT = 5;
    public static final int MAXIMAL_UPLOAD_SIZE = 1000000;
    public static final double STRUCTURE_OUTPUT_RMSD_LIMIT = 4.0;
    public static final int ALL_AGAINST_ONE_LIMIT = 1000;

    public static final StructureParser.LocalPDB LOCAL_PDB = new StructureParser.LocalPDB("/srv/pdb/", SourceLocation.OFFLINE_PDB);

    public static final int EXCHANGE_LIMIT = 3;

    public final static class DefaultJobParameters {
        public static final StructuralEntityFilter.AtomFilterType DEFAULT_ATOM_FILTER_TYPE = StructuralEntityFilter.AtomFilterType.ARBITRARY;
        public static final double DEFAULT_RMSD_LIMIT = 2.0;
        public static final StatisticalModelType DEFAULT_STATISTICAL_MODEL_TYPE = StatisticalModelType.FOFANOV;
    }

    public final static class Database {
        public static final String DB_HOST = "fit3d-web-mongodb";
        public static final int DB_PORT = 27017;
        public static final String DB_NAME = "fit3d";
        public static final String DB_COLLECTION_NAME = "jobs";
        public static final boolean DROP_DB_ON_RESTART = true;
    }

    public final static class Singa {
        public static final StructureParserOptions STRUCTURE_PARSER_OPTIONS = StructureParserOptions.withSettings(StructureParserOptions.Setting.OMIT_HYDROGENS,
                                                                                                                  StructureParserOptions.Setting.GET_IDENTIFIER_FROM_FILENAME);
    }

    public final static class JobManager {
        public static final long LOAD_UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(1);
        public static final long CLEANUP_INTERVAL = TimeUnit.MINUTES.toMillis(1);
        public static final int JOB_AGE_IN_HOURS = 3;
    }

    public final static class Mail {

        public static String SMTP_PASS;
        public static String SMTP_USER;

        static {
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
