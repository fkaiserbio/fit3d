package bio.fkaiser.fit3d.web.converter;

import de.bioforscher.singa.structure.model.families.AminoAcidFamily;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * @author fk
 */
public class AminoAcidFamilyConverter implements Converter {

    @Override public AminoAcidFamily getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        return AminoAcidFamily.valueOf(value);
    }

    @Override public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        return value.toString();
    }
}
