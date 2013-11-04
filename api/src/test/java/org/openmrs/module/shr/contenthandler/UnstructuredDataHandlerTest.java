package org.openmrs.module.shr.contenthandler;


import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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

/**
 * Tests make use of the standard OMRS test dataset:
 * <ul>
 * <li>https://wiki.openmrs.org/display/docs/Unit+Tests</li>
 * <li>https://github.com/openmrs/openmrs-core/blob/master/api/src/test/resources/org/openmrs/include/standardTestDataset.xml</li>
 * </ul>
 */
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
		assertEquals(TEST_CONTENT.getContentType(), theObs.getComplexData().getTitle());
		assertEquals(TEST_CONTENT, theObs.getComplexData().getData());
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

	/**
	 * @see UnstructuredDataHandler#fetchContent(int)
	 * @verifies return a Content object for the encounter if found
	 */
	@Test
	public void fetchContent_int_shouldReturnAContentObjectForTheEncounterIfFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler(TEST_CONTENT.getContentType());
		Encounter enc = saveTestEncounter();
		
		Content res = handler.fetchContent(enc.getId());
		assertNotNull(res);
		assertEquals(TEST_CONTENT, res);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(int)
	 * @verifies return null if the encounter doesn't contain an unstructured data obs
	 */
	@Test
	public void fetchContent_int_shouldReturnNullIfTheEncounterDoesntContainAnUnstructuredDataObs()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		
		Encounter testData = Context.getEncounterService().getEncounter(3);
		assertNotNull("Test data should be setup correctly", testData);
		Content content = handler.fetchContent(3);
		assertNull(content);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(int)
	 * @verifies return null if the encounter isn't found
	 */
	@Test
	public void fetchContent_int_shouldReturnNullIfTheEncounterIsntFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		
		Encounter testData = Context.getEncounterService().getEncounter(123456);
		assertNull("Test encounter should not exist", testData);
		Content content = handler.fetchContent(123456);
		assertNull(content);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(String)
	 * @verifies return a Content object for the encounter if found
	 */
	@Test
	public void fetchContent_string_shouldReturnAContentObjectForTheEncounterIfFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler(TEST_CONTENT.getContentType());
		Encounter enc = saveTestEncounter();
		
		Content res = handler.fetchContent(enc.getUuid());
		assertNotNull(res);
		assertEquals(TEST_CONTENT, res);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(String)
	 * @verifies return null if the encounter doesn't contain an unstructured data obs
	 */
	@Test
	public void fetchContent_string_shouldReturnNullIfTheEncounterDoesntContainAnUnstructuredDataObs()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		
		Encounter testData = Context.getEncounterService().getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac");
		assertNotNull("Test data should be setup correctly", testData);
		Content content = handler.fetchContent("6519d653-393b-4118-9c83-a3715b82d4ac");
		assertNull(content);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(String)
	 * @verifies return null if the encounter isn't found
	 */
	@Test
	public void fetchContent_string_shouldReturnNullIfTheEncounterIsntFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		
		Encounter testData = Context.getEncounterService().getEncounterByUuid("12345678-aaaa-bbbb-cccc-dddddddddddd");
		assertNull("Test encounter should not exist", testData);
		Content content = handler.fetchContent("12345678-aaaa-bbbb-cccc-dddddddddddd");
		assertNull(content);
	}

	/**
	 * @see UnstructuredDataHandler#queryEncounters(Patient,Date,Date)
	 * @verifies return a list of Content objects for all matching encounters
	 */
	@Test
	public void queryEncounters_shouldReturnAListOfContentObjectsForAllMatchingEncounters()
			throws Exception {
		//TODO auto-generated
		fail("Not yet implemented");
	}

	/**
	 * @see UnstructuredDataHandler#queryEncounters(Patient,Date,Date)
	 * @verifies return an empty list if no encounters with unstructured data obs are found
	 */
	@Test
	public void queryEncounters_shouldReturnAnEmptyListIfNoEncountersWithUnstructuredDataObsAreFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		
		Patient testPatient = Context.getPatientService().getPatient(7);
		Calendar from = new GregorianCalendar(2008, Calendar.AUGUST, 1);
		Calendar to = new GregorianCalendar(2008, Calendar.AUGUST, 19);
		
		List<Encounter> encs = Context.getEncounterService().getEncounters(
			testPatient, null, from.getTime(), to.getTime(), null, null, null, null, null, false
		);
		assertEquals("Test data should be setup correctly", 3, encs.size());
		
		List<Content> res = handler.queryEncounters(testPatient, from.getTime(), to.getTime());
		assertNotNull(res);
		assertTrue(res.isEmpty());
	}

	/**
	 * @see UnstructuredDataHandler#queryEncounters(Patient,List,Date,Date)
	 * @verifies return a list of Content objects for all matching encounters
	 */
	@Test
	public void queryEncounters_list_shouldReturnAListOfContentObjectsForAllMatchingEncounters()
			throws Exception {
		//TODO auto-generated
		fail("Not yet implemented");
	}

	/**
	 * @see UnstructuredDataHandler#queryEncounters(Patient,List,Date,Date)
	 * @verifies return an empty list if no encounters with unstructured data obs are found
	 */
	@Test
	public void queryEncounters_list_shouldReturnAnEmptyListIfNoEncountersWithUnstructuredDataObsAreFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler("text/plain");
		List<EncounterType> types = Collections.singletonList(Context.getEncounterService().getEncounterType(1));
		
		Patient testPatient = Context.getPatientService().getPatient(7);
		Calendar from = new GregorianCalendar(2008, Calendar.AUGUST, 1);
		Calendar to = new GregorianCalendar(2008, Calendar.AUGUST, 19);
		
		List<Encounter> encs = Context.getEncounterService().getEncounters(
			testPatient, null, from.getTime(), to.getTime(), null, types, null, null, null, false
		);
		assertEquals("Test data should be setup correctly", 2, encs.size());
		
		List<Content> res = handler.queryEncounters(testPatient, types, from.getTime(), to.getTime());
		assertNotNull(res);
		assertTrue(res.isEmpty());
	}
}