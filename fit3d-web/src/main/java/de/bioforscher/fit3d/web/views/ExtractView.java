package de.bioforscher.fit3d.web.views;

import de.bioforscher.fit3d.web.controllers.SessionController;
import de.bioforscher.fit3d.web.utilities.LogHandler;
import de.bioforscher.fit3d.web.utilities.enums.MotifComplexity;
import org.omnifaces.util.Faces;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.*;

public class ExtractView implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7092080767526243192L;
    private String pdbId;
    private String workingDirectory;
    private SessionController sessionController;
    private String[] extractAminoAcids;
    private String extractPdbPath;
    private String externalExtractPdbPath;
    private String[] aaIdentifiers;
    private boolean pdbUploaded;
    private String extractedMotifPath;
    private String externalExtractedMotifPath;
    private boolean motifExtracted;
    private String extractedMotifSeq;
    private String pdbName;
    private boolean blocked;
    private int motifAminoAcidCount;
    private double maxExtent;
    private String motifSeq;
    private String motifType;

    private MotifComplexity motifComplexity;

    /**
     * show in 2nd PV instance the extracted motif (and extract it in the first
     * place)
     *
     * @throws IOException
     */
    public void extractMotif() {

        if (this.extractAminoAcids.length < 2) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                    "Please define at least two amino acids.");
            FacesContext.getCurrentInstance().addMessage(null, message);

            RequestContext.getCurrentInstance().update("extractForm:messages");
        } else {
            // TODO directly extract motif and load only motif structure to
            // TODO implement
            // viewer
//			StructureParser sp = new QueryStructureParser(Fit3dConstants.PDB_DIR, this.extractPdbPath);
//
//			List<AminoAcid> aminoAcids = sp.parse();
//
//			List<AminoAcid> motif = new ArrayList<>();
//
//			for (String s : this.extractAminoAcids) {
//				/**
//				 * S, 7.1.15: since we replaced whitespaces of unnamed chains
//				 * with ?, they need to be formatted back to actually find
//				 * chains
//				 */
//				String chainId = (s.split("-")[0].equals("?") ? " " : s.split("-")[0]);
//
//				// String resType = s.split("-")[1].substring(0, 1);
//				int resNum = Integer.valueOf(s.split("-")[1].replaceAll("[A-za-z]*", ""));
//
//				aminoAcids.stream().filter(aa -> aa.getChainId().equals(chainId))
//						.filter(aa -> (aa.getResidueNumber().getSeqNum() == resNum)).forEach(aa -> motif.add(aa));
//
//			}
//
//			List<Atom> motifAtomsList = new ArrayList<>();
//
//			for (AminoAcid aminoAcid : motif) {
//
//				motifAtomsList.addAll(aminoAcid.getAtoms());
//			}
//
//			int number = FileUtils
//					.listFiles(new File(this.workingDirectory), FileFilterUtils.prefixFileFilter("motif"), null).size();
//			this.extractedMotifPath = this.workingDirectory + "/motif" + number + ".pdb";
//			this.externalExtractedMotifPath = "data/" + this.sessionController.getId() + "/motif" + number + ".pdb";
//
//			LogHandler.LOG.info("writing extracted motif " + this.extractedMotifPath);
//			BufferedWriter out = new BufferedWriter(new FileWriter(this.extractedMotifPath));

            // write all atoms to PDB file
            // out.write("REMARK SOURCE " + this.pdbName + "\n");
            // for (Atom atom : motifAtomsList) {
            //
            // out.write(atom.toPDB());
            // }
//			StringBuffer sb = new StringBuffer();
//			sb.append("REMARK SOURCE " + this.pdbName + "\n");
//			for (Atom atom : motifAtomsList) {
//				sb.append(atom.toPDB());
//			}
//
//			out.write(sb.toString());
//			this.motifExtracted = true;
//
//			// close buffered writer
//			out.close();

            // TODO implement
//            analyzeExtractedMotif();

            RequestContext.getCurrentInstance().update("extractForm:submitMotifButton");
            RequestContext.getCurrentInstance().update("extractForm:downloadMotifButton");
            RequestContext.getCurrentInstance().update("extractForm:motifLabel");
            //
            // StringBuilder motifResNums = new StringBuilder();
            // StringBuilder motifChains = new StringBuilder();
            // for (String s : this.extractAminoAcids) {
            //
            // String chainId = s.split("-")[0];
            // String resType = s.split("-")[1].substring(0, 1);
            // String resNum = s.split("-")[1].replaceAll("[A-za-z]*", "");
            //
            // motifResNums.append(resNum);
            // motifResNums.append(",");
            //
            // motifChains.append(chainId);
            // motifChains.append(",");
            // }
            //
            // motifResNums.deleteCharAt(motifResNums.length() - 1);
            // motifChains.deleteCharAt(motifChains.length() - 1);
            //
            // String pvCommand = "showExtractMotifViewer('"
            // + this.externalExtractPdbPath + "','viewer2',\"" + motifResNums
            // + "\")";
            // System.out.println(pvCommand);
            // //

            // RequestContext.getCurrentInstance().execute("showExtractedMotif('"
            // + this.externalExtractedMotifPath + "')");
            /**
             * S, 7.1.15: use alternate approach to feed data to the protein
             * viewer instead of submitting the generated motif pdb, use the
             * whole pdb file already known to the front end by submitting just
             * the residue ids
             */

            // StringBuffer motifString = new StringBuffer("{residues:[");
            // for (AminoAcid cur : motif)
            // motifString.append("{chain:'" + cur.getChainId() + "',resnum:" +
            // cur.getResidueNumber() + "},");
            // motifString.deleteCharAt(motifString.length() - 1);
            // motifString.append("]}");
            // RequestContext.getCurrentInstance().execute("viewer({ pdb : '" +
            // this.externalExtractPdbPath + "', motif : " + motifString + ")");

            // analyze motif
            // TODO implement
//            analyzeMotif();

            // update motif meta data
            RequestContext.getCurrentInstance().update("motifMetaData");

            // expand toolbox
            RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");

            // for (String line : FileUtils.readLines(new
            // File(this.extractedMotifPath)))
            // System.out.println(line);

            // show in protein viewer
            // System.out.println(sb);
            // RequestContext.getCurrentInstance().execute("viewer({ id :
            // 'motif-viewer', clear : true, labels : true, pdbData : '" + sb +
            // "' })");
            RequestContext.getCurrentInstance()
                          .execute("viewer({ id : 'motif-viewer', pdb : '" + this.externalExtractedMotifPath
                                   + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");
        }
    }

    public String[] getAaIdentifiers() {
        return this.aaIdentifiers;
    }

    public void setAaIdentifiers(String[] aaIdentifiers) {
        this.aaIdentifiers = aaIdentifiers;
    }

    public String getExternalExtractPdbPath() {
        return this.externalExtractPdbPath;
    }

    public void setExternalExtractPdbPath(String externalExtractPdbPath) {
        this.externalExtractPdbPath = externalExtractPdbPath;
    }

    public String[] getExtractAminoAcids() {
        return this.extractAminoAcids;
    }

    public void setExtractAminoAcids(String[] extractAminoAcids) {
        this.extractAminoAcids = extractAminoAcids;
    }

    public StreamedContent getExtractedMotif() throws FileNotFoundException {

        File extractedMotifFile = new File(this.extractedMotifPath);

        return new DefaultStreamedContent(new FileInputStream(extractedMotifFile), "chemical/x-pdb",
                                          "motif_" + this.extractedMotifSeq + ".pdb");
    }

    public String getExtractedMotifSeq() {
        return this.extractedMotifSeq;
    }

    public double getMaxExtent() {
        return this.maxExtent;
    }

    public void setMaxExtent(double maxExtent) {
        this.maxExtent = maxExtent;
    }

    public int getMotifAminoAcidCount() {
        return this.motifAminoAcidCount;
    }

    public void setMotifAminoAcidCount(int motifAminoAcidCount) {
        this.motifAminoAcidCount = motifAminoAcidCount;
    }

    public MotifComplexity getMotifComplexity() {
        return this.motifComplexity;
    }

    public void setMotifComplexity(MotifComplexity motifComplexity) {
        this.motifComplexity = motifComplexity;
    }

    public String getMotifSeq() {
        return this.motifSeq;
    }

    public void setMotifSeq(String motifSeq) {
        this.motifSeq = motifSeq;
    }

    public String getMotifType() {
        return this.motifType;
    }

    public void setMotifType(String motifType) {
        this.motifType = motifType;
    }

    public String getPdbId() {
        return this.pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }

    public String getPdbName() {
        return this.pdbName;
    }

    public SessionController getSessionController() {
        return this.sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public String getWorkingDirectory() {
        return this.workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void handlePdbUpload(FileUploadEvent event) throws IOException {

        LogHandler.LOG.info("trying to copy PDB file to local storage");

        copyFile(this.extractPdbPath, event.getFile().getInputstream());

        // set file name for viewer
        this.pdbName = event.getFile().getFileName();

        RequestContext.getCurrentInstance().execute("PF('extractWizard').next()");

        this.pdbUploaded = true;

    }

    @PostConstruct
    public void init() {
        // unix/windows spacken
        // this.workingDirectory =
        // FacesContext.getCurrentInstance().getExternalContext().getRealPath("data/"
        // + this.sessionController.getId());
        this.workingDirectory = System.getProperty("os.name").startsWith("Win")
                                ? FacesContext.getCurrentInstance().getExternalContext().getRealPath("/data/")
                                  + this.sessionController.getId()
                                : FacesContext.getCurrentInstance().getExternalContext()
                                              .getRealPath("data/" + this.sessionController.getId());

        this.extractPdbPath = this.workingDirectory + "/extract.pdb";
        this.externalExtractPdbPath = "data/" + this.sessionController.getId() + "/extract.pdb";
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isMotifExtracted() {
        return this.motifExtracted;
    }

    /**
     * initial display of the protein structure in the left part
     *
     * @param event
     * @return
     * @throws IOException
     */
    public String onFlowProcess(FlowEvent event) throws IOException {

        if (event.getNewStep().equals("pdb") && event.getOldStep().equals("motif")) {

            // hide toolbox
            RequestContext.getCurrentInstance().execute("PF('mainContainer').hide('east')");
        }

        if (this.pdbId.isEmpty() && !this.pdbUploaded) {

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                    "Please provide a PDB-ID or upload a PDB file.");
            FacesContext.getCurrentInstance().addMessage(null, message);

            RequestContext.getCurrentInstance().update("extractForm:messages");

            return "pdb";
        }

        if (event.getOldStep().equals("pdb")) {

            try {
                // analyze structure
                analyzePdb();

            } catch (IOException e) {

                LogHandler.LOG.warning("could not find PDB-ID " + this.pdbId);

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                        "Could not find a structure for PDB-ID " + this.pdbId);
                FacesContext.getCurrentInstance().addMessage(null, message);

                RequestContext.getCurrentInstance().update("extractForm:messages");

                return "pdb";
            }

            if (this.blocked) {

                RequestContext.getCurrentInstance().update("extractForm:messages");
                return "pdb";
            }

            // clear previous motif definition
            this.motifExtracted = false;
            this.extractedMotifSeq = null;
            this.extractAminoAcids = null;

            // show in viewer
            // RequestContext.getCurrentInstance().execute(
            // "showSimpleProteinViewer('" + this.externalExtractPdbPath
            // + "','viewerStructure')");
            // RequestContext.getCurrentInstance().execute("showProtein('" +
            // this.externalExtractPdbPath + "','viewer',true,false,true)");
            RequestContext.getCurrentInstance().execute(
                    "viewer({ pdb : '" + this.externalExtractPdbPath + "', style : 'cartoon', clear : true })");
        }

        return event.getNewStep();

    }

    public void submitMotif() throws IOException {

        Faces.setFlashAttribute("motifFilePath", this.extractedMotifPath);
        Faces.setFlashAttribute("extractPdbPath", this.extractPdbPath);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(externalContext.getRequestContextPath() + "/submit");
    }

    /**
     * triggers residue highlighting in the left part on changing selections
     */
    public void updateProteinViewer() {
        StringBuffer sb = new StringBuffer();
        for (String eaa : this.extractAminoAcids) {
            sb.append("'" + eaa + "',");
        }
        RequestContext.getCurrentInstance().execute("viewer({ highlight : [" + sb.toString() + "] })");
    }

    private void analyzeExtractedMotif() throws IOException {

        // TODO implement
//		StructureParser sp = new QueryStructureParser(Fit3dConstants.PDB_DIR, this.extractedMotifPath);
//
//		List<AminoAcid> motifAminoAcids = sp.parse();
//
//		// compute motif sequence
//		StringBuilder motifSeq = new StringBuilder();
//		Set<String> chainList = new HashSet<>();
//		for (AminoAcid aa : motifAminoAcids) {
//
//			motifSeq.append(aa.getAminoType());
//			chainList.add(aa.getChainId());
//		}
//
//		// do not accept motifs with none or one amino acid
//		if (motifAminoAcids.size() < 2) {
//
//			// TODO properly handle exception
//			throw new StructureException("motif must contain at least two amino acids");
//		}

        this.extractedMotifSeq = motifSeq.toString();

    }

    private void analyzeMotif() throws IOException {

        // load motif amino acids
//		QueryStructureParser qsp = new QueryStructureParser("", this.extractedMotifPath);
//
//		List<AminoAcid> motifAminoAcids = qsp.parse();
//
//		// count amino acids
//		this.motifAminoAcidCount = motifAminoAcids.size();
//
//		// compute spatial extent
//		this.maxExtent = Math.sqrt(StructureUtils.getMaxSquaredExtent(motifAminoAcids));
//
//		// compute motif sequence
//		StringBuilder motifSeq = new StringBuilder();
//		Set<String> chainList = new HashSet<>();
//		for (AminoAcid aa : motifAminoAcids) {
//
//			motifSeq.append(aa.getAminoType());
//			chainList.add(aa.getChainId());
//		}
//		this.motifSeq = motifSeq.toString();
//
//		// get motif type
//		if (chainList.size() > 1) {
//
//			this.motifType = "inter";
//		} else {
//
//			this.motifType = "intra";
//		}
//
//		// compute average complexity
//		int c;
//		int n = (int) this.maxExtent;
//		int k = this.motifAminoAcidCount - 1;
//		final int min = (k < n - k ? k : n - k);
//		long bin = 1;
//		for (int i = 1; i <= min; i++) {
//			bin *= n;
//			bin /= i;
//			n--;
//		}
//
//		c = (int) (Math.pow(this.motifAminoAcidCount, 2) + bin);
//
//		if (c < 100) {
//
//			this.motifComplexity = MotifComplexity.LOW;
//			this.blocked = false;
//
//			// update submitButton
//			RequestContext.getCurrentInstance().update("extractForm:submitMotifButton");
//		} else if (c > 100 && c < 1000) {
//
//			this.motifComplexity = MotifComplexity.MEDIUM;
//			this.blocked = false;
//
//			// update submitButton
//			RequestContext.getCurrentInstance().update("extractForm:submitMotifButton");
//		} else if (c > 1000) {
//
//			this.motifComplexity = MotifComplexity.HIGH;
//			this.blocked = true;
//
//			// update submitButton
//			RequestContext.getCurrentInstance().update("extractForm:submitMotifButton");
//
//			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
//					"Your motif was rated as too complex. Please define a simpler structure or use our command line implementation.");
//			FacesContext.getCurrentInstance().addMessage(null, message);
//		}
    }

    private void analyzePdb() throws IOException {

        // TODO implement
//		if (!this.pdbUploaded) {
//
//			StructureParser sp = new TargetStructureParser(Fit3dConstants.PDB_DIR, this.pdbId);
//
//			List<String> aaIdentifiers = new ArrayList<>();
//			List<AminoAcid> aminoAcids = sp.parse();
//			for (AminoAcid aa : aminoAcids) {
//
//				aaIdentifiers.add(InputOutputUtils.getAminoAcidString(aa));
//			}
//
//			List<Atom> atoms = new ArrayList<>();
//			aminoAcids.stream().forEach(a -> atoms.addAll(a.getAtoms()));
//
//			LogHandler.LOG.info("writing parsed PDB file to local storage " + this.extractPdbPath);
//
//			File extractPdb = new File(this.extractPdbPath);
//
//			// ensure that data folder exists
//			if (!extractPdb.getParentFile().exists()) {
//
//				extractPdb.getParentFile().mkdirs();
//			}
//
//			BufferedWriter out = new BufferedWriter(new FileWriter(extractPdb));
//
//			out.write(sp.parseStructure().toPDB());
//
//			// close buffered writer
//			out.close();
//
//			this.aaIdentifiers = aaIdentifiers.toArray(new String[aaIdentifiers.size()]);
//
//			// set PDB name for viewer
//			this.pdbName = this.pdbId.toUpperCase();
//
//			this.blocked = false;
//		} else {
//
//			StructureParser sp = new QueryStructureParser(Fit3dConstants.PDB_DIR, this.extractPdbPath);
//
//			List<String> aaIdentifiers = new ArrayList<>();
//			List<AminoAcid> aminoAcids = sp.parse();
//
//			if (aminoAcids.size() < 2) {
//
//				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
//						"Your PDB file is invalid. The source structure for motif extraction must contain at least two valid amino acids.");
//				FacesContext.getCurrentInstance().addMessage(null, message);
//
//				this.blocked = true;
//
//			} else {
//
//				for (AminoAcid aa : aminoAcids) {
//					/**
//					 * S, 7.1.15: replaced to fix issue in case of missing chain
//					 * names
//					 */
//					String s = InputOutputUtils.getAminoAcidString(aa);
//					if (s.startsWith(" -"))
//						s = "?" + s.substring(1);
//					aaIdentifiers.add(s);
//					// aaIdentifiers.add(InputOutputUtils.getAminoAcidString(aa));
//				}
//
//				this.aaIdentifiers = aaIdentifiers.toArray(new String[aaIdentifiers.size()]);
//
//				this.blocked = false;
//			}
//		}
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

        LogHandler.LOG.info("file created: " + outputPath);
    }
}
