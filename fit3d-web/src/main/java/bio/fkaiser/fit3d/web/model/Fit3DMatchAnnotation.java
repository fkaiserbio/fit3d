package bio.fkaiser.fit3d.web.model;

import de.bioforscher.singa.core.identifier.ECNumber;
import de.bioforscher.singa.core.identifier.PfamIdentifier;
import de.bioforscher.singa.core.identifier.UniProtIdentifier;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;

import java.util.Map;
import java.util.Optional;

/**
 * @author fk
 */
public class Fit3DMatchAnnotation {

    private final Fit3DMatch match;
    private final String chainIdentifier;
    private String uniProtIdentifier;
    private String pfamIdentifier;
    private String ecIdentifier;
    public Fit3DMatchAnnotation(Fit3DMatch match, String chainIdentifier) {
        this.match = match;
        this.chainIdentifier = chainIdentifier;
        initialize();
    }

    private void initialize() {
        // UniProt
        Optional<Map<String, UniProtIdentifier>> optionalUniProtIdentifiers = match.getUniProtIdentifiers();
        if (optionalUniProtIdentifiers.isPresent()) {
            Map<String, UniProtIdentifier> uniProtIdentifierMap = optionalUniProtIdentifiers.get();
            if (uniProtIdentifierMap.containsKey(chainIdentifier)) {
                uniProtIdentifier = uniProtIdentifierMap.get(chainIdentifier).getIdentifier();
            }
        }
        // Pfam
        Optional<Map<String, PfamIdentifier>> optionalPfamIdentifiers = match.getPfamIdentifiers();
        if (optionalPfamIdentifiers.isPresent()) {
            Map<String, PfamIdentifier> pfamIdentifierMap = optionalPfamIdentifiers.get();
            if (pfamIdentifierMap.containsKey(chainIdentifier)) {
                pfamIdentifier = pfamIdentifierMap.get(chainIdentifier).getIdentifier();
            }
        }
        // EC
        Optional<Map<String, ECNumber>> optionalEcIdentifiers = match.getEcNumbers();
        if (optionalEcIdentifiers.isPresent()) {
            Map<String, ECNumber> ecIdentifierMap = optionalEcIdentifiers.get();
            if (ecIdentifierMap.containsKey(chainIdentifier)) {
                ecIdentifier = ecIdentifierMap.get(chainIdentifier).getIdentifier();
            }
        }
    }

    public String getChainIdentifier() {
        return chainIdentifier;
    }

    public String getEcIdentifier() {
        return ecIdentifier;
    }

    public String getPfamIdentifier() {
        return pfamIdentifier;
    }

    public String getUniProtIdentifier() {
        return uniProtIdentifier;
    }
}
