package de.bioforscher.fit3d.web.views;

import de.bioforscher.fit3d.web.controllers.SessionController;
import de.bioforscher.fit3d.web.core.Fit3DJob;
import de.bioforscher.fit3d.web.core.Fit3DJobDummy;
import de.bioforscher.fit3d.web.core.RmsdDistribution;
import de.bioforscher.fit3d.web.io.DirectoryZip;
import de.bioforscher.fit3d.web.utilities.LoadMonitor;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
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
import java.util.ArrayList;
import java.util.List;

public class ResultView implements Serializable {

    private static final long serialVersionUID = 6674743107376192171L;
    private static final Logger logger = LoggerFactory.getLogger(ResultView.class);


    private static final int ALL_AGAINT_ONE_LIMIT = 1000;
    private int enqueuedCount;
    private Fit3DJob job;
    private List<Fit3DMatch> results;
    private SessionController sessionController;
    private LineChartModel rmsdChart;
    private int resultCount;
    private int resultStructures;

    private String currentPv;

    private long intraCount;

    private long interCount;
    private double intraCountRel;
    private double interCountRel;
    private double maxLrmsd;
    private double minLrmsd;
    private String currentMatchExternalPdb;
    private String currentQueryExternalPdb;
    private String extractStructureDescription;

    public String convertToExpasy(Fit3DMatch h) {

        // TODO implement
//		String ecNumber = h.getEcNumber();
//		if (h.getEcNumber().equals("?")) {
//
//			return "?";
//		} else {
//
//			String baseUrl = "http://enzyme.expasy.org/EC/";
//
//			if (Pattern.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+", ecNumber)) {
//
//				return baseUrl + ecNumber;
//			}
//
//			if (Pattern.matches("[0-9]+\\.[0-9]+\\.[0-9]+", ecNumber)) {
//
//				return baseUrl + ecNumber + ".-";
//			}
//
//			if (Pattern.matches("[0-9]+\\.[0-9]+", ecNumber)) {
//
//				return baseUrl + ecNumber + ".-.-";
//			}
//
//			if (Pattern.matches("[0-9]+\\.", ecNumber)) {
//
//				return baseUrl + ecNumber + ".-.-.-";
//			}
//		}

        return "?";
    }

    // TODO title mapping
    public StreamedContent getCsvResults() throws IOException {
        return new DefaultStreamedContent(Files.newInputStream(job.getWorkingDirectoryPath().resolve("summary.csv")), "text/csv", "summary.csv");
    }

    public String getCurrentPv() {
        return currentPv;
    }

    public void setCurrentPv(String currentPv) {
        this.currentPv = currentPv;
    }

    public int getEnqueuedCount() {

        enqueuedCount = LoadMonitor.getInstance().getEnqueued();

        return enqueuedCount;
    }

    public String getExtractStructureDescription() {
        return extractStructureDescription;
    }

    public void setExtractStructureDescription(String extractStructureDescription) {
        this.extractStructureDescription = extractStructureDescription;
    }

    public StreamedContent getHitPdbFile(Fit3DMatch h) throws FileNotFoundException {

        // TODO implement
//		String fileName = InputOutputUtils.getOutputFilename(h) + ".pdb";
//		File motifFile = new File(this.job.getWorkingDirectory() + "/structures/" + fileName);

//		return new DefaultStreamedContent(new FileInputStream(motifFile), "chemical/pdb", fileName);
        return null;
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

    public double getMaxLrmsd() {
        return maxLrmsd;
    }

    public double getMinLrmsd() {
        return minLrmsd;
    }

    public void setMinLrmsd(double minLrmsd) {
        this.minLrmsd = minLrmsd;
    }

    public int getResultCount() {
        return resultCount;
    }

    public List<Fit3DMatch> getResults() {
        return results;
    }

    public void setResults(List<Fit3DMatch> results) {
        this.results = results;
    }

    public int getResultStructures() {
        return resultStructures;
    }

    public void setResultStructures(int resultStructures) {
        this.resultStructures = resultStructures;
    }

    public LineChartModel getRmsdChart() {
        return rmsdChart;
    }

    public void setRmsdChart(LineChartModel rmsdChart) {
        this.rmsdChart = rmsdChart;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public StreamedContent getZippedResults() throws IOException {

        Path zipFilePath = job.getWorkingDirectoryPath().resolve("results.zip");

        // create directory zip class
        DirectoryZip dz = new DirectoryZip(zipFilePath.toString(), job.getWorkingDirectoryPath().toString());

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

    @PostConstruct
    public void init() {
        // for some unexplainable reason the ResultView is instantiated when
        // clicking the Extract-btn within the ExtractView
        if (FacesContext.getCurrentInstance().getViewRoot().getViewId().contains("extract.xhtml")) {
            logger.warn("illegal access to result view from extract view - fix me - urgent - seriously");
            return;
        }
        job = sessionController.getSelectedJob();

        if (job != null) {
            try {
                updateResults();
            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

    /**
     * if not yet done, a single PDB file for all result structures is created
     * and aligned to the query motif
     *
     * @throws IOException
     */
    public void showAllAgainstOne() throws IOException {

//        File structureDir = new File(job.getWorkingDirectory() + "/structures/");
//        File singlePdbFile = new File(job.getWorkingDirectory() + "/all.pdb");

        // cancel if single PDB file was already created
//        if (!singlePdbFile.exists()) {
//
//            // create single PDB file
//            int fileCounter = 0;
//            for (File f : structureDir.listFiles()) {
//
//                if (fileCounter > ALL_AGAINT_ONE_LIMIT) {
//                    break;
//                }
//                // TODO implement
////				String fileContent = FileUtils.readFileToString(f);
////				FileUtils.write(singlePdbFile, fileContent, true);
////				fileCounter++;
//            }
//        }

        String pdbPath, motifPath;
        if (job instanceof Fit3DJobDummy) {
            pdbPath = "'data/example/all.pdb'";
            motifPath = "'data/example/motif.pdb'";
            // RequestContext.getCurrentInstance().execute(
            // "viewer({ pdb : 'data/example/all.pdb', clear: true, style :
            // 'lines', additionalPdb : { pdb : 'data/example/motif.pdb', style
            // : 'sticks', color : 'green', labelColor : 'rgb(255, 255, 255)',
            // labelSize : 22, labels : true, labelStyle : 'bold' } })");
            // .execute("showAllAgainstOneAlignment('data/example/all.pdb','data/example/motif.pdb')");
        } else {
//            pdbPath = "'data/" + sessionController.getSessionIdentifier() + "/" + job.getId() + "/all.pdb'";
//            motifPath = "'data/" + sessionController.getSessionIdentifier() + "/" + job.getId() + "/motif.pdb'";
            // RequestContext.getCurrentInstance()
            // .execute("showAllAgainstOneAlignment('data/" +
            // this.sessionController.getId() + "/"
            // + this.job.getId() + "/all.pdb','data/" +
            // this.sessionController.getId() + "/"
            // + this.job.getId() + "/motif.pdb')");
            // .execute("viewer({ pdb : 'data/" +
            // this.sessionController.getId() + "/" + this.job.getId()
            // + "/all.pdb', " + "additionalPdbs : ['data/" +
            // this.sessionController.getId() + "/"
            // + this.job.getId() + "/motif.pdb'], style : 'lines' })");
            // .execute("viewer({ pdb : 'data/" + this.sessionController.getId()
            // + "/" + this.job.getId() + "/all.pdb', clear : true, style :
            // 'lines', additionalPdb : { pdb : 'data/"
            // + this.sessionController.getId() + "/" + this.job.getId()
            // + "/motif.pdb', style : 'sticks', color : 'green', labelColor :
            // 'rgb(255, 255, 255)', labelSize : 22, labels : true, labelStyle :
            // 'bold' } })");
        }
//        RequestContext.getCurrentInstance().execute("viewer({ pdb : " + pdbPath
//                                                    + ", clear: true, style : 'lines', additionalPdb : { pdb : " + motifPath
//                                                    + ", style : 'sticks', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labels : true, labelStyle : 'bold' } })");

        // update currently shown
        currentPv = "all against <span style=\"color:#00b04b\">query motif</span>";

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
//			this.currentMatchExternalPdb = "data/" + this.sessionController.getId() + "/" + this.job.getId()
//					+ "/aligned_match.pdb";
//			this.currentQueryExternalPdb = "data/" + this.sessionController.getId() + "/" + this.job.getId()
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
//			pdbPath = "'data/" + this.sessionController.getId() + "/" + this.job.getId() + "/structures/"
//					+ InputOutputUtils.getOutputFilename(h) + ".pdb'";
//			motifPath = "'data/" + this.sessionController.getId() + "/" + this.job.getId() + "/motif.pdb'";
//			// RequestContext.getCurrentInstance()
//			// .execute("showPairwiseAlignment('data/" +
//			// this.sessionController.getId() + "/" + this.job.getId()
//			// + "/structures/" + InputOutputUtils.getOutputFilename(h)
//			// + ".pdb','data/"
//			// + this.sessionController.getId() + "/" + this.job.getId()
//			// + "/motif.pdb','viewer')");
//			// .execute("viewer({ pdb : 'data/" + this.sessionController.getId()
//			// + "/" + this.job.getId() + "/structures/" +
//			// InputOutputUtils.getOutputFilename(h)
//			// + ".pdb', additionalPdbs : ['data/" +
//			// this.sessionController.getId() + "/" + this.job.getId() +
//			// "/motif.pdb'] })");
//		}

        // TODO implement
//		RequestContext.getCurrentInstance().execute("viewer({ pdb : " + pdbPath
//				+ ", style : 'sticks', labels : true, labelSize : 22, labelStyle : 'bold', clear : true, additionalPdb : { pdb : "
//				+ motifPath
//				+ ", style : 'sticks', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labels : true, labelStyle : 'bold', alternatePosition : true } })");

        // update currently shown
        currentPv = h.toString();
        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public void updateResults() throws ClassNotFoundException, IOException {
        if (results == null && job.isFinished()) {

            // load results
            results = sessionController.getResultsForSelectedJob();

            if (!results.isEmpty()) {

                // generate result statistics
                analyzeResults();

                // update motif meta data
                RequestContext.getCurrentInstance().update("resultsMetaData");

                // calculate all-against-one alignment
                showAllAgainstOne();

                // show toolbox
                showToolbox();

                // generate RMSD distribution
                initRmsdDistribution();
            }

            if (job.getParameters().getExtractPdbFilePath() != null) {

                // TODO implement
//				String id = new QueryStructureParser(Fit3dConstants.PDB_DIR,
//						this.job.getParameters().getExtractPdbFilePath()).parseStructure().getIdentifier();
//				this.extractStructureDescription = (id == null) ? "unknown" : id;

            }
        }
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

    private void analyzeResults() {

        // TODO implement
//		this.resultCount = this.results.size();
//
//		Set<String> resultsStructureSet = new HashSet<>();
//		this.results.parallelStream().forEach(e -> resultsStructureSet.add(e.getPdbId().toLowerCase()));
//		this.resultStructures = resultsStructureSet.size();
//
//		this.intraCount = this.results.parallelStream().filter(element -> element.getType().equals("intra")).count();
//		this.intraCountRel = (double) this.intraCount / (double) this.resultCount;
//
//		this.interCount = this.results.parallelStream().filter(element -> element.getType().equals("inter")).count();
//		this.interCountRel = (double) this.interCount / (double) this.resultCount;
//
//		this.maxLrmsd = this.results.stream().max((h1, h2) -> Double.compare(h1.getRmsd(), h2.getRmsd())).get()
//				.getRmsd();
//
//		this.minLrmsd = this.results.stream().min((h1, h2) -> Double.compare(h1.getRmsd(), h2.getRmsd())).get()
//				.getRmsd();
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

    private void initRmsdDistribution() {

        logger.info("initializing line chart model for job " + job);
        rmsdChart = new LineChartModel();
        rmsdChart.setTitle("LRMSD distribution");
        rmsdChart.setLegendPosition("e");
        Axis xAxis = rmsdChart.getAxis(AxisType.X);
        xAxis.setMin(0);
        xAxis.setLabel("LRMSD");
        Axis yAxis = rmsdChart.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setLabel("log10 occurrence");
        // yAxis.setMax(10);

        logger.info("calculating RMSD distribution for job " + job);
        RmsdDistribution rd = new RmsdDistribution(results);

        LineChartSeries series = new LineChartSeries();
        series.setLabel("matches");
        series.setData(rd.getValues());
        rmsdChart.addSeries(series);

    }

    private void showToolbox() {
        // expand toolbox when finished
        RequestContext.getCurrentInstance().execute("PF('mainContainer').show('east')");
    }
}
