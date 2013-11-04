package org.openmrs.module.shr.contenthandler;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.contenthandler.api.Content;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class UnstructuredDataHandlerTest extends BaseModuleContextSensitiveTest {
	
	private static final Content TEST_CONTENT = new Content("This is a test string. It is awesome.", "PlainString", "text/plain");


	/**
	 * @see UnstructuredDataHandler#cloneHandler()
	 * @verifies return an UnstructuredDataHandler instance with the same content type
	 */
	@Test
	public void cloneHandler_shouldReturnAnUnstructuredDataHandlerInstanceWithTheSameContentType()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler(TEST_CONTENT.getContentType());
		UnstructuredDataHandler clone = handler.cloneHandler();
		assertNotNull(clone);
		assertEquals(handler.contentType, clone.contentType);
	}

	/**
	 * @see UnstructuredDataHandler#saveContent(Patient,Provider,EncounterRole,EncounterType,Content)
	 * @verifies contain a complex obs containing the content
	 */
	@Test
	public void saveContent_shouldContainAComplexObsContainingTheContent()
			throws Exception {
		Encounter res = saveTestEncounter();
		Set<Obs> obs = res.getAllObs();
		
		assertNotNull(obs);
		assertEquals(obs.size(), 1);
		
		Obs theObs = obs.iterator().next();
		assertTrue(theObs.isComplex());
		assertEquals(theObs.getComplexData().getTitle(), TEST_CONTENT.getContentType());
		assertEquals(theObs.getComplexData().getData(), TEST_CONTENT);
	}

	/**
	 * @see UnstructuredDataHandler#saveContent(Patient,Provider,EncounterRole,EncounterType,Content)
	 * @verifies create a new encounter object using the current time
	 */
	@Test
	public void saveContent_shouldCreateANewEncounterObjectUsingTheCurrentTime()
			throws Exception {
		Date beforeSaveTime = new Date();
		Encounter res = saveTestEncounter();
		
		assertNotNull(res);
		
		Date encDateTime = res.getEncounterDatetime();
		assertNotNull(encDateTime);
		//There shouldn't be more than a few milliseconds diff
		long diff = Math.abs(encDateTime.getTime() - beforeSaveTime.getTime());
		assertTrue(diff <= 1000);
	}
	
	private Encounter saveTestEncounter() {
		UnstructuredDataHandler handler = new UnstructuredDataHandler(TEST_CONTENT.getContentType());
		Patient patient = Context.getPatientService().getPatient(2);
		Provider provider = Context.getProviderService().getProvider(1);
		EncounterRole role = Context.getEncounterService().getEncounterRole(1);
		EncounterType type = Context.getEncounterService().getEncounterType(1);
		return handler.saveContent(patient, provider, role, type, TEST_CONTENT);
	}
}