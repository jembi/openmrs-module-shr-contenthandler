package org.openmrs.module.shr.contenthandler;

import java.util.Date;

import org.apache.commons.lang.NotImplementedException;
import org.openmrs.Patient;
import org.openmrs.module.shr.contenthandler.api.ContentHandler;

/**
 * A content handler for storing data as unstructured "blobs".
 */
public class UnstructuredDataHandler implements ContentHandler {

	@Override
	public void saveContent(Patient patient, String content) {
		//TODO
		throw new NotImplementedException();
	}

	@Override
	public void saveContent(Patient patient, String documentId, String content) {
		//TODO
		throw new NotImplementedException();
	}

	@Override
	public String fetchDocument(String documentId) {
		//TODO
		throw new NotImplementedException();
	}

	@Override
	public String queryEncounters(Patient patient, Date from, Date to) {
		//TODO
		throw new NotImplementedException();
	}

	@Override
	public ContentHandler cloneHandler() {
		//There's no state, so it's safe to just return this.
		return this;
	}

}
