package de.bioforscher.fit3d.web.utilities;

import java.io.Serializable;
import java.util.List;

/**
 * A class representing a position specific exchange (PSE) of a motif amino
 * acid.
 * 
 * @author fkaiser
 *
 */
public class ExchangeDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1961478305493067940L;

	private Integer resNum;

	private char aminoAcidType;

	private List<String> exchangeAminoAcids;

	public ExchangeDefinition(Integer resNum, char aminoAcidType) {
		super();
		this.resNum = resNum;
		this.aminoAcidType = aminoAcidType;
	}

	public char getAminoAcidType() {
		return this.aminoAcidType;
	}

	public List<String> getExchangeAminoAcids() {
		return this.exchangeAminoAcids;
	}

	public Integer getResNum() {
		return this.resNum;
	}

	public void setAminoAcidType(char aminoAcidType) {
		this.aminoAcidType = aminoAcidType;
	}

	public void setExchangeAminoAcids(List<String> exchangeAminoAcids) {
		this.exchangeAminoAcids = exchangeAminoAcids;
	}

	public void setResNum(Integer resNum) {
		this.resNum = resNum;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(this.resNum);
		sb.append(":");

		if (this.exchangeAminoAcids != null) {

			for (String s : this.exchangeAminoAcids) {

				sb.append(s);
			}
		}

		return sb.toString();
	}
}
