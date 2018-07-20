package bio.fkaiser.fit3d.web.model;

import bio.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RmsdDistribution {

    private List<Fit3DMatch> results;
    private double xMin;
    private double xMax;

    private double yMin;
    private double yMax;

    private Map<Object, Number> values;

    public RmsdDistribution(List<Fit3DMatch> results) {
        this.results = results;
        init();
    }

    private void init() {
        values = new HashMap<>();
        NumberFormat nfFourDecimals = NumberFormat.getInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nfFourDecimals;
        df.applyPattern("0.00");
        for (Fit3DMatch h : results) {
            double roundedRmsd = Double.valueOf(df.format(h.getRmsd()));
            if (values.containsKey(roundedRmsd)) {
                values.put(roundedRmsd, (Double) values.get(roundedRmsd) + 1.0);
            } else {
                values.put(roundedRmsd, 0.0);
            }
        }
        scaleLogarithmic();
    }

    private void scaleLogarithmic() {
        for (Object o : values.keySet()) {
            Number n = values.get(o);
            if ((Double) n != 0.0) {
                values.put(o, Math.log10((Double) n));
            }
        }
    }

    public List<Fit3DMatch> getResults() {
        return results;
    }

    public void setResults(List<Fit3DMatch> results) {
        this.results = results;
    }

    public Map<Object, Number> getValues() {
        return values;
    }

    public void setValues(Map<Object, Number> values) {
        this.values = values;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }
}
