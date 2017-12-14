package de.bioforscher.fit3d.web.utilities.enums;

/**
 * TODO fill the corresponding lists with data
 * 
 * Represents the predefined target lists.
 * 
 * @author fkaiser
 *
 */
public enum PredefinedList {

	NONE("", "none"), BLASTe7("data/nrpdb.032614_BLAST_pvalue_10e-7", "BLASTe-7"), BLASTe40(
			"data/nrpdb.032614_BLAST_pvalue_10e-40", "BLASTe-40"), BLASTe80("data/nrpdb.032614_BLAST_pvalue_10e-80",
					"BLASTe-80"), CATH("data/nrCATH_4.0_13339_entries",
							"nrCATH 4.0"), SCOP("data/nrSCOP_1.75_11526_entries", "nrSCOP 1.75");

	private String label;
	private String path;

	private PredefinedList(String path, String label) {

		this.path = path;
		this.label = label;
	}

	public String getLabel() {

		return this.label;
	}

	public String getPath() {

		return this.path;
	}
}
