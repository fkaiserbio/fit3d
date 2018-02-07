package bio.fkaiser.fit3d.web.beans.application;

import bio.fkaiser.fit3d.web.model.constant.PredefinedList;
import bio.fkaiser.fit3d.web.model.constant.StatisticalModelType;
import de.bioforscher.singa.structure.model.families.AminoAcidFamily;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter.AtomFilterType;

import java.util.stream.Stream;

public class DataProvider {

    public AminoAcidFamily[] getAminoAcidFamilies() {
        return Stream.of(AminoAcidFamily.values())
                     .toArray(AminoAcidFamily[]::new);
    }

    public AtomFilterType[] getAtomFilterTypes() {
        return AtomFilterType.values();
    }

    public PredefinedList[] getPredefinedLists() {
        return PredefinedList.values();
    }

    public StatisticalModelType[] getStatisticalModelTypes() {
        return Stream.of(StatisticalModelType.values())
                     .toArray(StatisticalModelType[]::new);
    }
}
