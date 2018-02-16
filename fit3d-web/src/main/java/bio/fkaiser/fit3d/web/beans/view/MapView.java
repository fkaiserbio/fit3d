package bio.fkaiser.fit3d.web.beans.view;

import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 * @author fk
 */
public class MapView {

    private MapModel model;
    private Marker marker;

    public MapView() {
        model = new DefaultMapModel();
        marker = new Marker(new LatLng(50.9882602,12.9711203), "Haus 06 - Grunert de Jacome Bau");
        model.addOverlay(marker);
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        marker = (Marker) event.getOverlay();
    }

    public Marker getMarker() {
        return marker;
    }

    public MapModel getModel() {
        return model;
    }
}
