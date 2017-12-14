package de.bioforscher.fit3d.web.utilities.enums;

public enum MotifComplexity {

	LOW("low"), MEDIUM("medium"), HIGH("high");

	private String label;

	private MotifComplexity(String label) {

		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
