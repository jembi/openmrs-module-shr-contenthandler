/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.shr.contenthandler.api;

import java.util.Date;

import org.openmrs.Patient;

/**
 */
public interface ContentHandler {

	void saveContent(Patient patient, String content);
    void saveContent(Patient patient, String documentId, String content);
 
    String fetchDocument(String documentId);
    String queryEncounters(Patient patient, Date from, Date to);
 
    ContentHandler cloneHandler();
	
}