package bio.fkaiser.fit3d.web.converter;

import bio.fkaiser.fit3d.web.model.Fit3DJob;
import bio.fkaiser.fit3d.web.model.Fit3DJobParameters;
import bio.fkaiser.fit3d.web.model.constant.StatisticalModelType;
import bio.fkaiser.fit3d.web.model.ExchangeDefinition;
import bio.singa.structure.model.oak.StructuralEntityFilter.AtomFilterType;
import org.bson.Document;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class JobConverter {

    public static Document toDocument(Fit3DJob job) {

        Fit3DJobParameters parameters = job.getParameters();

        List<String> exchangeDefinitionStrings = parameters.getExchangeDefinitions().stream()
                                                           .map(ExchangeDefinition::toString)
                                                           .collect(Collectors.toList());

        Document parametersObject = new Document()
                .append("atomFilterType", parameters.getAtomFilterType().toString())
                .append("pdbTargetList", parameters.isPdbTargetList())
                .append("chainTargetList", parameters.isChainTargetList())
                .append("targetListPath", parameters.getTargetListPath().toString())
                .append("motifPath", parameters.getMotifPath().toString())
                .append("statisticalModel", parameters.getStatisticalModelType().toString())
                .append("rmsdLimit", parameters.getRmsdLimit())
                .append("exchangeDefinitions", exchangeDefinitionStrings);

        return new Document()
                .append("timeStamp", job.getTimeStamp().toString())
                .append("jobIdentifier", job.getJobIdentifier().toString())
                .append("sessionIdentifier", job.getSessionIdentifier().toString())
                .append("ipAddress", job.getIpAddress())
                .append("jobPath", job.getJobPath().toString())
                .append("description", job.getDescription())
                .append("email", job.getEmail())
                .append("enqueued", job.isEnqueued())
                .append("running", job.isRunning())
                .append("finished", job.isFinished())
                .append("failed", job.isFailed())
                .append("sendMail", job.isSendMail())
                .append("errorMessage", job.getErrorMessage())
                .append("parameters", parametersObject);
    }

    @SuppressWarnings("unchecked")
    public static Fit3DJob toFit3DJob(Document jobDocument) {

        Document parametersObject = (Document) jobDocument.get("parameters");

        List<ExchangeDefinition> exchangeDefinitions = ((List<String>) parametersObject.get("exchangeDefinitions")).stream()
                                                                                                                   .map(ExchangeDefinition::fromString)
                                                                                                                   .collect(Collectors.toList());
        Fit3DJobParameters parameters = new Fit3DJobParameters();
        parameters.setAtomFilterType(AtomFilterType.valueOf((String) parametersObject.get("atomFilterType")));
        parameters.setPdbTargetList((boolean) parametersObject.get("pdbTargetList"));
        parameters.setChainTargetList((boolean) parametersObject.get("chainTargetList"));
        parameters.setTargetListPath(Paths.get((String) parametersObject.get("targetListPath")));
        parameters.setMotifPath(Paths.get((String) parametersObject.get("motifPath")));
        parameters.setStatisticalModelType(StatisticalModelType.valueOf((String) parametersObject.get("statisticalModel")));
        parameters.setRmsdLimit((double) parametersObject.get("rmsdLimit"));
        parameters.setExchangeDefinitions(exchangeDefinitions);

        Fit3DJob fit3dJob = new Fit3DJob();
        fit3dJob.setTimeStamp(LocalDateTime.parse((String) jobDocument.get("timeStamp")));
        fit3dJob.setSessionIdentifier((UUID.fromString((String) jobDocument.get("sessionIdentifier"))));
        fit3dJob.setIpAddress((String) jobDocument.get("ipAddress"));
        fit3dJob.setJobIdentifier(UUID.fromString((String) jobDocument.get("jobIdentifier")));
        fit3dJob.setJobPath(Paths.get((String) jobDocument.get("jobPath")));
        fit3dJob.setDescription((String) jobDocument.get("description"));
        fit3dJob.setEmail((String) jobDocument.get("email"));
        fit3dJob.setEnqueued((boolean) jobDocument.get("enqueued"));
        fit3dJob.setRunning((boolean) jobDocument.get("running"));
        fit3dJob.setFinished((boolean) jobDocument.get("finished"));
        fit3dJob.setFailed((boolean) jobDocument.get("failed"));
        fit3dJob.setSendMail((boolean) jobDocument.get("sendMail"));
        fit3dJob.setErrorMessage((String) jobDocument.get("errorMessage"));
        fit3dJob.setParameters(parameters);

        return fit3dJob;
    }
}
