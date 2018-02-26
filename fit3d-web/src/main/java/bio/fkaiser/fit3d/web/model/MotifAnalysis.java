package bio.fkaiser.fit3d.web.model;

import bio.fkaiser.fit3d.web.model.constant.MotifComplexity;
import de.bioforscher.singa.structure.model.families.StructuralFamily;
import de.bioforscher.singa.structure.model.identifiers.LeafIdentifier;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import de.bioforscher.singa.structure.model.oak.Structures;

import java.util.stream.Collectors;

/**
 * TODO move to SiNGA?
 *
 * @author fk
 */
public class MotifAnalysis {

    private final int motifAminoAcidCount;
    private final double motifExtent;
    private final String motifSequence;
    private final StructuralMotif.Type motifType;
    private final MotifComplexity motifComplexity;
    private final String pdbIdentifier;
    private final boolean mixedMotif;

    public MotifAnalysis(int motifAminoAcidCount, double motifExtent, String motifSequence, StructuralMotif.Type motifType, MotifComplexity motifComplexity, String pdbIdentifier, boolean mixedMotif) {
        this.motifAminoAcidCount = motifAminoAcidCount;
        this.motifExtent = motifExtent;
        this.motifSequence = motifSequence;
        this.motifType = motifType;
        this.motifComplexity = motifComplexity;
        this.pdbIdentifier = pdbIdentifier;
        this.mixedMotif = mixedMotif;

    }

    public static MotifAnalysis of(StructuralMotif structuralMotif) {
        // count amino acids
        int motifAminoAcidCount = structuralMotif.size();
        // compute spatial extent
        double motifExtent = Structures.calculateExtent(structuralMotif);
        // determine motif sequence
        String motifSequence = structuralMotif.getAllLeafSubstructures().stream()
                                              .map(LeafSubstructure::getFamily)
                                              .map(StructuralFamily::getThreeLetterCode)
                                              .collect(Collectors.joining("-"));
        // get motif type
        StructuralMotif.Type motifType = StructuralMotif.Type.determine(structuralMotif);

        // calculate motif complexity based on rule set
        MotifComplexity motifComplexity;
        if (motifAminoAcidCount <= 3 && motifExtent <= 15.0) {
            motifComplexity = MotifComplexity.LOW;
        } else if (motifAminoAcidCount <= 3 && motifExtent >= 15.0) {
            motifComplexity = MotifComplexity.MEDIUM;
        } else if (motifAminoAcidCount >= 4 && motifExtent <= 15.0) {
            motifComplexity = MotifComplexity.MEDIUM;
        } else if (motifAminoAcidCount >= 4 && motifExtent >= 15.0) {
            motifComplexity = MotifComplexity.HIGH;
        } else if (motifAminoAcidCount == 5 && motifExtent <= 15.0) {
            motifComplexity = MotifComplexity.MEDIUM;
        } else if (motifAminoAcidCount >= 5) {
            motifComplexity = MotifComplexity.HIGH;
        } else {
            motifComplexity = MotifComplexity.HIGH;
        }

        String pdbIdentifier = "";
        if (!structuralMotif.getFirstLeafSubstructure().getPdbIdentifier().equals(LeafIdentifier.DEFAULT_PDB_IDENTIFIER)) {
            pdbIdentifier = structuralMotif.getFirstLeafSubstructure().getPdbIdentifier();
        }

        boolean mixedMotif = structuralMotif.getAllAminoAcids().size() != structuralMotif.size();

        return new MotifAnalysis(motifAminoAcidCount, motifExtent, motifSequence, motifType, motifComplexity, pdbIdentifier, mixedMotif);
    }

    public int getMotifAminoAcidCount() {
        return motifAminoAcidCount;
    }

    public MotifComplexity getMotifComplexity() {
        return motifComplexity;
    }

    public double getMotifExtent() {
        return motifExtent;
    }

    public String getMotifSequence() {
        return motifSequence;
    }

    public StructuralMotif.Type getMotifType() {
        return motifType;
    }

    public String getPdbIdentifier() {
        return pdbIdentifier;
    }

    public boolean isMixedMotif() {
        return mixedMotif;
    }
}
