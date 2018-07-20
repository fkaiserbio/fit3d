package bio.fkaiser.fit3d.web.model;

import bio.singa.structure.model.families.AminoAcidFamily;
import bio.singa.structure.model.identifiers.LeafIdentifier;
import bio.singa.structure.model.interfaces.AminoAcid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExchangeDefinition implements Serializable {

    private static final long serialVersionUID = 1961478305493067940L;
    private LeafIdentifier leafIdentifier;
    private AminoAcidFamily aminoAcidFamily;
    private List<AminoAcidFamily> exchangeAminoAcids;

    public ExchangeDefinition(AminoAcid aminoAcid) {
        leafIdentifier = aminoAcid.getIdentifier();
        aminoAcidFamily = aminoAcid.getFamily();
    }

    public ExchangeDefinition(LeafIdentifier leafIdentifier, AminoAcidFamily aminoAcidFamily, List<AminoAcidFamily> exchangeAminoAcids) {
        this.leafIdentifier = leafIdentifier;
        this.aminoAcidFamily = aminoAcidFamily;
        this.exchangeAminoAcids = exchangeAminoAcids;
    }

    public static ExchangeDefinition fromString(String exchangeDefinitionString) {
        String[] split = exchangeDefinitionString.split(":");
        LeafIdentifier leafIdentifier = LeafIdentifier.fromSimpleString(split[0].substring(0, split[0].lastIndexOf("-")));
        Optional<AminoAcidFamily> optionalAminoAcidFamily = AminoAcidFamily.getAminoAcidTypeByThreeLetterCode(split[0].split("-")[2]);
        AminoAcidFamily aminoAcidFamily = optionalAminoAcidFamily.orElse(AminoAcidFamily.UNKNOWN);
        if (split.length == 1) {
            return new ExchangeDefinition(leafIdentifier, aminoAcidFamily, new ArrayList<>());
        }
        List<AminoAcidFamily> exchangeAminoAcids = Stream.of(split[1].replaceAll("\\[", "").replaceAll("]", "").split(","))
                                                         .map(AminoAcidFamily::getAminoAcidTypeByThreeLetterCode)
                                                         .filter(Optional::isPresent)
                                                         .map(Optional::get)
                                                         .collect(Collectors.toList());
        return new ExchangeDefinition(leafIdentifier, aminoAcidFamily, exchangeAminoAcids);
    }

    @Override
    public String toString() {
        String exchangeString;
        if (exchangeAminoAcids != null && !exchangeAminoAcids.isEmpty()) {
            exchangeString = exchangeAminoAcids.stream()
                                               .map(AminoAcidFamily::getThreeLetterCode)
                                               .collect(Collectors.joining(",", "[", "]"));
        } else {
            exchangeString = "";
        }
        return getAminoAcidString() + ":" + exchangeString;
    }

    public AminoAcidFamily getAminoAcidFamily() {
        return aminoAcidFamily;
    }

    public String getAminoAcidString() {
        return leafIdentifier.toSimpleString() + "-" + aminoAcidFamily.getThreeLetterCode();
    }

    public List<AminoAcidFamily> getExchangeAminoAcids() {
        return exchangeAminoAcids;
    }

    public void setExchangeAminoAcids(List<AminoAcidFamily> exchangeAminoAcids) {
        this.exchangeAminoAcids = exchangeAminoAcids;
    }

    public LeafIdentifier getLeafIdentifier() {
        return leafIdentifier;
    }
}
