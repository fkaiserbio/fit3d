package bio.fkaiser.fit3d.web.validators;

import bio.fkaiser.fit3d.web.Fit3DWebConstants;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.List;

public class FileValidator implements Validator {
    @Override
    public void validate(FacesContext content, UIComponent component, Object value) throws ValidatorException {
        List<FacesMessage> messages = new ArrayList<>();
        Part file = (Part) value;
        if (file.getSize() > Fit3DWebConstants.MAXIMAL_UPLOAD_SIZE) {
            messages.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Maximal size exceeded.", "The file you are trying to upload exceeds the maximal allowed size."));
        }
        if (!"text/plain".equals(file.getContentType())) {
            messages.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not a text file.", "The file you are trying to upload is not a plain text file."));
        }
        if (!messages.isEmpty()) {
            throw new ValidatorException(messages);
        }
    }
}
