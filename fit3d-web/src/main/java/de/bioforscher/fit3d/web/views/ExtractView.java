package de.bioforscher.fit3d.web.views;

import de.bioforscher.fit3d.web.controllers.SessionController;
import de.bioforscher.fit3d.web.utilities.enums.MotifComplexity;
import de.bioforscher.singa.structure.model.identifiers.LeafIdentifier;
import de.bioforscher.singa.structure.model.interfaces.AminoAcid;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.interfaces.Structure;
import de.bioforscher.singa.structure.model.oak.OakStructure;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserException;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureWriter;
import org.omnifaces.util.Faces;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractView implements Serializable {

    private static final long serialVersionUID = 7587796072169171564L;
    private static final Logger logger = LoggerFactory.getLogger(ExtractView.class);

    private SessionController sessionController;

    private Path extractPdbFilePath;
    private String pdbFileName;
    private String[] leafIdentifierStrings;

    private String pdbIdentifier;
    private String[] extractAminoAcids;
    private String externalExtractPdbPath;
    private boolean pdbFileUploaded;
    private String extractedMotifPath;
    private String externalExtractedMotifPath;
    private boolean motifExtracted;
    private String extractedMotifSequence;
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

        if (extractAminoAcids.length < 2) {
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
                          .execute("viewer({ id : 'motif-viewer', pdb : '" + externalExtractedMotifPath
                                   + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");
        }
    }

    public String getExternalExtractPdbPath() {
        return externalExtractPdbPath;
    }

    public void setExternalExtractPdbPath(String externalExtractPdbPath) {
        this.externalExtractPdbPath = externalExtractPdbPath;
    }

    public String[] getExtractAminoAcids() {
        return extractAminoAcids;
    }

    public void setExtractAminoAcids(String[] extractAminoAcids) {
        this.extractAminoAcids = extractAminoAcids;
    }

    public StreamedContent getExtractedMotif() throws FileNotFoundException {

        File extractedMotifFile = new File(extractedMotifPath);

        return new DefaultStreamedContent(new FileInputStream(extractedMotifFile), "chemical/x-pdb",
                                          "motif_" + extractedMotifSequence + ".pdb");
    }

    public String getExtractedMotifSequence() {
        return extractedMotifSequence;
    }

    public double getMaxExtent() {
        return maxExtent;
    }

    public void setMaxExtent(double maxExtent) {
        this.maxExtent = maxExtent;
    }

    public int getMotifAminoAcidCount() {
        return motifAminoAcidCount;
    }

    public void setMotifAminoAcidCount(int motifAminoAcidCount) {
        this.motifAminoAcidCount = motifAminoAcidCount;
    }

    public MotifComplexity getMotifComplexity() {
        return motifComplexity;
    }

    public void setMotifComplexity(MotifComplexity motifComplexity) {
        this.motifComplexity = motifComplexity;
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

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public void handlePdbUpload(FileUploadEvent event) throws IOException {
        extractPdbFilePath = sessionController.getWorkingPath().resolve("extract.pdb");
        logger.info("copying PDB file to local path {}", extractPdbFilePath);
        Files.copy(event.getFile().getInputstream(), extractPdbFilePath);
        // set file name for protein viewer
        pdbFileName = event.getFile().getFileName();
        RequestContext.getCurrentInstance().execute("PF('extractWizard').next()");
        pdbFileUploaded = true;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isMotifExtracted() {
        return motifExtracted;
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

        if (pdbIdentifier.isEmpty() && !pdbFileUploaded) {

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                    "Please provide a PDB-ID or upload a PDB file.");
            FacesContext.getCurrentInstance().addMessage(null, message);

            RequestContext.getCurrentInstance().update("extractForm:messages");

            return "pdb";
        }

        if (event.getOldStep().equals("pdb")) {

            analyzePdb();

//                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
//                                                        "Could not find a structure for PDB-ID " + pdbIdentifier);
//                FacesContext.getCurrentInstance().addMessage(null, message);
//                RequestContext.getCurrentInstance().update("extractForm:messages");
//                return "pdb";

            if (blocked) {
                RequestContext.getCurrentInstance().update("extractForm:messages");
                return "pdb";
            }

            // clear previous motif definition
            motifExtracted = false;
            extractedMotifSequence = null;
            extractAminoAcids = null;

            // show in viewer
            Path webappBasePath = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/"));
            RequestContext.getCurrentInstance().execute("showSimpleProteinViewer('" + webappBasePath.relativize(extractPdbFilePath) + "','viewerStructure')");
            RequestContext.getCurrentInstance().execute("showProtein('" + externalExtractPdbPath + "','viewer',true,false,true)");
            RequestContext.getCurrentInstance().execute("viewer({ pdb : '" + externalExtractPdbPath + "', style : 'cartoon', clear : true })");
        }
        return event.getNewStep();
    }

    public void submitMotif() throws IOException {

        Faces.setFlashAttribute("motifFilePath", extractedMotifPath);
//        Faces.setFlashAttribute("extractPdbPath", extractPdbPath);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(externalContext.getRequestContextPath() + "/submit");
    }

    /**
     * triggers residue highlighting in the left part on changing selections
     */
    public void updateProteinViewer() {
        StringBuffer sb = new StringBuffer();
        for (String eaa : extractAminoAcids) {
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

        extractedMotifSequence = motifSeq.toString();

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

    public String getPdbIdentifier() {
        return pdbIdentifier;
    }

    public void setPdbIdentifier(String pdbIdentifier) {
        this.pdbIdentifier = pdbIdentifier;
    }

    public String[] getLeafIdentifierStrings() {
        return leafIdentifierStrings;
    }

    private void analyzePdb() {
        // read PDB file online with ID
        if (!pdbFileUploaded) {
            try {
                Structure structure = StructureParser.online()
                                                     .pdbIdentifier(pdbIdentifier)
                                                     .everything()
                                                     .parse();
                // generate amino acid identifiers
                leafIdentifierStrings = structure.getAllLeafSubstructures().stream()
                                                 .map(LeafSubstructure::getIdentifier)
                                                 .map(LeafIdentifier::toString).toArray(String[]::new);
                blocked = false;

                // write structure in PDB format
                extractPdbFilePath = sessionController.getWorkingPath().resolve("extract.pdb");
                StructureWriter.writeStructure((OakStructure) structure, extractPdbFilePath);
                logger.info("writing structure to local path {}", extractPdbFilePath);
            } catch (IOException | UncheckedIOException | StructureParserException e) {
                logger.error("failed to parse structure with ID {}", pdbIdentifier, e);
            }
        } else {
            Structure structure = StructureParser.local()
                                                 .path(extractPdbFilePath)
                                                 .everything()
                                                 .parse();

            // block wizard for structures with less than two amino acids
            int aminoAcidCount = (int) structure.getAllLeafSubstructures().stream()
                                                .filter(AminoAcid.class::isInstance).count();
            if (aminoAcidCount < 2) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                        "Your PDB file is invalid. The source structure for motif extraction must contain at least two valid amino acids.");
                FacesContext.getCurrentInstance().addMessage(null, message);
                blocked = true;
            }

            // generate amino acid identifiers
            leafIdentifierStrings = structure.getAllLeafSubstructures().stream()
                                             .map(LeafSubstructure::getIdentifier)
                                             .map(LeafIdentifier::toString).toArray(String[]::new);
        }
    }

    public String getPdbFileName() {
        return pdbFileName;
    }
}
