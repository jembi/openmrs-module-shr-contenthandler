package org.openmrs.module.shr.contenthandler;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class UnstructuredDataHandlerTest extends BaseModuleContextSensitiveTest {
	
	private static final String TEST_CONTENT = "this is a test string. it is awesome.";


	/**
	 * @see UnstructuredDataHandler#createEncounter(Patient,String)
	 * @verifies contain a complex obs containing the content
	 */
	@Test
	public void createEncounter_shouldContainAComplexObsContainingTheContent()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		Patient p = mock(Patient.class);
		Encounter res = handler.createEncounter(p, TEST_CONTENT);
		Set<Obs> obs = res.getAllObs();
		
		assertNotNull(obs);
		assertEquals(obs.size(), 1);
		
		Obs theObs = obs.iterator().next();
		assertTrue(theObs.isComplex());
		assertEquals(theObs.getComplexData().getTitle(), "text/plain");
		assertEquals(theObs.getComplexData().getData(), TEST_CONTENT);
	}

	/**
	 * @see UnstructuredDataHandler#createEncounter(Patient,String)
	 * @verifies create a new encounter object using the current time
	 */
	@Test
	public void createEncounter_shouldCreateANewEncounterObjectUsingTheCurrentTime()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		Patient p = mock(Patient.class);
		Date beforeSaveTime = new Date();
		Encounter res = handler.createEncounter(p, TEST_CONTENT);
		
		assertNotNull(res);
		
		Date encDateTime = res.getEncounterDatetime();
		assertNotNull(encDateTime);
		//There shouldn't be more than a few milliseconds diff
		long diff = Math.abs(encDateTime.getTime() - beforeSaveTime.getTime());
		assertTrue(diff <= 1000);
	}
}