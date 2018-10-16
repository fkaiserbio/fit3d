package bio.fkaiser.fit3d.web.model.constant;

import bio.singa.structure.algorithms.superimposition.fit3d.statistics.FofanovEstimation;
import bio.singa.structure.algorithms.superimposition.fit3d.statistics.StarkEstimation;
import bio.singa.structure.algorithms.superimposition.fit3d.statistics.StatisticalModel;

/**
 * @author fk
 */
public enum StatisticalModelType {

    FOFANOV("Fofanov et al. 2008", FofanovEstimation.class),
    STARK("Stark et al. 2003", StarkEstimation.class);

    private final String description;
    private final Class<? extends StatisticalModel> statisticalModel;

    StatisticalModelType(String description, Class<? extends StatisticalModel> statisticalModel) {
        this.description = description;
        this.statisticalModel = statisticalModel;
    }

    public String getDescription() {
        return description;
    }
}
