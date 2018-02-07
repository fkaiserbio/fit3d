package bio.fkaiser.fit3d.web.model.constant;

import bio.fkaiser.fit3d.web.beans.session.SessionManager;

import java.nio.file.Path;

public enum PredefinedList {

    NONE(null, "none"),
    BLASTe7(SessionManager.BASE_PATH.resolve("nrpdb_041416_BLAST_e-7.txt"), "BLASTe-7"),
    BLASTe40(SessionManager.BASE_PATH.resolve("nrpdb_041416_BLAST_e-40.txt"), "BLASTe-40"),
    BLASTe80(SessionManager.BASE_PATH.resolve("nrpdb_041416_BLAST_e-80.txt"), "BLASTe-80"),
    TEST(SessionManager.BASE_PATH.resolve("test.txt"), "test");

    private Path path;
    private String label;

    PredefinedList(Path path, String label) {
        this.path = path;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Path getPath() {
        return path;
    }
}
