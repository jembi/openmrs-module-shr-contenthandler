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

import java.util.Map;
import java.util.Set;

import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Provider;

/**
 * A content handler provides an implementation for storing and retrieving patient encounter data in a particular format.
 * <p>
 * Content handlers are registered with the {@link ContentHandlerService} and should not be instantiated directly.
 * To find a particular content handler, use the {@link ContentHandlerService#getContentHandler(String)} method.
 * <p>
 * A content handler can register itself for a particular content type using
 * the {@link ContentHandlerService#registerContentHandler(String, ContentHandler)} method
 * and deregister using the {@link ContentHandlerService#deregisterContentHandler(String)} method.
 * 
 * @see ContentHandlerService
 */
public interface ContentHandler {

	/**
	 * Parse and store clinical content for the specified patient.
	 * 
	 * @param patient The patient associated with the content
	 * @param providersByRole The clinical providers associated with this content mapped by their role
	 * @param encounterType The encounter type
	 * @param content The encounter data
	 * @return The created and saved encounter object
	 */
	Encounter saveContent(Patient patient, Map<EncounterRole, Set<Provider>> providersByRole, EncounterType encounterType, Content content) throws ContentHandlerException;

	/**
	 * Retrieve the content associated with the specified encounter uuid.
	 * 
	 * @param contentId The unique content identifier that was used to save the content
	 * @return The content in the content handler's format
	 */
	Content fetchContent(String contentId) throws ContentHandlerException;

	/**
	 * Create a clone of this handler.
	 * <p>
	 * Note that this method allows a content handler to return itself as an instance (i.e. {@code return this;}).
	 * This is likely to happen if the handler doesn't have any state or if it's thread safe.
	 * 
	 * @return A clone of this handler
	 */
	ContentHandler cloneHandler();
}
