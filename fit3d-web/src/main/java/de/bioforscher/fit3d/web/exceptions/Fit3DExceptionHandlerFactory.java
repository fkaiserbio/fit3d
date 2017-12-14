package de.bioforscher.fit3d.web.exceptions;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class Fit3DExceptionHandlerFactory extends ExceptionHandlerFactory {

	private ExceptionHandlerFactory parent;

	public Fit3DExceptionHandlerFactory(ExceptionHandlerFactory parent) {

		this.parent = parent;
	}

	@Override
	public ExceptionHandler getExceptionHandler() {
		ExceptionHandler handler = new Fit3DExceptionHandler(
				this.parent.getExceptionHandler());

		return handler;
	}
}
