package de.bioforscher.fit3d.web.utilities;

public final class Fit3dConstants {

	public static final int JOB_LIMIT = 5;

	public static final String PDB_DIR = "/var/pdb/";
	// public static final String PDB_DIR = "/opt/pdb/";

	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

	// public static final String FIT3D_LOCATION =
	// "/home/fkaiser/Workspace/git/fit3d/de.bioforscher.fit3d.webserver/WebContent/WEB-INF/lib/Fit3D.jar";
	public static final String FIT3D_LOCATION = "/var/lib/tomcat7/webapps/fit3d/WEB-INF/lib/Fit3D.jar";

	public static final String SMTP_PASS = "sRp7ctnf";
	public static final String SMTP_USER = "bigm";

	public static final int EXCHANGE_LIMIT = 3;

	public static final int COMPLEXITY_LOWER_BOUND = 100;

	public static final int COMPLEXTY_UPPER_BOUND = 1100;
}
