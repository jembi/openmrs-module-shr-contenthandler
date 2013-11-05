package org.openmrs.module.shr.contenthandler.api;

public class InvalidRepresentationException extends RuntimeException {

	private static final long serialVersionUID = 2639117949683711190L;

	public InvalidRepresentationException() { super(); }
	public InvalidRepresentationException(String msg) { super(msg); }
}
