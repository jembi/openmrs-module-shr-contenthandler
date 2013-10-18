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

import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(ContentHandlerService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface ContentHandlerService extends OpenmrsService {
	
	/**
	 * Returns a content handler for a specified content type.
	 * Will return a default handler for unknown types.
	 * 
	 * @return An appropriate content handler for a specified content type
	 * @should Get an appropriate content handler for a specified content type
	 * @should Return a clone of the requested handler using the handler's cloneHandler method
	 * @should Return the default handler (UnstructuredDataHandler) for an unknown content type
	 * @should Never return null
	 */
	ContentHandler getContentHandler(String contentType);
 
	/**
	 * Register a content handler for a specified content type.
	 * <p>
	 * The content handler service follows the prototype design pattern for instantiating handlers.
	 * Therefore a content handler instance has to be provided. This instance will be cloned for callers
	 * of the {@link #getContentHandler(String)} method.
	 * <p>
	 * This method should be called by processor modules on startup.
	 * 
	 * @throws AlreadyRegisteredException if a handler is already registered for a specified content type
	 * @throws InvalidContentTypeException if an invalid content type is specified
	 * @should Register the specified handler for the specified content type
	 * @should Throw an AlreadyRegisteredException if a handler is already registered for a specified content type
	 * @should Throw an InvalidContentTypeException if an invalid content type is specified
	 * @should Throw a NullPointerException if prototype is null
	 */
	void registerContentHandler(String contentType, ContentHandler prototype) throws AlreadyRegisteredException, InvalidContentTypeException;
    
	/**
	 * Deregister the current handler assigned for the specified content type.
	 * <p>
	 * This method should be called by processor modules on shutdown.
	 * 
	 * @should Deregister the handler assigned for to specified contentType
	 * @should Do nothing if there is no handler assigned for a specified contentType
	 * @should Do nothing if there is an invalid content type specified
	 */
	void deregisterContentHandler(String contentType);
}
