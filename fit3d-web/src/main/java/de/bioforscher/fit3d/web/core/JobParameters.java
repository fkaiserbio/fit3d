package de.bioforscher.fit3d.web.core;

import java.io.Serializable;
import java.util.List;

import de.bioforscher.fit3d.web.utilities.ExchangeDefinition;
import de.bioforscher.fit3d.web.utilities.enums.AtomSelection;
import de.bioforscher.fit3d.web.utilities.enums.PredefinedList;
import de.bioforscher.fit3d.web.utilities.enums.PvalueMethod;

public class JobParameters implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PvalueMethod pvalueMethod;
	private AtomSelection atomSelection;
	private String motifName;
	private PredefinedList predefinedList;
	private double lrmsdLimit;
	private boolean filtering;
	private List<ExchangeDefinition> exchangeDefinitions;
	private String userdefinedList;
	private String extractPdbFilePath;
	private String targetListFileLabel;

	public JobParameters(String motifName, AtomSelection atomSelection, PvalueMethod pvalueMethod,
			PredefinedList predefinedList, double lrmsdLimit, boolean filtering,
			List<ExchangeDefinition> exchangeDefinitions, String userdefinedList, String extractPdbFilePath,
			String targetListFileLabel) {

		this.motifName = motifName;
		this.atomSelection = atomSelection;
		this.pvalueMethod = pvalueMethod;
		this.predefinedList = predefinedList;
		this.lrmsdLimit = lrmsdLimit;
		this.filtering = filtering;
		this.exchangeDefinitions = exchangeDefinitions;
		this.userdefinedList = userdefinedList;
		this.extractPdbFilePath = extractPdbFilePath;
		this.targetListFileLabel = targetListFileLabel;

	}

	public AtomSelection getAtomSelection() {
		return this.atomSelection;
	}

	public List<ExchangeDefinition> getExchangeDefinitions() {
		return this.exchangeDefinitions;
	}

	public String getExtractPdbFilePath() {
		return this.extractPdbFilePath;
	}

	public double getLrmsdLimit() {
		return this.lrmsdLimit;
	}

	public String getMotifName() {
		return this.motifName;
	}

	public PredefinedList getPredefinedList() {
		return this.predefinedList;
	}

	public PvalueMethod getPvalueMethod() {
		return this.pvalueMethod;
	}

	public String getTargetListFileLabel() {
		return this.targetListFileLabel;
	}

	public String getUserdefinedList() {
		return this.userdefinedList;
	}

	public boolean isFiltering() {
		return this.filtering;
	}
}
