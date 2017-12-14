package de.bioforscher.fit3d.web.utilities;

import java.io.Serializable;

import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

public class MapBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2847858027104131482L;
	private MapModel model = new DefaultMapModel();
	private Marker marker;

	public MapBean() {

		Marker m = new Marker(new LatLng(50.989325, 12.970540), "bigM", null,
				"resources/static/images/marker.png");
		this.model.addOverlay(m);
	}

	public Marker getMarker() {
		return this.marker;
	}

	public MapModel getModel() {
		return this.model;
	}

	public void onMarkerSelect(OverlaySelectEvent event) {
		this.marker = (Marker) event.getOverlay();
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
}
