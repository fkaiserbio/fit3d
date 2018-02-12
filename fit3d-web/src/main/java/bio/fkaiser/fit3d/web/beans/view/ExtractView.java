package bio.fkaiser.fit3d.web.beans.view;

import bio.fkaiser.fit3d.web.Fit3DWebConstants;
import bio.fkaiser.fit3d.web.beans.session.SessionManager;
import bio.fkaiser.fit3d.web.model.MotifAnalysis;
import bio.fkaiser.fit3d.web.model.constant.MotifComplexity;
import de.bioforscher.singa.structure.model.identifiers.LeafIdentifier;
import de.bioforscher.singa.structure.model.interfaces.AminoAcid;
import de.bioforscher.singa.structure.model.interfaces.Structure;
import de.bioforscher.singa.structure.model.oak.OakStructure;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtractView implements Serializable {

    public static final int MINIMAL_MOTIF_SIZE = 2;

    private static final Logger logger = LoggerFactory.getLogger(ExtractView.class);
    private static final long serialVersionUID = -4370242518432523371L;

    private SessionManager sessionManager;

    private String pdbFileName;
    private String pdbIdentifier;

    private Path extractPdbFilePath;
    private Path externalExtractPdbFilePath;

    private String[] leafIdentifierStrings;
    private String[] extractAminoAcids;

    private Path extractedMotifPath;
    private Path externalExtractedMotifPath;
    private StructuralMotif extractedMotif;
    private MotifAnalysis motifAnalysis;

    private boolean pdbFileUploaded;
    private boolean motifExtracted;
    private boolean blocked;

    public void extractMotif() throws IOException {
        if (extractAminoAcids.length < MINIMAL_MOTIF_SIZE) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please define at least two amino acids.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else {
            Structure structure = StructureParser.local()
                                                 .path(extractPdbFilePath)
                                                 .everything()
                                                 .setOptions(Fit3DWebConstants.Singa.STRUCTURE_PARSER_OPTIONS)
                                                 .parse();
            List<LeafIdentifier> leafIdentifiers = new ArrayList<>();
            for (String extractAminoAcid : extractAminoAcids) {
                String[] split = extractAminoAcid.split("-");
                leafIdentifiers.add(LeafIdentifier.fromSimpleString((split[0] + "-" + split[1])));
            }
            extractedMotif = StructuralMotif.fromLeafIdentifiers(structure, leafIdentifiers);

            // determine consecutive number of extracted motif
            int consecutiveCount = (int) Files.walk(sessionManager.getSessionPath())
                                              .filter(path -> path.toFile().getName().startsWith("motif"))
                                              .count() + 1;

            extractedMotifPath = sessionManager.getSessionPath().resolve("motif_" + consecutiveCount + ".pdb");
            logger.info("storing extracted motif to path {}", extractedMotifPath);
            externalExtractedMotifPath = SessionManager.relativizePath(extractedMotifPath);
            StructureWriter.writeLeafSubstructureContainer(extractedMotif, extractedMotifPath);

            motifExtracted = true;

            RequestContext.getCurrentInstance().update("extractForm:submitMotifButton");
            RequestContext.getCurrentInstance().update("extractForm:downloadMotifButton");
            RequestContext.getCurrentInstance().update("extractForm:motifLabel");

            motifAnalysis = MotifAnalysis.of(extractedMotif);

            // block submission if motif is rated as too complex
            if (motifAnalysis.getMotifComplexity() == MotifComplexity.HIGH) {
                blocked = true;
                RequestContext.getCurrentInstance().update("extractForm:submitMotifButton");
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                        "Your motif was rated as too complex to be calculated on the web server. Please use the command line implementation or API.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }

            // update motif meta data
            RequestContext.getCurrentInstance().update("motifMetaData");

            // expand toolbox
            RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");

            // show in protein viewer
            RequestContext.getCurrentInstance().execute("viewer({ id : 'motif-viewer', pdb : '" + externalExtractedMotifPath
                                                        + "', clear : true, labels : true, labelSize : 22, labelStyle : 'bold' })");
        }
    }

    public void handlePdbUpload(FileUploadEvent event) throws IOException {
        extractPdbFilePath = sessionManager.getSessionPath().resolve("extract.pdb");
        logger.info("copying PDB file to local path {}", extractPdbFilePath);
        Files.copy(event.getFile().getInputstream(), extractPdbFilePath);
        // set file name for protein viewer
        pdbFileName = event.getFile().getFileName();
        RequestContext.getCurrentInstance().execute("PF('extractWizard').next()");
        pdbFileUploaded = true;
    }

    public String onFlowProcess(FlowEvent event) {

        if (event.getNewStep().equals("pdb") && event.getOldStep().equals("motif")) {
            // hide toolbox
            RequestContext.getCurrentInstance().execute("PF('mainContainer').hide('east')");
        }

        if (pdbIdentifier.isEmpty() && !pdbFileUploaded) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please provide a PDB-ID or upload a PDB file.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return "pdb";
        }

        if (event.getOldStep().equals("pdb")) {
            handlePdbStructure();

            // handling of the structure failed
            if (externalExtractPdbFilePath == null) {
                RequestContext.getCurrentInstance().update("extractForm:messages");
                return "pdb";
            }
            if (blocked) {
                RequestContext.getCurrentInstance().update("extractForm:messages");
                return "pdb";
            }

            // clear previous motif definition
            motifExtracted = false;
            motifAnalysis = null;
            extractAminoAcids = null;

            // show in viewer
            RequestContext.getCurrentInstance().execute("viewer({pdb : '" + externalExtractPdbFilePath.toString() + "', style : 'cartoon', clear : true })");
        }
        return event.getNewStep();
    }

    public void submitMotif() throws IOException {
        Faces.setFlashAttribute("extractedMotifPath", extractedMotifPath);
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(externalContext.getRequestContextPath() + "/submit");
    }

    public void updateResidueHighlighting() {
        String highlightCommand = "viewer({ highlight : " + Stream.of(extractAminoAcids)
                                                                  .collect(Collectors.joining("','", "['", "']")) + " })";
        RequestContext.getCurrentInstance().execute(highlightCommand);
    }

    private void handlePdbStructure() {
        // read PDB file online with ID
        if (!pdbFileUploaded) {
            try {
                Structure structure = StructureParser.pdb()
                                                     .pdbIdentifier(pdbIdentifier)
                                                     .everything()
                                                     .setOptions(Fit3DWebConstants.Singa.STRUCTURE_PARSER_OPTIONS)
                                                     .parse();
                // generate amino acid identifiers
                leafIdentifierStrings = structure.getAllLeafSubstructures().stream()
                                                 .map(leafSubstructure -> leafSubstructure.getIdentifier().toSimpleString() + "-" + leafSubstructure.getFamily().getThreeLetterCode())
                                                 .toArray(String[]::new);
                blocked = false;

                // write structure in PDB format
                extractPdbFilePath = sessionManager.getSessionPath().resolve("extract.pdb");
                StructureWriter.writeStructure((OakStructure) structure, extractPdbFilePath);
                logger.info("writing structure to local path {}", extractPdbFilePath);
            } catch (IOException | UncheckedIOException | StructureParserException e) {
                logger.error("failed to parse structure with PDB-D {}", pdbIdentifier, e);
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not find a structure for PDB-ID " + pdbIdentifier);
                FacesContext.getCurrentInstance().addMessage(null, message);
                return;
            }
        } else {
            try {
                Structure structure = StructureParser.local()
                                                     .path(extractPdbFilePath)
                                                     .everything()
                                                     .setOptions(Fit3DWebConstants.Singa.STRUCTURE_PARSER_OPTIONS)
                                                     .parse();

                // block wizard for structures with less than two amino acids
                int aminoAcidCount = (int) structure.getAllLeafSubstructures().stream()
                                                    .filter(AminoAcid.class::isInstance).count();
                if (aminoAcidCount < MINIMAL_MOTIF_SIZE) {
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                            "Your PDB file is invalid. The source structure for motif extraction must contain at least two valid amino acids.");
                    FacesContext.getCurrentInstance().addMessage(null, message);
                    blocked = true;
                }

                // generate amino acid identifiers
                leafIdentifierStrings = structure.getAllLeafSubstructures().stream()
                                                 .map(leafSubstructure -> leafSubstructure.getIdentifier().toSimpleString() + "-" + leafSubstructure.getFamily().getThreeLetterCode())
                                                 .toArray(String[]::new);
            } catch (UncheckedIOException | StructureParserException e) {
                logger.error("failed to parse structure {}", extractPdbFilePath, e);
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Your PDB file is invalid. Parsing of the structure failed.");
                FacesContext.getCurrentInstance().addMessage(null, message);
                return;
            }
        }

        // set the external file path of the handled PDB file
        externalExtractPdbFilePath = SessionManager.relativizePath(extractPdbFilePath);
    }

    public String[] getExtractAminoAcids() {
        return extractAminoAcids;
    }

    public void setExtractAminoAcids(String[] extractAminoAcids) {
        this.extractAminoAcids = extractAminoAcids;
    }

    public StreamedContent getExtractedMotif() throws FileNotFoundException {
        return new DefaultStreamedContent(new FileInputStream(extractedMotifPath.toFile()), "chemical/x-pdb", extractedMotif.toString() + ".pdb");
    }

    public String[] getLeafIdentifierStrings() {
        return leafIdentifierStrings;
    }

    public MotifAnalysis getMotifAnalysis() {
        return motifAnalysis;
    }

    public String getPdbFileName() {
        return pdbFileName;
    }

    public String getPdbIdentifier() {
        return pdbIdentifier;
    }

    public void setPdbIdentifier(String pdbIdentifier) {
        this.pdbIdentifier = pdbIdentifier;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
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
}
