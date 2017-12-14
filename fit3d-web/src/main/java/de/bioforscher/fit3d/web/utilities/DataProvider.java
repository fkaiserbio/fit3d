package de.bioforscher.fit3d.web.utilities;

import de.bioforscher.fit3d.web.utilities.enums.AtomSelection;
import de.bioforscher.fit3d.web.utilities.enums.PredefinedList;
import de.bioforscher.fit3d.web.utilities.enums.PvalueMethod;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;

public class DataProvider {

	public AtomSelection[] getAtomSelections() {

		return AtomSelection.values();
	}

	public String getHitFileName(Fit3DMatch match) {

		return match.getSubstructureSuperimposition().getCandidate().toString();

	}

	public PredefinedList[] getPredefinedLists() {

		return PredefinedList.values();
	}

	public PvalueMethod[] getPvalueMethods() {

		return PvalueMethod.values();
	}

	// TODO bind enum
	public String[] getSingleLetterCodes() {

		return new String[] { "A", "R", "N", "D", "C", "Q", "E", "G", "H", "I", "L", "K", "M", "F", "P", "S", "T", "W",
				"Y", "V" };
	}

}
