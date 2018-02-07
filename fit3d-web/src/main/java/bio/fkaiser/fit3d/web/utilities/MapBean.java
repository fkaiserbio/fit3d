package bio.fkaiser.fit3d.web.utilities;

import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import java.io.Serializable;

public class MapBean implements Serializable {

    private static final long serialVersionUID = 2944836997545953281L;
    private MapModel model = new DefaultMapModel();
    private Marker marker;

    public MapBean() {
        Marker m = new Marker(new LatLng(50.989325, 12.970540), "bigM", null, "resources/static/images/marker.png");
        model.addOverlay(m);
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        marker = (Marker) event.getOverlay();
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public MapModel getModel() {
        return model;
    }
}
