package org.openmrs.module.shr.contenthandler.api;

public class InvalidContentTypeException extends Exception {

	private static final long serialVersionUID = 8393178939513509354L;

	public InvalidContentTypeException() { super("Invalid content type"); }
	public InvalidContentTypeException(String msg) { super(msg); }
}
