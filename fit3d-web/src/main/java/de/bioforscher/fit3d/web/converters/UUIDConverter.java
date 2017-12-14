package de.bioforscher.fit3d.web.converters;

import java.util.UUID;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class UUIDConverter implements Converter {

	@Override
	public UUID getAsObject(FacesContext context, UIComponent component,
			String value) {

		return UUID.fromString(value);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		// TODO Auto-generated method stub
		return null;
	}
}
