package bio.fkaiser.fit3d.web.beans.view;

import bio.fkaiser.fit3d.web.beans.session.SessionManager;
import bio.fkaiser.fit3d.web.io.DirectoryZip;
import bio.fkaiser.fit3d.web.model.*;
import bio.fkaiser.fit3d.web.utilities.Fit3DWebConstants;
import de.bioforscher.singa.structure.algorithms.superimposition.SubstructureSuperimposition;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureWriter;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class ResultView implements Serializable {

    private static final long serialVersionUID = 6674743107376192171L;
    private static final Logger logger = LoggerFactory.getLogger(ResultView.class);

    private Fit3DJob job;
    private List<Fit3DMatch> matches;
    private StructuralMotif motif;
    private MotifAnalysis motifAnalysis;
    private int matchCount;
    private int structureCount;
    private SessionManager sessionManager;
    private LineChartModel rmsdChart;
    private String currentPvLabel;
    private long intraCount;
    private long interCount;
    private double intraCountRel;
    private double interCountRel;
    private double maximalRrmsd;
    private double minimalRrmsd;
    private String currentMatchExternalPdb;
    private String currentQueryExternalPdb;
    private Path motifPath;

    public StreamedContent getHitPdbFile(Fit3DMatch h) throws FileNotFoundException {

        // TODO implement
//		String fileName = InputOutputUtils.getOutputFilename(h) + ".pdb";
//		File motifFile = new File(this.job.getWorkingDirectory() + "/structures/" + fileName);

//		return new DefaultStreamedContent(new FileInputStream(motifFile), "chemical/pdb", fileName);
        return null;
    }

    @PostConstruct
    public void init() {
//        // for some unexplainable reason the ResultView is instantiated when
//        // clicking the Extract-btn within the ExtractView
//        if (FacesContext.getCurrentInstance().getViewRoot().getViewId().contains("extract.xhtml")) {
//            logger.warn("illegal access to result view from extract view - fix me - urgent - seriously");
//            return;
//        }
        job = sessionManager.getSelectedJob();
        if (job != null) {
            updateResults();
        }
    }

    /**
     * redirects to job selection if no job is associated to prevent direct
     * access to result page
     *
     * @throws IOException
     */
    public void redirectToJobSelection() throws IOException {
        if (job == null) {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect(externalContext.getRequestContextPath() + "/jobs");
        }
    }

    public void showAllAgainstOne() throws IOException {

        // write matches of job
        job.writeMatches();

        // create single PDB file of all aligned matches
        Path singlePdbFilePath = job.getJobPath().resolve("all.pdb");
        if (!singlePdbFilePath.toFile().exists()) {
            Files.createFile(singlePdbFilePath);
            // create single PDB file
            final int[] fileCounter = {0};
            Files.walk(job.getJobPath().resolve("matches")).filter(path -> path.toFile().isFile()).forEach(path -> {
                if (fileCounter[0] > Fit3DWebConstants.ALL_AGAINST_ONE_LIMIT) {
                    return;
                } else {
                    try {
                        String fileContent = Files.lines(path)
                                                  .collect(Collectors.joining("\n"));
                        Files.write(singlePdbFilePath, fileContent.getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        logger.warn("failed to read {} and append to single structure file", path, e);
                    }
                }
                fileCounter[0]++;
            });
        }

        logger.info("single structure file {} created", singlePdbFilePath);

//         TODO implement
//        String pdbPath, motifPath;
//        if (job instanceof Fit3DJobDummy) {
//            pdbPath = "'data/example/all.pdb'";
//            motifPath = "'data/example/motif.pdb'";
//            // RequestContext.getCurrentInstance().execute(
//            // "viewer({ pdb : 'data/example/all.pdb', clear: true, style :
//            // 'lines', additionalPdb : { pdb : 'data/example/motif.pdb', style
//            // : 'sticks', color : 'green', labelColor : 'rgb(255, 255, 255)',
//            // labelSize : 22, labels : true, labelStyle : 'bold' } })");
//            // .execute("showAllAgainstOneAlignment('data/example/all.pdb','data/example/motif.pdb')");
//        } else {
//            pdbPath = "'data/" + sessionManager.getSessionIdentifier() + "/" + job.getId() + "/all.pdb'";
//            motifPath = "'data/" + sessionManager.getSessionIdentifier() + "/" + job.getId() + "/motif.pdb'";
            // RequestContext.getCurrentInstance()
            // .execute("showAllAgainstOneAlignment('data/" +
            // this.sessionManager.getId() + "/"
            // + this.job.getId() + "/all.pdb','data/" +
            // this.sessionManager.getId() + "/"
            // + this.job.getId() + "/motif.pdb')");
            // .execute("viewer({ pdb : 'data/" +
            // this.sessionManager.getId() + "/" + this.job.getId()
            // + "/all.pdb', " + "additionalPdbs : ['data/" +
            // this.sessionManager.getId() + "/"
            // + this.job.getId() + "/motif.pdb'], style : 'lines' })");
            // .execute("viewer({ pdb : 'data/" + this.sessionManager.getId()
            // + "/" + this.job.getId() + "/all.pdb', clear : true, style :
            // 'lines', additionalPdb : { pdb : 'data/"
            // + this.sessionManager.getId() + "/" + this.job.getId()
            // + "/motif.pdb', style : 'sticks', color : 'green', labelColor :
            // 'rgb(255, 255, 255)', labelSize : 22, labels : true, labelStyle :
            // 'bold' } })");
//        }
        String executionString = "viewer({ pdb : '" + SessionManager.relativizePath(singlePdbFilePath)
                   + "', clear: true, style : 'lines', additionalPdb : { pdb : '" + SessionManager.relativizePath(motifPath)
                   + "', style : 'sticks', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labels : true, labelStyle : 'bold' } })";
        RequestContext.getCurrentInstance().execute(executionString);

        // update currently shown
        currentPvLabel = "all against <span style=\"color:#00b04b\">query motif</span>";

        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public void showGlobalAlignment(Fit3DMatch h) throws IOException {

        // TODO implement
//		Structure matchStructure = new TargetStructureParser(Fit3dConstants.PDB_DIR, h.getPdbId()).parseStructure();
//		FileUtils.writeStringToFile(new File(this.job.getWorkingDirectory() + "/aligned_match.pdb"),
//				matchStructure.toPDB());
//
//		Structure queryStructure = new QueryStructureParser(Fit3dConstants.PDB_DIR,
//				this.job.getWorkingDirectory() + "/extract.pdb").parseStructure();
//
//		calculateAlignment(h, queryStructure);
//
//		FileUtils.writeStringToFile(new File(this.job.getWorkingDirectory() + "/aligned_query.pdb"),
//				queryStructure.toPDB());
//
//		Structure queryMotif = new QueryStructureParser(Fit3dConstants.PDB_DIR,
//				this.job.getWorkingDirectory() + "/motif.pdb").parseStructure();
//
//		if (!(this.job instanceof Fit3DJobDummy)) {
//			this.currentMatchExternalPdb = "data/" + this.sessionManager.getId() + "/" + this.job.getId()
//					+ "/aligned_match.pdb";
//			this.currentQueryExternalPdb = "data/" + this.sessionManager.getId() + "/" + this.job.getId()
//					+ "/aligned_query.pdb";
//		} else {
//
//			this.currentMatchExternalPdb = "data/example/aligned_match.pdb";
//			this.currentQueryExternalPdb = "data/example/aligned_query.pdb";
//		}
//
//		StringBuffer sb1 = new StringBuffer();
//		for (HitAminoAcid haa : h.getAminoAcids()) {
//			if (sb1.length() > 0)
//				sb1.append(",");
//			sb1.append("'" + haa.getChainId() + "-" + haa.getResidueType() + haa.getResidueNumber() + "'");
//		}
//
//		StringBuffer sb2 = new StringBuffer();
//		for (Chain c : queryMotif.getChains()) {
//			for (Group g : c.getAtomGroups()) {
//				if (g instanceof AminoAcid) {
//					if (sb2.length() > 0)
//						sb2.append(",");
//					sb2.append("'" + InputOutputUtils.getAminoAcidString((AminoAcid) g) + "'");
//				}
//			}
//		}
//
//		RequestContext.getCurrentInstance()
//				.execute("viewer({ pdb : '" + this.currentMatchExternalPdb + "', motif : [" + sb1.toString()
//						+ "], clear : true, style : 'cartoon', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labelStyle : 'bold', alternatePosition : true, additionalPdb : { pdb
// : '"
//						+ this.currentQueryExternalPdb + "', style : 'cartoon', color : 'lightgrey', motif : ["
//						+ sb2.toString() + "], labelSize : 22, labelStyle : 'bold' } })");
//
//		this.currentPv = matchStructure.getIdentifier() + " against <span style=\"color:#00b04b\">"
//				+ queryStructure.getIdentifier() + "</span>";

        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public void showInStructure(Fit3DMatch h) throws IOException {

        // create PDB directory if it not exists
        File pdbDir = new File(System.getProperty("os.name").startsWith("Win")
                               ? FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "data/pdb/"
                               : FacesContext.getCurrentInstance().getExternalContext().getRealPath("data/pdb/"));
        if (!pdbDir.exists()) {

            pdbDir.mkdirs();
        }

        // TODO implement
//		String pdbId = h.getPdbId();
//		String pdbFilePath = System.getProperty("os.name").startsWith("Win")
//				? FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "data/pdb/" + pdbId + ".pdb"
//				: FacesContext.getCurrentInstance().getExternalContext().getRealPath("data/pdb/" + pdbId + ".pdb");
//		File pdbFile = new File(pdbFilePath);
//
//		if (!pdbFile.exists()) {
//
//			StructureParser sp = new TargetStructureParser(Fit3dConstants.PDB_DIR, pdbId);
//
//			List<AminoAcid> aminoAcids = sp.parse();
//
//			List<Atom> atoms = new ArrayList<>();
//			aminoAcids.stream().forEach(a -> atoms.addAll(a.getAtoms()));
//
//			LogHandler.LOG.info("writing PDB file for structure view to local storage");
//
//			BufferedWriter out = new BufferedWriter(new FileWriter(pdbFile));
//
//			// write all atoms to PDB file
//			for (Atom atom : atoms) {
//
//				out.write(atom.toPDB());
//			}
//
//			// close buffered writer
//			out.close();
//		}

        // extract motif from PDB file
        // extractMotifFromPdb(pdbFilePath, h);

        // StringBuffer motifString = new StringBuffer("{residues:[");
        // for (HitAminoAcid hAa : h.getAminoAcids()) {
        //
        // motifString.append("{chain:'" + hAa.getChainId() + "',resnum:" +
        // hAa.getResidueNumber() + "},");
        // }
        // motifString.deleteCharAt(motifString.length() - 1);
        // motifString.append("]}");
        //
        // //
        // RequestContext.getCurrentInstance().execute("showInStructureAlignment('data/pdb/"
        // // + pdbId + ".pdb'," + motifString + ")");
        // RequestContext.getCurrentInstance().execute("viewer({ pdb :
        // 'data/pdb/" + pdbId
        // + ".pdb', clear : true, style : 'cartoon', highlight : " +
        // motifString + " })");

        // TODO implement
//		StringBuffer sb = new StringBuffer();
//		for (HitAminoAcid haa : h.getAminoAcids()) {
//			if (sb.length() > 0)
//				sb.append(",");
//			sb.append("'" + haa.getChainId() + "-" + haa.getResidueType() + haa.getResidueNumber() + "'");
//		}
//		// System.out.println(sb.toString());
//		RequestContext.getCurrentInstance()
//				.execute("viewer({ pdb : 'data/pdb/" + pdbId
//						+ ".pdb', clear : true, style : 'cartoon', labelSize : 22, labelStyle : 'bold', motif : ["
//						+ sb.toString() + "] })");
//
//		// update currently shown
//		this.currentPv = pdbId;

        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public void showPairwise(Fit3DMatch h) {

        // TODO implement
//		String pdbPath, motifPath;
//		if (this.job instanceof Fit3DJobDummy) {
//			pdbPath = "'data/example/structures/" + InputOutputUtils.getOutputFilename(h) + ".pdb'";
//			motifPath = "'data/example/motif.pdb'";
//			// RequestContext.getCurrentInstance().execute("showPairwiseAlignment('data/example/structures/"
//			// + InputOutputUtils.getOutputFilename(h) +
//			// ".pdb','data/example/motif.pdb','viewer')");
//			// RequestContext.getCurrentInstance().execute("viewer({ pdb :
//			// 'data/example/structures/" +
//			// InputOutputUtils.getOutputFilename(h)
//			// + ".pdb', style : 'cartoon', clear : true, additionalPdb : { pdb
//			// : 'data/example/motif.pdb', style : 'cartoon' } })");
//		} else {
//			pdbPath = "'data/" + this.sessionManager.getId() + "/" + this.job.getId() + "/structures/"
//					+ InputOutputUtils.getOutputFilename(h) + ".pdb'";
//			motifPath = "'data/" + this.sessionManager.getId() + "/" + this.job.getId() + "/motif.pdb'";
//			// RequestContext.getCurrentInstance()
//			// .execute("showPairwiseAlignment('data/" +
//			// this.sessionManager.getId() + "/" + this.job.getId()
//			// + "/structures/" + InputOutputUtils.getOutputFilename(h)
//			// + ".pdb','data/"
//			// + this.sessionManager.getId() + "/" + this.job.getId()
//			// + "/motif.pdb','viewer')");
//			// .execute("viewer({ pdb : 'data/" + this.sessionManager.getId()
//			// + "/" + this.job.getId() + "/structures/" +
//			// InputOutputUtils.getOutputFilename(h)
//			// + ".pdb', additionalPdbs : ['data/" +
//			// this.sessionManager.getId() + "/" + this.job.getId() +
//			// "/motif.pdb'] })");
//		}

        // TODO implement
//		RequestContext.getCurrentInstance().execute("viewer({ pdb : " + pdbPath
//				+ ", style : 'sticks', labels : true, labelSize : 22, labelStyle : 'bold', clear : true, additionalPdb : { pdb : "
//				+ motifPath
//				+ ", style : 'sticks', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labels : true, labelStyle : 'bold', alternatePosition : true } })");

        // update currently shown
        currentPvLabel = h.toString();
        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public List<Fit3DMatchAnnotation> getAnnotations(Fit3DMatch match) {
        List<String> chainIdentifiers = match.getCandidateMotif().getAllLeafSubstructures().stream()
                                             .map(LeafSubstructure::getChainIdentifier)
                                             .distinct()
                                             .collect(Collectors.toList());
        List<Fit3DMatchAnnotation> annotations = new ArrayList<>();
        for (String chainIdentifier : chainIdentifiers) {
            annotations.add(new Fit3DMatchAnnotation(match, chainIdentifier));
        }
        return annotations;
    }

    public void updateResults() {

        // read motif
        motif = StructuralMotif.fromLeafSubstructures(StructureParser.local()
                                                                     .path(job.getParameters().getMotifPath())
                                                                     .parse().getAllLeafSubstructures());
        // write motif
        motifPath = job.getJobPath().resolve("motif.pdb");
        if (!motifPath.toFile().exists()) {
            try {
                StructureWriter.writeLeafSubstructureContainer(motif, motifPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            logger.info("motif written to {}", motifPath);
        }

        motifAnalysis = MotifAnalysis.of(motif);

        if (matches == null && job.isFinished()) {

            // load results
            matches = sessionManager.getSelectedJob().getMatches();

            if (!matches.isEmpty()) {

                // generate result statistics
                analyzeResults();

                // update motif meta data
                RequestContext.getCurrentInstance().update("resultsMetaData");

                // calculate all-against-one alignment
//                showAllAgainstOne();

                // show toolbox
                showToolbox();

                // generate RMSD distribution
                initRmsdDistribution();
            }

//            if (job.getParameters().getExtractPdbFilePath() != null) {

            // TODO implement
//				String id = new QueryStructureParser(Fit3dConstants.PDB_DIR,
//						this.job.getParameters().getExtractPdbFilePath()).parseStructure().getIdentifier();
//				this.extractStructureDescription = (id == null) ? "unknown" : id;

//            }
        }
    }

    private void analyzeResults() {

        matchCount = matches.size();

        // determine number of distinct structures that contain a match
        structureCount = (int) matches.stream()
                                      .map(Fit3DMatch::getSubstructureSuperimposition)
                                      .map(SubstructureSuperimposition::getCandidate)
                                      .filter(leafSubstructures -> leafSubstructures.iterator().hasNext())
                                      .map(leafSubstructures -> leafSubstructures.iterator().next())
                                      .map(LeafSubstructure::getPdbIdentifier)
                                      .distinct()
                                      .count();

        // determine number of intra- and inter-molecular matches
        Map<StructuralMotif.Type, List<StructuralMotif>> matchesByType = matches.stream()
                                                                                .map(Fit3DMatch::getSubstructureSuperimposition)
                                                                                .map(SubstructureSuperimposition::getMappedFullCandidate)
                                                                                .map(StructuralMotif::fromLeafSubstructures)
                                                                                .collect(Collectors.groupingBy(StructuralMotif.Type::determine));
        if (matchesByType.containsKey(StructuralMotif.Type.INTRA)) {
            intraCount = matchesByType.get(StructuralMotif.Type.INTRA).size();
            intraCountRel = (double) intraCount / (double) matchCount;
        }
        if (matchesByType.containsKey(StructuralMotif.Type.INTER)) {
            interCount = matchesByType.get(StructuralMotif.Type.INTER).size();
            interCountRel = (double) interCount / (double) matchCount;
        }

        // determine maximal and minimal RMSD values
        Optional<Fit3DMatch> maximalMatchOptional = matches.stream()
                                                           .max(Comparator.comparingDouble(Fit3DMatch::getRmsd));
        maximalMatchOptional.ifPresent(match -> maximalRrmsd = match.getRmsd());
        Optional<Fit3DMatch> minimalMatchOptional = matches.stream()
                                                           .min(Comparator.comparingDouble(Fit3DMatch::getRmsd));
        minimalMatchOptional.ifPresent(match -> minimalRrmsd = match.getRmsd());
    }

    private void initRmsdDistribution() {

        logger.info("initializing line chart model for job " + job);
        rmsdChart = new LineChartModel();
        rmsdChart.setTitle("RMSD distribution");
        rmsdChart.setLegendPosition("e");
        Axis xAxis = rmsdChart.getAxis(AxisType.X);
        xAxis.setMin(0);
        xAxis.setLabel("RMSD");
        Axis yAxis = rmsdChart.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setLabel("log10 occurrence");
        // yAxis.setMax(10);

        logger.info("calculating RMSD distribution for job " + job);
        RmsdDistribution rmsdDistribution = new RmsdDistribution(matches);

        LineChartSeries series = new LineChartSeries();
        series.setLabel("matches");
        series.setData(rmsdDistribution.getValues());
        rmsdChart.addSeries(series);

    }

    private void showToolbox() {
        RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");
    }

    // TODO title mapping
    public StreamedContent getCsvResults() throws IOException {
        return new DefaultStreamedContent(Files.newInputStream(job.getJobPath().resolve("summary.csv")), "text/csv", "summary.csv");
    }

    public String getCurrentPvLabel() {
        return currentPvLabel;
    }

    public void setCurrentPvLabel(String currentPvLabel) {
        this.currentPvLabel = currentPvLabel;
    }

    public long getInterCount() {
        return interCount;
    }

    public double getInterCountRel() {
        return interCountRel;
    }

    public Object getIntraCount() {
        return intraCount;
    }

    public double getIntraCountRel() {
        return intraCountRel;
    }

    public Fit3DJob getJob() {
        return job;
    }

    public void setJob(Fit3DJob job) {
        this.job = job;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public List<Fit3DMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<Fit3DMatch> matches) {
        this.matches = matches;
    }

    public double getMaximalRrmsd() {
        return maximalRrmsd;
    }

    public double getMinimalRrmsd() {
        return minimalRrmsd;
    }

    public void setMinimalRrmsd(double minimalRrmsd) {
        this.minimalRrmsd = minimalRrmsd;
    }

    public StructuralMotif getMotif() {
        return motif;
    }

    public MotifAnalysis getMotifAnalysis() {
        return motifAnalysis;
    }

    public LineChartModel getRmsdChart() {
        return rmsdChart;
    }

    public void setRmsdChart(LineChartModel rmsdChart) {
        this.rmsdChart = rmsdChart;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // private void extractMotifFromPdb(String pdbFilePath, Hit h)
    // throws IOException {
    //
    // StructureParser sp = new QueryStructureParser(Fit3dConstants.PDB_DIR,
    // pdbFilePath);
    //
    // List<AminoAcid> motifInStructure = new ArrayList<>();
    // for (HitAminoAcid hAa : h.getAminoAcids()) {
    //
    // for (AminoAcid aa : sp.parse()) {
    //
    // if (aa.getChainId().equals(String.valueOf(hAa.getChainId()))
    // && aa.getResidueNumber().getSeqNum() == hAa
    // .getResidueNumber()) {
    //
    // if (hAa.getInsCode() != null
    // && aa.getResidueNumber().getInsCode() != null) {
    //
    // if (hAa.getInsCode().equals(
    // aa.getResidueNumber().getInsCode())) {
    //
    // motifInStructure.add(aa);
    // }
    // } else {
    //
    // motifInStructure.add(aa);
    // }
    // }
    // }
    // }
    //
    // List<Atom> atoms = new ArrayList<>();
    // motifInStructure.stream().forEach(a -> atoms.addAll(a.getAtoms()));
    //
    // LogHandler.LOG
    // .info("writing PDB file for structure view to local storage");
    //
    // BufferedWriter out = new BufferedWriter(new FileWriter(FacesContext
    // .getCurrentInstance().getExternalContext()
    // .getRealPath("data/pdb/motif.pdb")));
    //
    // // write all atoms to PDB file
    // for (Atom atom : atoms) {
    //
    // out.write(atom.toPDB());
    // }
    //
    // // close buffered writer
    // out.close();
    // }

    public int getStructureCount() {
        return structureCount;
    }

    // TODO implement
//	/**
//	 * superimposes two structures by biojava's svd then calculating
//	 * ca-ca-distances of corresponding atoms
//	 *
//	 * @param s1
//	 *            a structure
//	 * @param s2
//	 *            yet another structure
//	 * @param queryMotif2
//	 * @return the rmsd of the alignment
//	 * @throws StructureException
//	 *             svd's internal errors propagated
//	 * @throws IOException
//	 */
//	// TODO: rework, error-handling - not essential though
//	private void calculateAlignment(Hit h, Structure queryStructure) throws StructureException, IOException {
//
//		LogHandler.LOG.info("aligning structure " + queryStructure.getIdentifier() + " against " + h);
//
//		Calc.rotate(queryStructure, h.getRotation());
//		Calc.shift(queryStructure, h.getShift());
//	}

    public StreamedContent getZippedResults() throws IOException {

        Path zipFilePath = job.getJobPath().resolve("results.zip");

        // create directory zip class
        DirectoryZip dz = new DirectoryZip(zipFilePath.toString(), job.getJobPath().toString());

        // ignore files
        List<String> ignoreFiles = new ArrayList<>();

        ignoreFiles.add("all.pdb");
        ignoreFiles.add("results.zip");
        ignoreFiles.add("results.fit");
        ignoreFiles.add("Fit3D.log");
        dz.setIgnoreFiles(true);
        dz.setIgnoreFileList(ignoreFiles);

        // zip directory
        dz.zipRecursively();

        return new DefaultStreamedContent(new FileInputStream(dz.getZipFileName()), "application/octet-stream",
                                          "results.zip");
    }
}
