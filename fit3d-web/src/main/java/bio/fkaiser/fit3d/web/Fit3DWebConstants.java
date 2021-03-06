package bio.fkaiser.fit3d.web;

import bio.fkaiser.fit3d.web.model.constant.StatisticalModelType;
import bio.singa.core.utility.Resources;
import bio.singa.structure.model.oak.StructuralEntityFilter.AtomFilterType;
import bio.singa.structure.parser.pdb.structures.SourceLocation;
import bio.singa.structure.parser.pdb.structures.StructureParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final StructureParser.LocalPDB LOCAL_PDB = new StructureParser.LocalPDB("/srv/pdb/", SourceLocation.OFFLINE_MMTF);

    public static final int EXCHANGE_LIMIT = 3;

    public final static class DefaultJobParameters {
        public static final AtomFilterType DEFAULT_ATOM_FILTER_TYPE = AtomFilterType.ARBITRARY;
        public static final Set<AtomFilterType> ENABLED_ATOM_FILTERS = Stream.of(AtomFilterType.ALPHA_CARBON, AtomFilterType.ARBITRARY, AtomFilterType.BACKBONE, AtomFilterType.SIDE_CHAIN)
                                                                             .collect(Collectors.toSet());
        public static final double DEFAULT_RMSD_LIMIT = 2.0;
        public static final double DEFAULT_FILTER_THRESHOLD = 8.0;
        public static final StatisticalModelType DEFAULT_STATISTICAL_MODEL_TYPE = StatisticalModelType.FOFANOV;
    }

    public final static class Database {
        public static final String DB_HOST = "fit3d-web-mongodb";
        //        public static final String DB_HOST = "localhost";
        public static final int DB_PORT = 27017;
        public static final String DB_NAME = "fit3d";
        public static final String DB_COLLECTION_NAME = "jobs";
        public static final boolean DROP_DB_ON_RESTART = false;
    }

    public final static class JobManager {
        public static final long LOAD_UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(1);
        public static final long CLEANUP_INTERVAL = TimeUnit.HOURS.toMillis(1);
        public static final int JOB_AGE_IN_HOURS = 72;
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
