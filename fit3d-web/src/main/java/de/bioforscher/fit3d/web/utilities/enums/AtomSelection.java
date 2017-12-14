package de.bioforscher.fit3d.web.utilities.enums;

public enum AtomSelection {

	ALL("", "all non-hydrogen"), CA("CA", "C&alpha; only"), BACKBONE(
			"N,CA,C,O", "backbone"), SIDECHAIN(
			"ND,ND1,ND2,ND3,NE,NE1,NE2,NE3,NH,NH1,NH2,NZ,CB,CG,CG1,CG2,CG3,CD,CD1,CD2,CD3,CE,CE1,CE2,CE3,CZ,CZ1,CZ2,CZ3,CH,CH1,CH2,CH3,OD,OD1,OD2,OD3,OE,OE1,OE2,OE3,OG,OG1,OG2,OG3,OH,S,SA,SB,SD,SG",
			"sidechain");

	private String atoms;
	private String label;

	private AtomSelection(String atoms, String label) {

		this.atoms = atoms;
		this.label = label;
	}

	public String getAtoms() {

		return this.atoms;
	}

	public String getLabel() {

		return this.label;
	}
}
