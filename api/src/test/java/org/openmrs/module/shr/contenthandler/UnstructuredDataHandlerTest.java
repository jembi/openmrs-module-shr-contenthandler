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
package org.openmrs.module.shr.contenthandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.contenthandler.api.CodedValue;
import org.openmrs.module.shr.contenthandler.api.Content;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests make use of the standard OMRS test dataset:
 * <ul>
 * <li>https://wiki.openmrs.org/display/docs/Unit+Tests</li>
 * <li>https://github.com/openmrs/openmrs-core/blob/master/api/src/test/resources/org/openmrs/include/standardTestDataset.xml</li>
 * </ul>
 */
public class UnstructuredDataHandlerTest extends BaseModuleContextSensitiveTest {
	
	private static final CodedValue TEST_CODE_PLAIN = new CodedValue("plain", "test", "test");
	
	private static final Content TEST_CONTENT_PLAIN = new Content("testId", "This is a test string. It is awesome.", TEST_CODE_PLAIN, TEST_CODE_PLAIN, "text/plain");


	@SuppressWarnings("deprecation")
	@Before
	public void before() {
		//Use our in-memory complex obs handler
		Context.getAdministrationService().setGlobalProperty(UnstructuredDataHandler.UNSTRUCTURED_DATA_HANDLER_GLOBAL_PROP, "InMemoryComplexObsHandler");
	}
		
	/**
	 * @see UnstructuredDataHandler#saveContent(Patient,Provider,EncounterRole,EncounterType,Content)
	 * @verifies contain a complex obs containing the content
	 */
	@Test
	public void saveContent_shouldContainAComplexObsContainingTheContent()
			throws Exception {
		Encounter res = saveTestEncounter(TEST_CONTENT_PLAIN);
		Set<Obs> obs = res.getAllObs();
		
		assertNotNull(obs);
		assertEquals(obs.size(), 1);
		
		Obs theObs = obs.iterator().next();
		assertTrue(theObs.isComplex());
		String expectedTitle = "testId";
		assertEquals(expectedTitle, theObs.getComplexData().getTitle());
		assertEquals(TEST_CONTENT_PLAIN, theObs.getComplexData().getData());
	}

	/**
	 * @see UnstructuredDataHandler#saveContent(Patient,Provider,EncounterRole,EncounterType,Content)
	 * @verifies create a new encounter object using the current time
	 */
	@Test
	public void saveContent_shouldCreateANewEncounterObjectUsingTheCurrentTime()
			throws Exception {
		Date beforeSaveTime = new Date();
		Encounter res = saveTestEncounter(TEST_CONTENT_PLAIN);
		
		assertNotNull(res);
		
		Date encDateTime = res.getEncounterDatetime();
		assertNotNull(encDateTime);
		//There shouldn't be more than a few milliseconds diff
		long diff = Math.abs(encDateTime.getTime() - beforeSaveTime.getTime());
		assertTrue(diff <= 1000);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(string)
	 * @verifies return a Content object for the encounter if found
	 */
	@Test
	public void fetchContent_string_shouldReturnAContentObjectForTheEncounterIfFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler();
		saveTestEncounter(TEST_CONTENT_PLAIN);
		
		Content res = handler.fetchContent(TEST_CONTENT_PLAIN.getContentId());
		assertNotNull(res);
		assertEquals(TEST_CONTENT_PLAIN, res);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(string)
	 * @verifies return null if the encounter isn't found
	 */
	@Test
	public void fetchContent_string_shouldReturnNullIfTheEncounterIsntFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler();
		
		Encounter testData = Context.getEncounterService().getEncounter(123456);
		assertNull("Test encounter should not exist", testData);
		Content content = handler.fetchContent("unknownId");
		assertNull(content);
	}

	/* Utils */
	
	private Encounter saveTestEncounter(Content content) {
		return saveTestEncounter(content, 1);
	}
	
	private Encounter saveTestEncounter(Content content, int typeId) {
		UnstructuredDataHandler handler = new UnstructuredDataHandler();
		
		Patient patient = Context.getPatientService().getPatient(2);
		Provider provider = Context.getProviderService().getProvider(1);
		EncounterRole role = Context.getEncounterService().getEncounterRole(1);
		EncounterType type = Context.getEncounterService().getEncounterType(typeId);
		
		Map<EncounterRole, Set<Provider>> providersByRole = new HashMap<EncounterRole, Set<Provider>>();
		Set<Provider> providers = new HashSet<Provider>();
		providers.add(provider);
		providersByRole.put(role, providers);
		
		Encounter res = handler.saveContent(patient, providersByRole, type, content);
		
		//Workaround for the testing obs handler, see InMemoryComplexObsHandler#saveObs
		for (Obs obs : res.getAllObs()) {
			if (obs.isComplex()) {
				InMemoryComplexObsHandler.store.put(obs.getObsId(), (Content)obs.getComplexData().getData());
			}
		}
		
		return res;
	}
	
	/**
	 * An in-memory obs handler for testing.
	 */
	public static class InMemoryComplexObsHandler extends AbstractHandler implements ComplexObsHandler {
		static Map<Integer, Content> store = new HashMap<Integer, Content>();

		@Override
		public Obs getObs(Obs obs, String view) {
			Content c = store.get(obs.getObsId());
			obs.setComplexData(new ComplexData(c.getContentType(), c));
			return obs;
		}

		//For some reason this method isn't being called during testing
		//(saveObs is however being called correctly at runtime)
		//As a work-around we manually add the content object to the store
		//after saving an encounter [during a test]
		@Override
		public Obs saveObs(Obs obs) throws APIException {
			store.put(obs.getObsId(), (Content)obs.getComplexData().getData());
			obs.setComplexData(null);
			return obs;
		}
		
	}
}