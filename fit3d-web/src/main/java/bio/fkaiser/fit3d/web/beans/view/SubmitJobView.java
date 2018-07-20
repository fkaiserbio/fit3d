package bio.fkaiser.fit3d.web.beans.view;

import bio.fkaiser.fit3d.web.Fit3DWebConstants;
import bio.fkaiser.fit3d.web.beans.application.JobExecutor;
import bio.fkaiser.fit3d.web.beans.application.JobManager;
import bio.fkaiser.fit3d.web.beans.session.SessionManager;
import bio.fkaiser.fit3d.web.model.ExchangeDefinition;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import bio.fkaiser.fit3d.web.model.Fit3DJobParameters;
import bio.fkaiser.fit3d.web.model.MotifAnalysis;
import bio.fkaiser.fit3d.web.model.constant.PredefinedList;
import bio.fkaiser.fit3d.web.model.constant.StatisticalModelType;
import bio.singa.structure.model.identifiers.PDBIdentifier;
import bio.singa.structure.model.oak.StructuralEntityFilter.AtomFilterType;
import bio.singa.structure.model.oak.StructuralMotif;
import bio.singa.structure.parser.pdb.structures.StructureParser;
import bio.singa.structure.parser.pdb.structures.StructureParserOptions;
import org.omnifaces.util.Faces;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static bio.fkaiser.fit3d.web.beans.session.SessionManager.BASE_PATH;

public class SubmitJobView implements Serializable {

    private static final long serialVersionUID = -4697699771578104350L;
    private static final Logger logger = LoggerFactory.getLogger(SubmitJobView.class);

    /**
     * controllers
     */
    private JobManager jobManager;
    private SessionManager sessionManager;

    private JobExecutor jobExecutor;

    private boolean redirectedFromExtract;

    /**
     * job information
     */
    private UUID jobIdentifier;
    private Path jobPath;

    private boolean motifFileUploaded;


    private boolean targetListFileUploaded;
    private boolean targetListSelected;


    private boolean blocked;

    private String targetListFileLabel;

    private PredefinedList predefinedList;
    private Path motifPath;
    private Path externalMotifPath;
    private Path targetListPath;
    private StructuralMotif motif;
    private MotifAnalysis motifAnalysis;
    private int targetListSize;
    private String motifFileName;

    /**
     * parameters
     */
    private String description;
    private String email;
    private double rmsdLimit = Fit3DWebConstants.DefaultJobParameters.DEFAULT_RMSD_LIMIT;
    private StatisticalModelType statisticalModelType = Fit3DWebConstants.DefaultJobParameters.DEFAULT_STATISTICAL_MODEL_TYPE;
    private AtomFilterType atomFilterType = Fit3DWebConstants.DefaultJobParameters.DEFAULT_ATOM_FILTER_TYPE;
    private boolean pdbTargetList;
    private boolean chainTargetList;
    private List<ExchangeDefinition> exchangeDefinitions;

    private static void createJobDirectory(Path jobPath) throws IOException {
        if (!jobPath.toFile().exists()) {
            logger.info("initially creating job path {}", jobPath);
            Files.createDirectories(jobPath);
        }
    }

    public void handleAdvancedOptionsToggle(ToggleEvent event) {
        if (event.getVisibility() == Visibility.VISIBLE) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Changing advanced options can have a strong impact on algorithm performance.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void handleExchangeDefinition() {
        if (!motifFileUploaded) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "A query motif file is required before exchangeDefinitions can be defined.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else {
            // initialize motif file exchange definition
            initializeExchangeDefinitions();
        }
    }

    public void handleMotifUpload(FileUploadEvent event) throws IOException {
        motifPath = jobPath.resolve("motif.pdb");
        createJobDirectory(jobPath);

        Files.copy(event.getFile().getInputstream(), motifPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("copied motif file to working directory: {}", motifPath);

        // update file name to indicate uploaded file
        motifFileName = event.getFile().getFileName();
        RequestContext.getCurrentInstance().update("mainForm:motifFileLabel");

        externalMotifPath = SessionManager.relativizePath(motifPath);
        motifFileUploaded = true;

        // analyze motif structure
        analyzeMotif();

        // update motif meta data
        RequestContext.getCurrentInstance().update("motifMetaData");

        // expand toolbox
        RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");

        // show protein viewer
        RequestContext.getCurrentInstance().execute("viewer({ pdb : '" + externalMotifPath + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");

        FacesMessage message = new FacesMessage("Successful", motifFileName + " successfully uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void handleTargetListSelected() {
        targetListSelected = !predefinedList.equals(PredefinedList.NONE);
    }

    public void handleTargetListUpload(FileUploadEvent event) throws IOException {
        targetListPath = jobPath.resolve("targets.txt");
        createJobDirectory(jobPath);

        Files.copy(event.getFile().getInputstream(), targetListPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("copied target list file to working directory: {}", targetListPath);
        targetListFileLabel = event.getFile().getFileName();

        // update file name to indicate uploaded file
        RequestContext.getCurrentInstance().update("mainForm:targetListFileLabel");
        targetListFileUploaded = true;

        // update main form
        RequestContext.getCurrentInstance().update("mainForm:targetList");

        // analyze target list
        analyzeTargetList();

        // update target list meta data
        RequestContext.getCurrentInstance().update("targetListMetaData");

        FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " successfully uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    @PostConstruct
    public void init() {
        jobIdentifier = UUID.randomUUID();
        exchangeDefinitions = new ArrayList<>();
        jobPath = sessionManager.getSessionPath().resolve(jobIdentifier.toString());
        Flash flash = Faces.getFlash();
        String key = "extractedMotifPath";
        if (flash.containsKey(key)) {
            Path extractedMotifPath = Faces.getFlashAttribute(key);
            if (extractedMotifPath != null) {
                handleExtractedMotifSubmission(extractedMotifPath);
            }
        }
    }

    private void handleExtractedMotifSubmission(Path extractedMotifPath) {

        logger.info("handling submission from extraction wizard for motif {}", extractedMotifPath);

        motifPath = extractedMotifPath;
        externalMotifPath = SessionManager.relativizePath(extractedMotifPath);
        motifFileUploaded = true;

        // set flag that we are coming from extract page
        redirectedFromExtract = true;

        // update motif meta data
        RequestContext.getCurrentInstance().update("motifFile");

        // analyze motif structure
        analyzeMotif();

        // update motif meta data
        RequestContext.getCurrentInstance().update("motifMetaData");

        // expand toolbox
        RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");

        // show protein viewer
        RequestContext.getCurrentInstance().execute("viewer({ pdb : '" + externalMotifPath + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");
    }

    public String submit() {

        // determine if too many jobs were submitted in current session
        int jobCount = getJobCountOfCurrentSession();
        if (jobCount > Fit3DWebConstants.JOB_LIMIT) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Too many jobs submitted. Please wait until until some jobs are finished.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        // determine if too many exchanges were defined
        if (exchangeDefinitions != null) {
            int exchangeCount = (int) exchangeDefinitions.stream()
                                                         .map(ExchangeDefinition::getExchangeAminoAcids)
                                                         .mapToLong(Collection::size)
                                                         .sum();
            if (exchangeCount > Fit3DWebConstants.EXCHANGE_LIMIT) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Only " + Fit3DWebConstants.EXCHANGE_LIMIT
                                                                                              + " PSEs are allowed. Please use the command line or API version for more complex calculations.");
                FacesContext.getCurrentInstance().addMessage(null, message);
                return null;
            }
        }

        // motif file upload is mandatory
        if (!motifFileUploaded) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "A motif file is required.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        // use predefined target list if no one was provided
        if (!targetListFileUploaded) {
            if (predefinedList == PredefinedList.NONE) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select or upload a target list.");
                FacesContext.getCurrentInstance().addMessage(null, message);
                return null;
            } else {
                targetListPath = predefinedList.getPath();
                chainTargetList = true;
            }
        }

        Fit3DJobParameters jobParameters = new Fit3DJobParameters();
        jobParameters.setAtomFilterType(atomFilterType);
        jobParameters.setChainTargetList(chainTargetList);
        jobParameters.setPdbTargetList(pdbTargetList);
        if (motifPath.endsWith("4cha_motif.pdb")) {
            targetListPath = motifPath.getParent().resolve("targets.txt");
            logger.info("example run detected, using short target list {}", targetListPath);
            motifPath = motifPath.getParent().resolve("4cha_motif.pdb");
        }
        jobParameters.setMotifPath(motifPath);
        jobParameters.setTargetListPath(targetListPath);
        jobParameters.setRmsdLimit(rmsdLimit);
        jobParameters.setExchangeDefinitions(exchangeDefinitions);
        jobParameters.setStatisticalModelType(statisticalModelType);

        Fit3DJob job = new Fit3DJob(jobIdentifier, sessionManager.getSessionIdentifier(), determineIpAddress(), description, email, jobPath, jobParameters);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Flash flash = facesContext.getExternalContext().getFlash();
        flash.put("job", job);

        return "success";
    }

    private String determineIpAddress() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null) {
            ipAddress = ipAddress.replaceFirst(",.*", "");
        } else {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public void submitExample() {

        predefinedList = PredefinedList.BLASTe7;
        description = "trypsin active site";

        // read example motif (path for example motif differs from usual motif submission)
        String exampleMotifFileName = "4cha_motif.pdb";
        motifPath = BASE_PATH.getParent().resolve("example").resolve(exampleMotifFileName);
        externalMotifPath = SessionManager.relativizePath(motifPath);

        motifFileName = exampleMotifFileName;

        analyzeMotif();

        // update motif meta data
        RequestContext.getCurrentInstance().update("motifMetaData");

        // expand toolbox
        RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");

        // show protein viewer
        RequestContext.getCurrentInstance().execute("viewer({ pdb : '" + externalMotifPath + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");

        motifFileUploaded = true;
    }

    private void analyzeMotif() {
        // read motif
        if (!redirectedFromExtract) {
            motif = StructuralMotif.fromLeafSubstructures(StructureParser.local()
                                                                         .path(motifPath)
                                                                         .everything()
                                                                         .setOptions(StructureParserOptions.withSettings(StructureParserOptions.Setting.OMIT_HYDROGENS,
                                                                                                                         StructureParserOptions.Setting.GET_IDENTIFIER_FROM_FILENAME))
                                                                         .parse().getAllLeafSubstructures());
        } else {
            motif = StructuralMotif.fromLeafSubstructures(StructureParser.local()
                                                                         .path(motifPath)
                                                                         .everything()
                                                                         .setOptions(StructureParserOptions.withSettings(StructureParserOptions.Setting.OMIT_HYDROGENS))
                                                                         .parse().getAllLeafSubstructures());
        }
        motifAnalysis = MotifAnalysis.of(motif);

        // only allow all-atom for mixed motifs
        if (motifAnalysis.isMixedMotif()) {
            atomFilterType = AtomFilterType.ARBITRARY;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Your motif contains a non-amino acid element. Only all-atom alignment is supported.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            RequestContext.getCurrentInstance().update("mainForm:atomFilterType");
        } else {
            RequestContext.getCurrentInstance().update("mainForm:atomFilterType");
        }
    }

    private void analyzeTargetList() throws IOException {

        // check if target list contains exclusively PDB-IDs
        List<String> pdbIdentifiers = Files.lines(targetListPath)
                                           .filter(Pattern.compile("^" + PDBIdentifier.PATTERN.pattern() + "$").asPredicate())
                                           .distinct()
                                           .collect(Collectors.toList());

        List<String> chainIdentifiers = Files.lines(targetListPath)
                                             .filter(Pattern.compile("^" + PDBIdentifier.PATTERN.pattern() + "\\s[0-9A-Za-z]+$").asPredicate())
                                             .distinct()
                                             .collect(Collectors.toList());

        if (!pdbIdentifiers.isEmpty() && !chainIdentifiers.isEmpty()) {

            if (!blocked) {
                blocked = true;
                RequestContext.getCurrentInstance().update("mainForm:submitButton");
            }

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Your target list is a mix of PDB-IDs and chain-IDs.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else {
            if (!pdbIdentifiers.isEmpty()) {
                targetListSize = pdbIdentifiers.size();
                chainTargetList = false;
                pdbTargetList = true;

                if (blocked) {
                    blocked = false;
                    RequestContext.getCurrentInstance().update("mainForm:submitButton");
                }
                targetListPath = jobPath.resolve("targets.txt");
                Files.write(jobPath.resolve("targets.txt"), pdbIdentifiers.stream()
                                                                          .collect(Collectors.joining("\n"))
                                                                          .getBytes());
                logger.info("copied PDB-ID target list to {}", targetListPath);
            } else if (!chainIdentifiers.isEmpty()) {
                targetListSize = chainIdentifiers.size();
                pdbTargetList = false;
                chainTargetList = true;

                if (blocked) {
                    blocked = false;
                    RequestContext.getCurrentInstance().update("mainForm:submitButton");
                }

                targetListPath = jobPath.resolve("targets.txt");
                Files.write(targetListPath, chainIdentifiers.stream()
                                                            .collect(Collectors.joining("\n"))
                                                            .getBytes());
                logger.info("copied chain-ID target list to {}", targetListPath);
            } else {
                if (!blocked) {
                    blocked = true;
                    RequestContext.getCurrentInstance().update("mainForm:submitButton");
                }
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Your target list does not contain valid PDB-IDs.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    private void initializeExchangeDefinitions() {
        exchangeDefinitions = motif.getAllAminoAcids().stream()
                                   .map(ExchangeDefinition::new)
                                   .collect(Collectors.toList());
    }

    public AtomFilterType getAtomFilterType() {
        return atomFilterType;
    }

    public void setAtomFilterType(AtomFilterType atomFilterType) {
        this.atomFilterType = atomFilterType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ExchangeDefinition> getExchangeDefinitions() {
        return exchangeDefinitions;
    }

    public void setExchangeDefinitions(List<ExchangeDefinition> exchangeDefinitions) {
        this.exchangeDefinitions = exchangeDefinitions;
    }

    private int getJobCountOfCurrentSession() {
        List<Fit3DJob> currentJobs = jobManager.getManagedJobs().get(sessionManager.getSessionIdentifier());
        if (currentJobs != null) {
            return (int) currentJobs.stream()
                                    .filter(job -> job.isRunning() || job.isEnqueued())
                                    .count();
        } else {
            return 0;
        }
    }

    public JobExecutor getJobExecutor() {
        return jobExecutor;
    }

    public void setJobExecutor(JobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public MotifAnalysis getMotifAnalysis() {
        return motifAnalysis;
    }

    public String getMotifFileName() {
        return motifFileName;
    }

    public PredefinedList getPredefinedList() {
        return predefinedList;
    }

    public void setPredefinedList(PredefinedList predefinedList) {
        this.predefinedList = predefinedList;
    }

    public double getRmsdLimit() {
        return rmsdLimit;
    }

    public void setRmsdLimit(double rmsdLimit) {
        this.rmsdLimit = rmsdLimit;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public StatisticalModelType getStatisticalModelType() {
        return statisticalModelType;
    }

    public void setStatisticalModelType(StatisticalModelType statisticalModelType) {
        this.statisticalModelType = statisticalModelType;
    }

    public String getTargetListFileLabel() {
        return targetListFileLabel;
    }

    public int getTargetListSize() {
        return targetListSize;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isMotifFileUploaded() {
        return motifFileUploaded;
    }

    public void setMotifFileUploaded(boolean motifFileUploaded) {
        this.motifFileUploaded = motifFileUploaded;
    }

    public boolean isRedirectedFromExtract() {
        return redirectedFromExtract;
    }

    public void setRedirectedFromExtract(boolean redirectedFromExtract) {
        this.redirectedFromExtract = redirectedFromExtract;
    }

    public boolean isTargetListFileUploaded() {
        return targetListFileUploaded;
    }

    public void setTargetListFileUploaded(boolean targetListFileUploaded) {
        this.targetListFileUploaded = targetListFileUploaded;
    }

    public boolean isTargetListSelected() {
        return targetListSelected;
    }

    public void setTargetListSelected(boolean targetListSelected) {
        this.targetListSelected = targetListSelected;
    }
}
