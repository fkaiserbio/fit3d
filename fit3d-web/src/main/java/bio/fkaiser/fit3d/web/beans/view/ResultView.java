package bio.fkaiser.fit3d.web.beans.view;

import bio.fkaiser.fit3d.web.Fit3DWebConstants;
import bio.fkaiser.fit3d.web.beans.session.SessionManager;
import bio.fkaiser.fit3d.web.io.DirectoryZip;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import bio.fkaiser.fit3d.web.model.Fit3DMatchAnnotation;
import bio.fkaiser.fit3d.web.model.MotifAnalysis;
import bio.fkaiser.fit3d.web.model.RmsdDistribution;
import de.bioforscher.singa.structure.algorithms.superimposition.SubstructureSuperimposition;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import de.bioforscher.singa.structure.model.interfaces.Structure;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParserOptions;
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
    private Path motifPath;

    public StreamedContent getMatchPDBFile(Fit3DMatch match) throws FileNotFoundException {

        // write matches of job
        job.writeMatches();

        String fileName = match.getSubstructureSuperimposition().getStringRepresentation() + ".pdb";
        Path matchPath = job.getJobPath().resolve("matches").resolve(fileName);

        return new DefaultStreamedContent(new FileInputStream(matchPath.toFile()), "chemical/pdb", fileName);
    }

    @PostConstruct
    public void init() {
        job = sessionManager.getSelectedJob();
        if (job != null) {
            updateResults();
        }
    }

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

        String executionString = "viewer({ pdb : '" + SessionManager.relativizePath(singlePdbFilePath)
                                 + "', clear: true, style : 'lines', additionalPdb : { pdb : '" + SessionManager.relativizePath(motifPath)
                                 + "', style : 'sticks', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labels : true, labelStyle : 'bold' } })";

        RequestContext.getCurrentInstance().execute(executionString);

        // update currently shown
        currentPvLabel = "all against <span style=\"color:#00b04b\">query motif</span>";

        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public void showGlobalAlignment(Fit3DMatch match) throws IOException {

        logger.info("calculating global alignment for match {}", match);

        Structure motifStructure = StructureParser.pdb()
                                                  .pdbIdentifier(motif.getFirstLeafSubstructure().getPdbIdentifier())
                                                  .everything()
                                                  .parse();

        Structure matchStructure = StructureParser.pdb()
                                                  .pdbIdentifier(match.getCandidateMotif().getFirstLeafSubstructure().getPdbIdentifier())
                                                  .everything()
                                                  .setOptions(StructureParserOptions.withSettings(StructureParserOptions.Setting.OMIT_HYDROGENS))
                                                  .parse();

        // write structures
        Path alignedMotifStructurePath = job.getJobPath().resolve("aligned_query.pdb");
        StructureWriter.writeLeafSubstructureContainer(motifStructure, alignedMotifStructurePath);

        // apply superimposition to global match structure
        Path alignedMotifStructureExternalPath = SessionManager.relativizePath(alignedMotifStructurePath);
        SubstructureSuperimposition substructureSuperimposition = match.getSubstructureSuperimposition();
        List<LeafSubstructure<?>> superimposedMatchStructure = substructureSuperimposition.applyTo(matchStructure.getAllLeafSubstructures());

        Path alignedMatchPath = job.getJobPath().resolve("aligned_match.pdb");
        StructureWriter.writeLeafSubstructures(superimposedMatchStructure, alignedMatchPath);
        Path alignedMatchExternalPath = SessionManager.relativizePath(alignedMatchPath);

        String matchMotifString = match.getCandidateMotif().getAllLeafSubstructures().stream()
                                       .map(leafSubstructure -> leafSubstructure.getIdentifier().toSimpleString() + "-" + leafSubstructure.getFamily().getThreeLetterCode())
                                       .collect(Collectors.joining("','", "['", "']"));

        String motifString = motif.getAllLeafSubstructures().stream()
                                  .map(leafSubstructure -> leafSubstructure.getIdentifier().toSimpleString() + "-" + leafSubstructure.getFamily().getThreeLetterCode())
                                  .collect(Collectors.joining("','", "['", "']"));

        String executionString = "viewer({ pdb : '" +
                                 alignedMatchExternalPath +
                                 "', motif : " +
                                 matchMotifString +
                                 ", clear : true, style : 'cartoon', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labelStyle : 'bold', alternatePosition : " +
                                 "true, additionalPdb : { pdb : '" +
                                 alignedMotifStructureExternalPath +
                                 "', style : 'cartoon', color : 'lightgrey', motif : " +
                                 motifString +
                                 ", labelSize : 22, labelStyle : 'bold' } })";

        RequestContext.getCurrentInstance().execute(executionString);

        currentPvLabel = matchStructure.getPdbIdentifier() + " against <span style=\"color:#00b04b\">" + motif.getFirstLeafSubstructure().getPdbIdentifier() + "</span>";

        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public void showInStructure(Fit3DMatch match) throws IOException {

        // parse structure of match
        Structure matchStructure = StructureParser.pdb()
                                                  .pdbIdentifier(match.getCandidateMotif().getFirstLeafSubstructure().getPdbIdentifier())
                                                  .everything()
                                                  .setOptions(StructureParserOptions.withSettings(StructureParserOptions.Setting.OMIT_HYDROGENS))
                                                  .parse();

        Path matchStructurePath = job.getJobPath().resolve("pdb").resolve(matchStructure.getPdbIdentifier() + ".pdb");
        StructureWriter.writeLeafSubstructureContainer(matchStructure, matchStructurePath);
        Path matchStructureExternalPath = SessionManager.relativizePath(matchStructurePath);

        String motifString = match.getCandidateMotif().getAllLeafSubstructures().stream()
                                  .map(leafSubstructure -> leafSubstructure.getIdentifier().toSimpleString() + "-" + leafSubstructure.getFamily().getThreeLetterCode())
                                  .collect(Collectors.joining("','", "['", "']"));

        String executionString = "viewer({ pdb : '" + matchStructureExternalPath + "', clear : true, style : 'cartoon', labelSize : 22, labelStyle : 'bold', motif : " + motifString + " })";
        RequestContext.getCurrentInstance().execute(executionString);

        currentPvLabel = matchStructure.getPdbIdentifier();

        RequestContext.getCurrentInstance().update("proteinViewerStatus");

    }

    public void showPairwise(Fit3DMatch match) {

        // ensure matches are written
        job.writeMatches();

        // find corresponding PDB file
        Path matchPath = job.getJobPath().resolve("matches").resolve(match.getSubstructureSuperimposition().getStringRepresentation() + ".pdb");
        if (matchPath.toFile().exists()) {
            RequestContext.getCurrentInstance().execute("viewer({ pdb : '" + SessionManager.relativizePath(matchPath) +
                                                        "', style : 'sticks', labels : true, labelSize : 22, labelStyle : 'bold', clear : true, additionalPdb : { pdb : '"
                                                        + SessionManager.relativizePath(motifPath) +
                                                        "', style : 'sticks', color : 'green', labelColor : 'rgb(0, 255, 0)', labelSize : 22, labels : true, labelStyle : 'bold', alternatePosition :" +
                                                        " true } })");
            currentPvLabel = match.getCandidateMotif().toString() + " against <span style=\"color:#00b04b\">query motif</span>";
            RequestContext.getCurrentInstance().update("proteinViewerStatus");
        } else {
            currentPvLabel = "no structure available";
        }
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
                                                                     .everything()
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

                try {
                    // calculate all-against-one alignment
                    showAllAgainstOne();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                // show toolbox
                showToolbox();

                // generate RMSD distribution
                initRmsdDistribution();
            }
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

    public String getPDBIdentifier(Fit3DMatch match) {
        return match.getCandidateMotif().getFirstLeafSubstructure().getPdbIdentifier();
    }

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

    public int getStructureCount() {
        return structureCount;
    }

    public StreamedContent getZippedResults() throws IOException {

        Path zipFilePath = job.getJobPath().resolve("results.zip");

        // create directory zip class
        DirectoryZip directoryZip = new DirectoryZip(zipFilePath.toString(), job.getJobPath().toString());

        // ignore files
        List<String> ignoreFiles = new ArrayList<>();

        ignoreFiles.add("all.pdb");
        ignoreFiles.add("pdb");
        directoryZip.setIgnoreFiles(true);
        directoryZip.setIgnoreFileList(ignoreFiles);

        // zip directory
        directoryZip.zipRecursively();

        return new DefaultStreamedContent(new FileInputStream(directoryZip.getZipFileName()), "application/octet-stream", "results.zip");
    }
}
