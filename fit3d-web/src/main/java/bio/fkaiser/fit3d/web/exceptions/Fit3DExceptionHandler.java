package bio.fkaiser.fit3d.web.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

public class Fit3DExceptionHandler extends ExceptionHandlerWrapper {

    private static final Logger logger = LoggerFactory.getLogger(Fit3DExceptionHandler.class);

    private ExceptionHandler wrapped;

    public Fit3DExceptionHandler(ExceptionHandler exception) {
        wrapped = exception;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {

        final Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents()
                .iterator();
        while (i.hasNext()) {

            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event
                    .getSource();

            // get the exception from context
            Throwable t = context.getException();

            final FacesContext fc = FacesContext.getCurrentInstance();
            final Map<String, Object> requestMap = fc.getExternalContext()
                                                     .getRequestMap();
            final NavigationHandler nav = fc.getApplication()
                                            .getNavigationHandler();

            // here you do what ever you want with exception
            try {

                // log error ?
                logger.error(t.getMessage(), t);

                Flash flash = fc.getExternalContext().getFlash();

                // Put the exception in the flash scope to be displayed in the
                // error
                // page if necessary ...
                // TODO prevent direct access to errorpage
                flash.put("errorDetails", t.getMessage());

                // redirect error page
                requestMap.put("exceptionMessage", t.getMessage());
                nav.handleNavigation(fc, null, "/errorpages/generic");
                fc.renderResponse();

                // remove the comment below if you want to report the error in a
                // jsf error message
                // JsfUtil.addErrorMessage(t.getMessage());

            } finally {
                // remove it from queue
                i.remove();
            }
        }

        // parent handle
        getWrapped().handle();
    }

}
