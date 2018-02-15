package bio.fkaiser.fit3d.web.beans.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author fk
 */
public class LoginView {

    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);

    private String requestedUri;
    private String username;
    private String password;

    @PostConstruct
    public void init() {
        requestedUri = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(RequestDispatcher.FORWARD_REQUEST_URI);
        if (requestedUri == null) {
            requestedUri = "home";
        }
    }

    public void submit() throws IOException {
        System.out.println(username + ":" + password);
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        try {
            request.login(username, password);
            logger.info("authenticated {}", request.getUserPrincipal());
            externalContext.redirect(requestedUri);
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bad login", null));
            logger.warn("failed login attempt {}:{}", username, password);
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
