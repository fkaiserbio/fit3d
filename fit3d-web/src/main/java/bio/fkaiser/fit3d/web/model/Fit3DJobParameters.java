package bio.fkaiser.fit3d.web.model;

import bio.fkaiser.fit3d.web.model.constant.StatisticalModelType;
import de.bioforscher.singa.structure.model.oak.StructuralEntityFilter.AtomFilterType;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class Fit3DJobParameters implements Serializable {

    private static final long serialVersionUID = 6180575290352584883L;

    private AtomFilterType atomFilterType;
    private boolean pdbTargetList;
    private boolean chainTargetList;
    private Path targetListPath;
    private Path motifPath;
    private StatisticalModelType statisticalModelType;
    private double rmsdLimit;
    private List<ExchangeDefinition> exchangeDefinitions;

    public AtomFilterType getAtomFilterType() {
        return atomFilterType;
    }

    public void setAtomFilterType(AtomFilterType atomFilterType) {
        this.atomFilterType = atomFilterType;
    }

    public List<ExchangeDefinition> getExchangeDefinitions() {
        return exchangeDefinitions;
    }

    public void setExchangeDefinitions(List<ExchangeDefinition> exchangeDefinitions) {
        this.exchangeDefinitions = exchangeDefinitions;
    }

    public Path getMotifPath() {
        return motifPath;
    }

    public void setMotifPath(Path motifPath) {
        this.motifPath = motifPath;
    }

    public double getRmsdLimit() {
        return rmsdLimit;
    }

    public void setRmsdLimit(double rmsdLimit) {
        this.rmsdLimit = rmsdLimit;
    }

    public StatisticalModelType getStatisticalModelType() {
        return statisticalModelType;
    }

    public void setStatisticalModelType(StatisticalModelType statisticalModelType) {
        this.statisticalModelType = statisticalModelType;
    }

    public Path getTargetListPath() {
        return targetListPath;
    }

    public void setTargetListPath(Path targetListPath) {
        this.targetListPath = targetListPath;
    }

    public boolean isChainTargetList() {
        return chainTargetList;
    }

    public void setChainTargetList(boolean chainTargetList) {
        this.chainTargetList = chainTargetList;
    }

    public boolean isPdbTargetList() {
        return pdbTargetList;
    }

    public void setPdbTargetList(boolean pdbTargetList) {
        this.pdbTargetList = pdbTargetList;
    }
}
