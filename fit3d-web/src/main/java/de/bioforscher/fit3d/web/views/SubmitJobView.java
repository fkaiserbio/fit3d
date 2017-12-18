package de.bioforscher.fit3d.web.views;

import de.bioforscher.fit3d.web.controllers.JobController;
import de.bioforscher.fit3d.web.controllers.SessionController;
import de.bioforscher.fit3d.web.core.Fit3DJob;
import de.bioforscher.fit3d.web.services.JobExecutorService;
import de.bioforscher.fit3d.web.utilities.ExchangeDefinition;
import de.bioforscher.fit3d.web.utilities.Fit3dConstants;
import de.bioforscher.fit3d.web.utilities.enums.AtomSelection;
import de.bioforscher.fit3d.web.utilities.enums.MotifComplexity;
import de.bioforscher.fit3d.web.utilities.enums.PredefinedList;
import de.bioforscher.fit3d.web.utilities.enums.PvalueMethod;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.*;
import java.util.List;
import java.util.UUID;

public class SubmitJobView implements Serializable {

    private static final long serialVersionUID = -4697699771578104350L;
    private static final Logger logger = LoggerFactory.getLogger(SubmitJobView.class);

    private boolean complete;
    private String description;
    private String email;
    private boolean fileDefinedTargetList;
    private boolean redirectedFromExtract;

    private boolean filtering;

    private UUID id;

    private JobController jobController;

    private JobExecutorService jobExecutorService;
    private double maxRmsd = 2.0;

    private int motifAminoAcidCount;

    private String motifSeq;

    private String motifType;

    private MotifComplexity motifComplexity;

    private String motifFileExternalPath;

    private boolean motifFileUploaded;

    private PredefinedList predefinedTargetList;

    private PvalueMethod pvalueMethod;

    private AtomSelection alignmentAtomSelection;

    private SessionController sessionController;
    private String targetListFileInternalPath;

    private boolean targetListFileUploaded;
    private boolean targetListSelected;
    private String workingDirectory;

    private List<ExchangeDefinition> exchanges;

    private double maxExtent;

    private int targetListCount;

    private boolean blocked;

    private String motifFileLabel;

    private String targetListFileLabel;

    private String extractPdbFileInternalPath;

    private String extractPdbFileExternalPath;

    public SubmitJobView() {

        id = UUID.randomUUID();
    }

    public AtomSelection getAlignmentAtomSelection() {
        return alignmentAtomSelection;
    }

    public void setAlignmentAtomSelection(AtomSelection alignmentAtomSelection) {
        this.alignmentAtomSelection = alignmentAtomSelection;
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

    public List<ExchangeDefinition> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<ExchangeDefinition> exchanges) {
        this.exchanges = exchanges;
    }

    public String getExtractPdbFileExternalPath() {
        return extractPdbFileExternalPath;
    }

    public String getExtractPdbFileInternalPath() {
        return extractPdbFileInternalPath;
    }

    public JobController getJobController() {
        return jobController;
    }

    public void setJobController(JobController jobController) {
        this.jobController = jobController;
    }

    public JobExecutorService getJobExecutorService() {
        return jobExecutorService;
    }

    public void setJobExecutorService(JobExecutorService jobExecutorService) {
        this.jobExecutorService = jobExecutorService;
    }

    public double getMaxExtent() {
        return maxExtent;
    }

    public double getMaxRmsd() {
        return maxRmsd;
    }

    public void setMaxRmsd(double maxRmsd) {
        this.maxRmsd = maxRmsd;
    }

    public int getMotifAminoAcidCount() {
        return motifAminoAcidCount;
    }

    public MotifComplexity getMotifComplexity() {
        return motifComplexity;
    }

    public void setMotifComplexity(MotifComplexity motifComplexity) {
        this.motifComplexity = motifComplexity;
    }

    public String getMotifFileExternalPath() {
        return motifFileExternalPath;
    }

    public void setMotifFileExternalPath(String motifFileExternalPath) {
        this.motifFileExternalPath = motifFileExternalPath;
    }

    public String getMotifFileLabel() {
        return motifFileLabel;
    }

    public String getMotifSeq() {
        return motifSeq;
    }

    public void setMotifSeq(String motifSeq) {
        this.motifSeq = motifSeq;
    }

    public String getMotifType() {
        return motifType;
    }

    public void setMotifType(String motifType) {
        this.motifType = motifType;
    }

    public PredefinedList getPredefinedTargetList() {
        return predefinedTargetList;
    }

    public void setPredefinedTargetList(PredefinedList predefinedTargetList) {
        this.predefinedTargetList = predefinedTargetList;
    }

    public PvalueMethod getPvalueMethod() {
        return pvalueMethod;
    }

    public void setPvalueMethod(PvalueMethod pvalueMethod) {
        this.pvalueMethod = pvalueMethod;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public int getTargetListCount() {
        return targetListCount;
    }

    public String getTargetListFileInternalPath() {
        return targetListFileInternalPath;
    }

    public void setTargetListFileInternalPath(String targetListFileInternalPath) {
        this.targetListFileInternalPath = targetListFileInternalPath;
    }

    public String getTargetListFileLabel() {
        return targetListFileLabel;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void handleAdvancedOptionsToggle(ToggleEvent event) {

        if (event.getVisibility() == Visibility.VISIBLE) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning",
                                                "Changing advanced options is mostly not neccessary and can have drastical impact on algorithm performance.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void handleExchangeDefinition() throws IOException {

        if (!motifFileUploaded) {

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                    "A query motif file is required before exchanges can be defined.");
            FacesContext.getCurrentInstance().addMessage(null, message);

        } else {

            // initialize motif file exchanges
            initializeExchangeSelection();
        }
    }

    /**
     * handles input submission based on previously extracted motif
     *
     * @param extractMotifFilePath
     * @param extractPdbFilePath
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void handleExtractMotifSubmission(String extractMotifFilePath, String extractPdbFilePath)
            throws FileNotFoundException, IOException {

        logger.info("trying to copy motif file to local storage");

        copyFile(workingDirectory + "/motif.pdb", new FileInputStream(new File(extractMotifFilePath)));
        copyFile(workingDirectory + "/extract.pdb", new FileInputStream(new File(extractPdbFilePath)));

        extractPdbFileInternalPath = workingDirectory + "/extract.pdb";
        extractPdbFileExternalPath = "data/" + sessionController.getSessionIdentifier() + "/" + id + "/extract.pdb";

        motifFileExternalPath = "data/" + sessionController.getSessionIdentifier() + "/" + id + "/motif.pdb";
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
        RequestContext.getCurrentInstance().execute("viewer({ pdb : '" + motifFileExternalPath
                                                    + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");

        // FacesMessage message = new FacesMessage("Succesful", event.getFile()
        // .getFileName() + " successfully uploaded.");
        // FacesContext.getCurrentInstance().addMessage(null, message);
    }

    /**
     * handles motif file upload and triggers client side visualization
     *
     * @param event
     * @throws IOException
     */
    public void handleMotifUpload(FileUploadEvent event) throws IOException {

        logger.info("trying to copy motif file to local storage");

        copyFile(workingDirectory + "/motif.pdb", event.getFile().getInputstream());
        motifFileLabel = event.getFile().getFileName();

        // update file name to indicate uploaded file
        RequestContext.getCurrentInstance().update("mainForm:motifFileLabel");

        motifFileExternalPath = "data/" + sessionController.getSessionIdentifier() + "/" + id + "/motif.pdb";
        motifFileUploaded = true;

        // analyze motif structure
        analyzeMotif();

        // update motif meta data
        RequestContext.getCurrentInstance().update("motifMetaData");

        // expand toolbox
        RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");

        // show protein viewer
        RequestContext.getCurrentInstance().execute("viewer({ pdb : '" + motifFileExternalPath
                                                    + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");

        FacesMessage message = new FacesMessage("Succesful", motifFileLabel + " successfully uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void handleTargetListSelected() {

        if (predefinedTargetList.equals(PredefinedList.NONE)) {

            targetListSelected = false;
        } else {

            targetListSelected = true;
        }
    }

    /**
     * handles target list file upload
     *
     * @param event
     * @throws IOException
     */
    public void handleTargetListUpload(FileUploadEvent event) throws IOException {

       logger.info("trying to copy target list file to local storage");

        targetListFileInternalPath = workingDirectory + "/" + "targets.txt";

        copyFile(targetListFileInternalPath, event.getFile().getInputstream());
        targetListFileLabel = event.getFile().getFileName();

        // update file name to indicate uploaded file
        RequestContext.getCurrentInstance().update("mainForm:targetListFileLabel");

        targetListFileUploaded = true;

        // update main formular
        RequestContext.getCurrentInstance().update("mainForm:targetList");

        // analyze target list
        analyzeTargetList();

        // update target list meta data
        RequestContext.getCurrentInstance().update("targetListMetaData");

        FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " successfully uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    @PostConstruct
    public void init() {
        // this.workingDirectory =
        // FacesContext.getCurrentInstance().getExternalContext()
        // .getRealPath("data/" + this.sessionController.getId() + "/" + this.id
        // + "/");
        // unix/windows spacken
        // this.workingDirectory =
        // FacesContext.getCurrentInstance().getExternalContext().getRealPath("data/"
        // + this.sessionController.getId());

        // TODO implement
//		this.workingDirectory = System.getProperty("os.name").startsWith("Win")
//				? FacesContext.getCurrentInstance().getExternalContext().getRealPath("/data/")
//						+ this.sessionController.getId() + "/" + this.id + "/"
//				: FacesContext.getCurrentInstance().getExternalContext()
//						.getRealPath("data/" + this.sessionController.getId() + "/" + this.id + "/");
//
//		// set defaults
//		this.filtering = true;
//
//		String extractMotifFilePath = Faces.getFlashAttribute("motifFilePath");
//		String extractPdbFilePath = Faces.getFlashAttribute("extractPdbPath");
//		if (extractMotifFilePath != null && extractPdbFilePath != null) {
//
//			try {
//				handleExtractMotifSubmission(extractMotifFilePath, extractPdbFilePath);
//			} catch (IOException | StructureException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isFileDefinedTargetList() {
        return fileDefinedTargetList;
    }

    public void setFileDefinedTargetList(boolean fileDefinedTargetList) {
        this.fileDefinedTargetList = fileDefinedTargetList;
    }

    public boolean isFiltering() {
        return filtering;
    }

    public void setFiltering(boolean filtering) {
        this.filtering = filtering;
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

    public String submit() throws IOException {

        long jobCount = countJobs();
        if (jobCount > Fit3dConstants.JOB_LIMIT) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                    "Too many jobs sumitted. Please wait until until some jobs are finished.");
            FacesContext.getCurrentInstance().addMessage(null, message);

            return null;
        }

        if (exchanges != null) {

            // count exchanges and block if more than limit
            int exchangeCount = 0;
            for (ExchangeDefinition exchangeDefinition : exchanges) {

                exchangeCount += exchangeDefinition.getExchangeAminoAcids().size();
            }
            if (exchangeCount > Fit3dConstants.EXCHANGE_LIMIT) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Sorry, only "
                                                                                              + Fit3dConstants.EXCHANGE_LIMIT
                                                                                              + " PSEs are allowed. Please use the command line version for more complex calculations.");
                FacesContext.getCurrentInstance().addMessage(null, message);

                return null;
            }
        }

        if (!motifFileUploaded) {

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                    "A query motif file is required.");
            FacesContext.getCurrentInstance().addMessage(null, message);

            return null;
        }

        // use predefined target list if no one was provided
        if (!targetListFileUploaded) {

            if (predefinedTargetList == PredefinedList.NONE) {

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                        "Please select or upload a target list.");
                FacesContext.getCurrentInstance().addMessage(null, message);

                return null;
            } else {

                targetListFileInternalPath = FacesContext.getCurrentInstance().getExternalContext()
                                                         .getRealPath(predefinedTargetList.getPath());
            }
        }

        // create new Fit3D Job
        // TODO implement
//        String commandLine = generateCommandLine();
//        LogHandler.LOG.info("generated command line: " + commandLine);
//        Fit3DJob job = new Fit3DJob(this.id, this.sessionController.getId(), new Date(), this.description, this.email,
//                                    this.workingDirectory, commandLine,
//                                    new JobParameters(this.motifSeq, this.alignmentAtomSelection, this.pvalueMethod,
//                                                      this.predefinedTargetList, this.maxRmsd, true, this.exchanges,
//                                                      FilenameUtils.getName(this.targetListFileInternalPath), this.extractPdbFileInternalPath,
//                                                      this.targetListFileLabel));

        // add new job to controller
        // TODO implement
//        this.jobController.addNewJob(this.sessionController.getId(), job);

        return "success";
    }

    public String submitExample() throws IOException {

        long jobCount = countJobs();

        if (jobCount > Fit3dConstants.JOB_LIMIT) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                    "Too many jobs sumitted. Please wait until until some jobs are finished.");
            FacesContext.getCurrentInstance().addMessage(null, message);

            return null;
        }

        predefinedTargetList = PredefinedList.BLASTe80;
        description = "4CHA catalytic triad";
        // this.workingDirectory =
        // FacesContext.getCurrentInstance().getExternalContext().getRealPath("data/example/");
        workingDirectory = System.getProperty("os.name").startsWith("Win")
                           ? FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "data/example/"
                           : FacesContext.getCurrentInstance().getExternalContext().getRealPath("data/example/");
        motifSeq = "HDS";
        alignmentAtomSelection = AtomSelection.ALL;
        pvalueMethod = PvalueMethod.FOFANOV;
        predefinedTargetList = PredefinedList.BLASTe80;
        maxRmsd = 2.5;
        filtering = true;
        exchanges = null;
        targetListFileInternalPath = null;
        extractPdbFileInternalPath = workingDirectory + "/extract.pdb";

        // TODO implement
//        Fit3DJobDummy job = new Fit3DJobDummy(this.id, this.sessionController.getId(), new Date(), this.description,
//                                              this.email, this.workingDirectory, generateCommandLine(),
//                                              new JobParameters(this.motifSeq, this.alignmentAtomSelection, this.pvalueMethod,
//                                                                this.predefinedTargetList, this.maxRmsd, this.filtering, this.exchanges,
//                                                                FilenameUtils.getName(this.targetListFileInternalPath), this.extractPdbFileInternalPath,
//                                                                this.targetListFileLabel));
//
//        this.jobController.addNewJob(this.sessionController.getId(), job);

        return "success";
    }

    private void analyzeMotif() throws IOException {

        // TODO implement
//        // load motif amino acids
//        QueryStructureParser qsp = new QueryStructureParser("", this.workingDirectory + "/motif.pdb");
//
//        List<AminoAcid> motifAminoAcids = qsp.parse();
//
//        // count amino acids
//        this.motifAminoAcidCount = motifAminoAcids.size();
//
//        // do not accept motifs with none or one amino acid
//        if (this.motifAminoAcidCount < 2) {
//
//            this.blocked = true;
//
//            // update submitButton
//            RequestContext.getCurrentInstance().update("mainForm");
//
//            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
//                                                    "Your PDB file is invalid. The query motif must contain at least two valid amino acids.");
//            FacesContext.getCurrentInstance().addMessage(null, message);
//        }
//
//        // compute spatial extent
//        this.maxExtent = Math.sqrt(StructureUtils.getMaxSquaredExtent(motifAminoAcids));
//
//        // compute motif sequence
//        StringBuilder motifSeq = new StringBuilder();
//        Set<String> chainList = new HashSet<>();
//        for (AminoAcid aa : motifAminoAcids) {
//
//            motifSeq.append(aa.getAminoType());
//            chainList.add(aa.getChainId());
//        }
//        this.motifSeq = motifSeq.toString();
//
//        // get motif type
//        if (chainList.size() > 1) {
//
//            this.motifType = "inter";
//        } else {
//
//            this.motifType = "intra";
//        }
//
//        // compute average complexity
//        int c;
//        int n = (int) this.maxExtent;
//        int k = this.motifAminoAcidCount - 1;
//        final int min = (k < n - k ? k : n - k);
//        long bin = 1;
//        for (int i = 1; i <= min; i++) {
//            bin *= n;
//            bin /= i;
//            n--;
//        }
//
//        c = (int) (Math.pow(this.motifAminoAcidCount, 2) + bin);
//
//        if (c < Fit3dConstants.COMPLEXITY_LOWER_BOUND) {
//
//            this.motifComplexity = MotifComplexity.LOW;
//        } else if (c > Fit3dConstants.COMPLEXITY_LOWER_BOUND && c < Fit3dConstants.COMPLEXTY_UPPER_BOUND) {
//
//            this.motifComplexity = MotifComplexity.MEDIUM;
//        } else if (c > Fit3dConstants.COMPLEXTY_UPPER_BOUND) {
//
//            this.motifComplexity = MotifComplexity.HIGH;
//            this.blocked = true;
//
//            // update submitButton
//            RequestContext.getCurrentInstance().update("mainForm:submitButton");
//
//            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
//                                                    "Your motif was rated as too complex. Please define a simpler structure or use our command line implementation.");
//            FacesContext.getCurrentInstance().addMessage(null, message);
//        }
    }

    private void analyzeTargetList() throws IOException {

        // check if target file is valid
        checkValidTargetListFile();

        // reduce to unique set
        reduceTargetList();

        // TODO implement
//        List<String> targetList = FileUtils.readLines(new File(this.targetListFileInternalPath));
//
//        this.targetListCount = targetList.size();
    }

    private void checkValidTargetListFile() throws IOException {

        // TODO implement
//        List<String> targetListFileContent = FileUtils.readLines(new File(this.targetListFileInternalPath));
//
//        for (String s : targetListFileContent) {
//
//            if (!Pattern.matches("(^\\w{4}$)", s)) {
//
//                this.blocked = true;
//
//                // update submitButton
//                RequestContext.getCurrentInstance().update("mainForm");
//
//                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
//                                                        "Your target list file contains invalid PDB-IDs.");
//                FacesContext.getCurrentInstance().addMessage(null, message);
//            }
//        }
    }

    private void copyFile(String outputPath, InputStream is) throws IOException {

        File f = new File(outputPath);

        // ensure that data folder exists
        if (!f.getParentFile().exists()) {

            f.getParentFile().mkdirs();
        }

        OutputStream out = new FileOutputStream(new File(outputPath));

        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = is.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }

        is.close();
        out.flush();
        out.close();

        logger.info("file created: " + outputPath);
    }

    private long countJobs() {
        List<Fit3DJob> currentJobs = jobController.getManagedJobs().get(sessionController.getSessionIdentifier());

        long jobCount = (currentJobs != null)
                        ? currentJobs.stream().filter(j -> (j.isRunning() || j.isEnqueued())).count() : 0;

        return jobCount;
    }

    /**
     * generates a Fit3D command line
     *
     * @return
     * @throws IOException
     */
    private String generateCommandLine() throws IOException {

        // TODO implement
//        StringBuilder cl = new StringBuilder();
//
//        cl.append("-m ");
//        cl.append("motif.pdb");
//        cl.append(" ");
//
//        cl.append("-l ");
//        cl.append(this.targetListFileInternalPath);
//        cl.append(" ");
//
//        cl.append("-r ");
//        cl.append(String.valueOf(this.maxRmsd));
//        cl.append(" ");
//
//        if (this.filtering) {
//
//            cl.append("-F ");
//        }
//
//        cl.append("-E ");
//
//        cl.append("-M ");
//
//        cl.append("-N ");
//
//        if (this.targetListFileUploaded) {
//
//            cl.append(this.targetListCount);
//            cl.append(" ");
//        } else {
//            String path = System.getProperty("os.name").startsWith("Win")
//                          ? FacesContext.getCurrentInstance().getExternalContext().getRealPath("/")
//                            + this.predefinedTargetList.getPath() + "/"
//                          : FacesContext.getCurrentInstance().getExternalContext()
//                                        .getRealPath(this.predefinedTargetList.getPath());
//            cl.append((FileUtils.readLines(new File(path))).size());
//            cl.append(" ");
//        }
//
//        cl.append("-f ");
//        cl.append("results.csv");
//        cl.append(" ");
//
//        cl.append("-p ");
//        cl.append(Fit3dConstants.PDB_DIR);
//        cl.append(" ");
//
//        cl.append("-P ");
//        cl.append(this.pvalueMethod.getCommandlineOption());
//        cl.append(" ");
//
//        cl.append("-T ");
//
//        cl.append("-o ");
//        cl.append("structures");
//        cl.append(" ");
//
//        cl.append("-g ");
//
//        cl.append("-c ");
//
//        if (!this.alignmentAtomSelection.getAtoms().equals("")) {
//
//            cl.append("-a ");
//            cl.append(this.alignmentAtomSelection.getAtoms());
//            cl.append(" ");
//        }
//
//        if (this.exchanges != null) {
//
//            if (!this.exchanges.isEmpty()) {
//
//                StringBuilder plainExchangeDefinition = new StringBuilder();
//                for (ExchangeDefinition ed : this.exchanges) {
//
//                    if (ed.getExchangeAminoAcids().isEmpty()) {
//
//                        continue;
//                    }
//
//                    plainExchangeDefinition.append(ed);
//                    plainExchangeDefinition.append(",");
//                }
//
//                if (!plainExchangeDefinition.toString().isEmpty()) {
//
//                    cl.append("-e ");
//                    cl.append(plainExchangeDefinition);
//                    cl.delete(cl.length() - 1, cl.length());
//                }
//            }
//        }

        return "";
    }

    private void initializeExchangeSelection() throws IOException {

        // TODO implement
//        QueryStructureParser qsp = new QueryStructureParser("", this.workingDirectory + "/motif.pdb");
//
//        this.exchanges = new ArrayList<ExchangeDefinition>();
//
//        for (AminoAcid aa : qsp.parse()) {
//
//            ExchangeDefinition ed = new ExchangeDefinition(aa.getResidueNumber().getSeqNum(), aa.getAminoType());
//            this.exchanges.add(ed);
//        }
    }

    private void reduceTargetList() throws IOException {

        // TODO implement
//        List<String> lowerCaseList = new ArrayList<>();
//
//        for (String line : FileUtils.readLines(new File(this.targetListFileInternalPath))) {
//
//            lowerCaseList.add(line.toLowerCase());
//        }
//
//        // reduce list
//        Set<String> reducedTargetList = new HashSet<>(lowerCaseList);
//
//        // override target list with unique version
//        FileUtils.writeLines(new File(this.targetListFileInternalPath), reducedTargetList);
    }
}
