package org.openmrs.module.shr.contenthandler.api;

public class AlreadyRegisteredException extends Exception {

	private static final long serialVersionUID = -1746568651507035602L;

	public AlreadyRegisteredException() {}
	public AlreadyRegisteredException(String msg) { super(msg); }
	public AlreadyRegisteredException(Throwable ex) { super(ex); }
}
