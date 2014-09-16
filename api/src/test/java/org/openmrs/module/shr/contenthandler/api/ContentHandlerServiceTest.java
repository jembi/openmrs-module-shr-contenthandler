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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.contenthandler.UnstructuredDataHandler;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests {@link ${ContentHandlerService}}.
 */
public class  ContentHandlerServiceTest extends BaseModuleContextSensitiveTest {
	
	private static final CodedValue TEST_TYPE_CODE = new CodedValue("testType", "test", "test");
	private static final CodedValue TEST_FORMAT_CODE = new CodedValue("testFormat", "test", "test");
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(ContentHandlerService.class));
	}
	
	private ContentHandlerService getService() {
		ContentHandlerService chs = Context.getService(ContentHandlerService.class);
		chs.deregisterContentHandler("text/plain");
		chs.deregisterContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE);
		return chs;
	}
	
	
	/* Content type */

	/**
	 * @see ContentHandlerService#deregisterContentHandler(String)
	 * @verifies Deregister the handler assigned for to specified contentType
	 */
	@Test
	public void deregisterContentHandler_shouldDeregisterTheHandlerAssignedForToSpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		chs.registerContentHandler("text/plain", mockHandler);
		assertTrue(chs.getContentHandler("text/plain") == mockHandler);
		
		chs.deregisterContentHandler("text/plain");
		assertTrue(chs.getContentHandler("text/plain") != mockHandler);
	}

	/**
	 * @see ContentHandlerService#deregisterContentHandler(String)
	 * @verifies Do nothing if there is no handler assigned for a specified contentType
	 */
	@Test
	public void deregisterContentHandler_shouldDoNothingIfThereIsNoHandlerAssignedForASpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		chs.deregisterContentHandler("application/nothing-here");
	}

	/**
	 * @see ContentHandlerService#getContentHandler(String)
	 * @verifies Get an appropriate content handler for a specified content type
	 */
	@Test
	public void getContentHandler_shouldGetAnAppropriateContentHandlerForASpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		chs.registerContentHandler("text/plain", mockHandler);
		assertTrue(chs.getContentHandler("text/plain") == mockHandler);
	}

	/**
	 * @see ContentHandlerService#getContentHandler(String)
	 * @verifies Return the default handler (UnstructuredDataHandler) for an unknown content type
	 */
	@Test
	public void getContentHandler_shouldReturnNullForAnUnknownContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		assertNull(chs.getContentHandler("application/nothing-here"));
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
	 * @verifies Register the specified handler for the specified content type
	 */
	@Test
	public void registerContentHandler_shouldRegisterTheSpecifiedHandlerForTheSpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		assertTrue(chs.getContentHandler("text/plain") != mockHandler);
		chs.registerContentHandler("text/plain", mockHandler);
		assertTrue(chs.getContentHandler("text/plain") == mockHandler);
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
	 * @verifies Throw an AlreadyRegisteredException if a handler is already registered for a specified content type
	 */
	@Test
	public void registerContentHandler_shouldThrowAnAlreadyRegisteredExceptionIfAHandlerIsAlreadyRegisteredForASpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		ContentHandler mockHandler2 = mock(ContentHandler.class);
		
		chs.registerContentHandler("text/plain", mockHandler);
		try {
			chs.registerContentHandler("text/plain", mockHandler2);
			fail();
		} catch (AlreadyRegisteredException ex) {
			//expected
		}
	}

	/**
	 * @see ContentHandlerService#deregisterContentHandler(String)
	 * @verifies Do nothing if there is an invalid content type specified
	 */
	@Test
	public void deregisterContentHandler_shouldDoNothingIfThereIsAnInvalidContentTypeSpecified()
			throws Exception {
		ContentHandlerService chs = getService();
		
		chs.deregisterContentHandler(null);
		chs.deregisterContentHandler("");
		chs.deregisterContentHandler("this isn't valid");
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
	 * @verifies Throw an InvalidContentTypeException if an invalid content type is specified
	 */
	@Test
	public void registerContentHandler_shouldThrowAnInvalidContentTypeExceptionIfAnInvalidContentTypeIsSpecified()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		try {
			chs.registerContentHandler(null, mockHandler);
			fail();
		} catch (InvalidContentTypeException ex) {
			//expected
		}
		try {
			chs.registerContentHandler("", mockHandler);
			fail();
		} catch (InvalidContentTypeException ex) {
			//expected
		}
		try {
			chs.registerContentHandler("This isn't valid", mockHandler);
			fail();
		} catch (InvalidContentTypeException ex) {
			//expected
		}
	}

	/**
	 * @see ContentHandlerService#getContentHandler(String)
	 * @verifies Return a clone of the requested handler using the handler's cloneHandler method
	 */
	@Test
	public void getContentHandler_shouldReturnACloneOfTheRequestedHandlerUsingTheHandlersCloneHandlerMethod()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		chs.registerContentHandler("text/plain", mockHandler);
		chs.getContentHandler("text/plain");
		verify(mockHandler).cloneHandler();
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
	 * @verifies Throw a NullPointerException if prototype is null
	 */
	@Test
	public void registerContentHandler_shouldThrowANullPointerExceptionIfPrototypeIsNull()
			throws Exception {
		ContentHandlerService chs = getService();
		
		try {
			chs.registerContentHandler("text/plain", null);
			fail();
		} catch (NullPointerException ex) {
			//expected
		}
	}
	
	
	/* Type and format code */
	
	/**
	 * @see ContentHandlerService#deregisterContentHandler(CodedValue, CodedValue)
	 * @verifies Deregister the handler assigned to specified type and format code
	 */
	@Test
	public void deregisterContentHandler_tfCode_shouldDeregisterTheHandlerAssignedForToSpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		chs.registerContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE, mockHandler);
		assertTrue(chs.getContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE) == mockHandler);
		
		chs.deregisterContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE);
		assertTrue(chs.getContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE) != mockHandler);
	}

	/**
	 * @see ContentHandlerService#deregisterContentHandler(CodedValue, CodedValue)
	 * @verifies Do nothing if there is no handler assigned for a specified type and format code
	 */
	@Test
	public void deregisterContentHandler_tfCode_shouldDoNothingIfThereIsNoHandlerAssignedForASpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		chs.deregisterContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE);
	}

	/**
	 * @see ContentHandlerService#getContentHandler(CodedValue, CodedValue)
	 * @verifies Get an appropriate content handler for a specified type and format code
	 */
	@Test
	public void getContentHandler_tfCode_shouldGetAnAppropriateContentHandlerForASpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		chs.registerContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE, mockHandler);
		assertTrue(chs.getContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE) == mockHandler);
	}

	/**
	 * @see ContentHandlerService#getContentHandler(CodedValue, CodedValue)
	 * @verifies Return the default handler (UnstructuredDataHandler) for an unknown type and format code
	 */
	@Test
	public void getContentHandler_tfCode_shouldReturnNullForAnUnknownContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		assertNull(chs.getContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE));
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(CodedValue, CodedValue, ContentHandler)
	 * @verifies Register the specified handler for the specified type and format code
	 */
	@Test
	public void registerContentHandler_tfCode_shouldRegisterTheSpecifiedHandlerForTheSpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		assertTrue(chs.getContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE) != mockHandler);
		chs.registerContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE, mockHandler);
		assertTrue(chs.getContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE) == mockHandler);
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(CodedValue, CodedValue, ContentHandler)
	 * @verifies Throw an AlreadyRegisteredException if a handler is already registered for a specified type and format code
	 */
	@Test
	public void registerContentHandler_tfCode_shouldThrowAnAlreadyRegisteredExceptionIfAHandlerIsAlreadyRegisteredForASpecifiedContentType()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		ContentHandler mockHandler2 = mock(ContentHandler.class);
		
		chs.registerContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE, mockHandler);
		try {
			chs.registerContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE, mockHandler2);
			fail();
		} catch (AlreadyRegisteredException ex) {
			//expected
		}
	}

	/**
	 * @see ContentHandlerService#deregisterContentHandler(String)
	 * @verifies Do nothing if there is an invalid content type specified
	 */
	@Test
	public void deregisterContentHandler_tfCode_shouldDoNothingIfThereIsAnInvalidContentTypeSpecified()
			throws Exception {
		ContentHandlerService chs = getService();
		
		chs.deregisterContentHandler(null);
		chs.deregisterContentHandler("");
		chs.deregisterContentHandler("this isn't valid");
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(CodedValue, CodedValue, ContentHandler)
	 * @verifies Throw an InvalidCodedTypeException if an invalid type and format code is specified
	 */
	@Test
	public void registerContentHandler_tfCode_shouldThrowAnInvalidContentTypeExceptionIfAnInvalidContentTypeIsSpecified()
			throws Exception {
		ContentHandlerService chs = getService();
		CodedValue blank = new CodedValue("", "");
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		try {
			chs.registerContentHandler(null, null, mockHandler);
			fail();
		} catch (InvalidCodedValueException ex) {
			//expected
		}
		try {
			chs.registerContentHandler(blank, blank, mockHandler);
			fail();
		} catch (InvalidCodedValueException ex) {
			//expected
		}
		try {
			chs.registerContentHandler(blank, null, mockHandler);
			fail();
		} catch (InvalidCodedValueException ex) {
			//expected
		}
		try {
			chs.registerContentHandler(null, blank, mockHandler);
			fail();
		} catch (InvalidCodedValueException ex) {
			//expected
		}
	}

	/**
	 * @see ContentHandlerService#getContentHandler(CodedValue, CodedValue)
	 * @verifies Return a clone of the requested handler using the handler's cloneHandler method
	 */
	@Test
	public void getContentHandler_tfCode_shouldReturnACloneOfTheRequestedHandlerUsingTheHandlersCloneHandlerMethod()
			throws Exception {
		ContentHandlerService chs = getService();
		
		ContentHandler mockHandler = mock(ContentHandler.class);
		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
		
		chs.registerContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE, mockHandler);
		chs.getContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE);
		verify(mockHandler).cloneHandler();
	}

	/**
	 * @see ContentHandlerService#registerContentHandler(CodedValue, CodedValue, ContentHandler)
	 * @verifies Throw a NullPointerException if prototype is null
	 */
	@Test
	public void registerContentHandler_tfCode_shouldThrowANullPointerExceptionIfPrototypeIsNull()
			throws Exception {
		ContentHandlerService chs = getService();
		
		try {
			chs.registerContentHandler(TEST_TYPE_CODE, TEST_FORMAT_CODE, null);
			fail();
		} catch (NullPointerException ex) {
			//expected
		}
	}

	/**
	 * @see ContentHandlerService#getDefaultUnstructuredHandler()
	 * @verifies Return the default handler (UnstructuredDataHandler)
	 */
	@Test
	public void getDefaultHandler_contentType_shouldReturnTheDefaultHandlerUnstructuredDataHandler()
			throws Exception {
		ContentHandlerService chs = getService();
		assertTrue(chs.getDefaultUnstructuredHandler() instanceof UnstructuredDataHandler);
	}

	/**
	 * @see ContentHandlerService#getContentHandlerByClass(Class)
	 * @verifies return a content handler for the given class
	 */
	@Test
	public void getContentHandlerByClass_shouldReturnAContentHandlerForTheGivenClass()
			throws Exception {
		ContentHandlerService chs = getService();
		chs.registerContentHandler("text/xml", new KnownContentHandler());
		
		ContentHandler ch = chs.getContentHandlerByClass(KnownContentHandler.class);
		assertThat(ch, instanceOf(KnownContentHandler.class));
	}

	/**
	 * @see ContentHandlerService#getContentHandlerByClass(Class)
	 * @verifies return null if no content handler was found
	 */
	@Test
	public void getContentHandlerByClass_shouldReturnNullIfNoContentHandlerWasFound()
			throws Exception {
		ContentHandlerService chs = getService();

		ContentHandler ch = chs.getContentHandlerByClass(UnkownContentHandler.class);
		assertNull(ch);
	}
	
	private class UnkownContentHandler implements ContentHandler {
		@Override
		public Encounter saveContent(Patient patient,
				Map<EncounterRole, Set<Provider>> providersByRole,
				EncounterType encounterType, Content content) {
			return null;
		}

		@Override
		public Content fetchContent(String contentId) {
			return null;
		}

		@Override
		public ContentHandler cloneHandler() {
			return new UnkownContentHandler();
		}
	}
	
	private class KnownContentHandler implements ContentHandler {
		@Override
		public Encounter saveContent(Patient patient,
				Map<EncounterRole, Set<Provider>> providersByRole,
				EncounterType encounterType, Content content) {
			return null;
		}

		@Override
		public Content fetchContent(String contentId) {
			return null;
		}

		@Override
		public ContentHandler cloneHandler() {
			return new KnownContentHandler();
		}
	}
}
