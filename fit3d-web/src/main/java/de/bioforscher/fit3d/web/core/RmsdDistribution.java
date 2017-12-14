package de.bioforscher.fit3d.web.core;

import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;

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

	public List<Fit3DMatch> getResults() {
		return this.results;
	}

	public Map<Object, Number> getValues() {
		return this.values;
	}

	public double getxMax() {
		return this.xMax;
	}

	public double getxMin() {
		return this.xMin;
	}

	public double getyMax() {
		return this.yMax;
	}

	public double getyMin() {
		return this.yMin;
	}

	public void setResults(List<Fit3DMatch> results) {
		this.results = results;
	}

	public void setValues(Map<Object, Number> values) {
		this.values = values;
	}

	public void setxMax(double xMax) {
		this.xMax = xMax;
	}

	public void setxMin(double xMin) {
		this.xMin = xMin;
	}

	public void setyMax(double yMax) {
		this.yMax = yMax;
	}

	public void setyMin(double yMin) {
		this.yMin = yMin;
	}

	private void init() {

		this.values = new HashMap<>();

		NumberFormat nfFourDecimals = NumberFormat.getInstance(Locale.US);
		DecimalFormat df = (DecimalFormat) nfFourDecimals;
		df.applyPattern("0.00");

		for (Fit3DMatch h : this.results) {

			double roundedRmsd = Double.valueOf(df.format(h.getRmsd()));

			if (this.values.containsKey(roundedRmsd)) {

				this.values.put(roundedRmsd,
						(Double) this.values.get(roundedRmsd) + 1.0);
			} else {

				this.values.put(roundedRmsd, 0.0);
			}
		}

		scaleLogarithmic();
	}

	private void scaleLogarithmic() {

		for (Object o : this.values.keySet()) {

			Number n = this.values.get(o);

			if ((Double) n != 0.0) {

				this.values.put(o, Math.log10((Double) n));
			}
		}
	}
}
