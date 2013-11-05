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
	
	private static final Content TEST_CONTENT_PLAIN = new Content("This is a test string. It is awesome.", "PlainString", "text/plain");
	private static final Content TEST_CONTENT_PLAIN2 = new Content("This is a test string. It is awesome.", "PlainString2", "text/plain");
	private static final Content TEST_CONTENT_XML = new Content("<test>This is a test string. It is awesome.</test>", "XMLString", "text/xml");


	/**
	 * @see UnstructuredDataHandler#cloneHandler()
	 * @verifies return an UnstructuredDataHandler instance with the same content type
	 */
	@Test
	public void cloneHandler_shouldReturnAnUnstructuredDataHandlerInstanceWithTheSameContentType()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler(TEST_CONTENT_PLAIN.getContentType());
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
		Encounter res = saveTestEncounter(TEST_CONTENT_PLAIN);
		Set<Obs> obs = res.getAllObs();
		
		assertNotNull(obs);
		assertEquals(obs.size(), 1);
		
		Obs theObs = obs.iterator().next();
		assertTrue(theObs.isComplex());
		assertEquals(TEST_CONTENT_PLAIN.getContentType(), theObs.getComplexData().getTitle());
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
	
	private Encounter saveTestEncounter(Content content) {
		return saveTestEncounter(content, 1);
	}
	
	private Encounter saveTestEncounter(Content content, int typeId) {
		UnstructuredDataHandler handler = new UnstructuredDataHandler(content.getContentType());
		
		Patient patient = Context.getPatientService().getPatient(2);
		Provider provider = Context.getProviderService().getProvider(1);
		EncounterRole role = Context.getEncounterService().getEncounterRole(1);
		EncounterType type = Context.getEncounterService().getEncounterType(typeId);
		
		return handler.saveContent(patient, provider, role, type, content);
	}

	/**
	 * @see UnstructuredDataHandler#fetchContent(int)
	 * @verifies return a Content object for the encounter if found
	 */
	@Test
	public void fetchContent_int_shouldReturnAContentObjectForTheEncounterIfFound()
			throws Exception {
		UnstructuredDataHandler handler = new UnstructuredDataHandler(TEST_CONTENT_PLAIN.getContentType());
		Encounter enc = saveTestEncounter(TEST_CONTENT_PLAIN);
		
		Content res = handler.fetchContent(enc.getId());
		assertNotNull(res);
		assertEquals(TEST_CONTENT_PLAIN, res);
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
		UnstructuredDataHandler handler = new UnstructuredDataHandler(TEST_CONTENT_PLAIN.getContentType());
		Encounter enc = saveTestEncounter(TEST_CONTENT_PLAIN);
		
		Content res = handler.fetchContent(enc.getUuid());
		assertNotNull(res);
		assertEquals(TEST_CONTENT_PLAIN, res);
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
		for (int i=0; i<3; i++)
			saveTestEncounter(TEST_CONTENT_PLAIN);
		Encounter oldEnc = saveTestEncounter(TEST_CONTENT_PLAIN2);
		
		Calendar oldDate = new GregorianCalendar();
		oldDate.add(Calendar.DAY_OF_MONTH, -2);
		
		oldEnc.setEncounterDatetime(oldDate.getTime());
		Context.getEncounterService().saveEncounter(oldEnc);
		
		//'oldEnc' should not be in the result set
		getAndCheckContent(3, TEST_CONTENT_PLAIN.getContentType(), TEST_CONTENT_PLAIN.getFormatCode());
	}

	/**
	 * @see UnstructuredDataHandler#queryEncounters(Patient,Date,Date)
	 * @verifies only return Content objects that match the handler's content type
	 */
	@Test
	public void queryEncounters_shouldOnlyReturnContentObjectsThatMatchTheHandlersContentType()
			throws Exception {
		for (int i=0; i<3; i++)
			saveTestEncounter(TEST_CONTENT_PLAIN);
		saveTestEncounter(TEST_CONTENT_XML);
		
		//XML content should not be in the result set
		getAndCheckContent(3, TEST_CONTENT_PLAIN.getContentType(), TEST_CONTENT_PLAIN.getFormatCode());
		//Plain text content should not be in the result set
		getAndCheckContent(1, TEST_CONTENT_XML.getContentType(), TEST_CONTENT_XML.getFormatCode());
	}
	
	private void getAndCheckContent(int expectedNumEncounters, String handlerContentType, String expectedFormatCode) {
		Calendar from = new GregorianCalendar();
		Calendar to = new GregorianCalendar();
		from.add(Calendar.DAY_OF_MONTH, -1);
		to.add(Calendar.DAY_OF_MONTH, 1);
		
		UnstructuredDataHandler handler = new UnstructuredDataHandler(handlerContentType);
		Patient patient = Context.getPatientService().getPatient(2);
		List<Content> res = handler.queryEncounters(patient, from.getTime(), to.getTime());
		
		assertEquals(expectedNumEncounters, res.size());
		for (Content c : res) {
			assertEquals(expectedFormatCode, c.getFormatCode());
			assertEquals(handlerContentType, c.getContentType());
		}
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
		int encType1 = 1;
		int encType2 = 2;
		
		for (int i=0; i<3; i++)
			saveTestEncounter(TEST_CONTENT_PLAIN, encType1);
		Encounter oldEnc = saveTestEncounter(TEST_CONTENT_PLAIN2, encType1);
		saveTestEncounter(TEST_CONTENT_PLAIN2, encType2);
		
		Calendar oldDate = new GregorianCalendar();
		oldDate.add(Calendar.DAY_OF_MONTH, -2);
		
		oldEnc.setEncounterDatetime(oldDate.getTime());
		Context.getEncounterService().saveEncounter(oldEnc);
		
		List<EncounterType> encTypes1 = Collections.singletonList(Context.getEncounterService().getEncounterType(encType1));
		List<EncounterType> encTypes2 = Collections.singletonList(Context.getEncounterService().getEncounterType(encType2));
		
		//'oldEnc' and encType2 data should not be in the result set
		getAndCheckContent(encTypes1, 3, TEST_CONTENT_PLAIN.getContentType(), TEST_CONTENT_PLAIN.getFormatCode());
		//Only one encType2 data item should be in the result set
		getAndCheckContent(encTypes2, 1, TEST_CONTENT_PLAIN2.getContentType(), TEST_CONTENT_PLAIN2.getFormatCode());
	}

	/**
	 * @see UnstructuredDataHandler#queryEncounters(Patient,List,Date,Date)
	 * @verifies only return Content objects that match the handler's content type
	 */
	@Test
	public void queryEncounters_list_shouldOnlyReturnContentObjectsThatMatchTheHandlersContentType()
			throws Exception {
		int encType1 = 1;
		int encType2 = 2;
		
		for (int i=0; i<3; i++)
			saveTestEncounter(TEST_CONTENT_PLAIN, encType1);
		saveTestEncounter(TEST_CONTENT_XML, encType1);
		saveTestEncounter(TEST_CONTENT_PLAIN2, encType2);
		
		List<EncounterType> encTypes1 = Collections.singletonList(Context.getEncounterService().getEncounterType(encType1));
		List<EncounterType> encTypes2 = Collections.singletonList(Context.getEncounterService().getEncounterType(encType2));
		
		//XML and encType2 data should not be in the result set
		getAndCheckContent(encTypes1, 3, TEST_CONTENT_PLAIN.getContentType(), TEST_CONTENT_PLAIN.getFormatCode());
		//only XML data should be in the result set
		getAndCheckContent(encTypes1, 1, TEST_CONTENT_XML.getContentType(), TEST_CONTENT_XML.getFormatCode());
		//only encType2 data should be in the result set
		getAndCheckContent(encTypes2, 1, TEST_CONTENT_PLAIN2.getContentType(), TEST_CONTENT_PLAIN2.getFormatCode());
	}
	
	private void getAndCheckContent(List<EncounterType> types, int expectedNumEncounters, String handlerContentType, String expectedFormatCode) {
		Calendar from = new GregorianCalendar();
		Calendar to = new GregorianCalendar();
		
		from.add(Calendar.DAY_OF_MONTH, -1);
		to.add(Calendar.DAY_OF_MONTH, 1);
		
		UnstructuredDataHandler handler = new UnstructuredDataHandler(handlerContentType);
		Patient patient = Context.getPatientService().getPatient(2);
		List<Content> res = handler.queryEncounters(patient, types, from.getTime(), to.getTime());
		
		assertEquals(expectedNumEncounters, res.size());
		for (Content c : res) {
			assertEquals(expectedFormatCode, c.getFormatCode());
			assertEquals(handlerContentType, c.getContentType());
		}
		
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