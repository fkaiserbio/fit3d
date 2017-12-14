package de.bioforscher.fit3d.web.validators;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;

public class FileValidator implements Validator {

	@Override
	public void validate(FacesContext content, UIComponent component,
			Object value) throws ValidatorException {

		List<FacesMessage> msgs = new ArrayList<>();
		Part file = (Part) value;
		if (file.getSize() > 1000000) {
			msgs.add(new FacesMessage("file too big"));
		}
		if (!"text/plain".equals(file.getContentType())) {
			msgs.add(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Not a text file.",
					"The file you are trying to upload is not a plain text file."));
		}
		if (!msgs.isEmpty()) {
			throw new ValidatorException(msgs);
		}
	}
}
