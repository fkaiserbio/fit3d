package de.bioforscher.fit3d.web.utilities.enums;

public enum PvalueMethod {

	FOFANOV("F", "Fofanov et al. 2008"), STARK("S", "Stark et al. 2003");

	private String label;
	private String commandlineOption;

	private PvalueMethod(String commandlineOption, String label) {

		this.commandlineOption = commandlineOption;
		this.label = label;
	}

	public String getCommandlineOption() {
		return this.commandlineOption;
	}

	public String getLabel() {
		return this.label;
	}
}
