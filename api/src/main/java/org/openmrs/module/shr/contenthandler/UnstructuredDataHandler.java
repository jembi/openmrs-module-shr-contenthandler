package org.openmrs.module.shr.contenthandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.NotImplementedException;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.contenthandler.api.ContentHandler;
import org.openmrs.obs.ComplexData;

/**
 * A content handler for storing data as unstructured "blobs".
 */
public class UnstructuredDataHandler implements ContentHandler {
	
	protected static final String UNSTRUCTURED_ATTACHMENT_CONCEPT = "Unstructured Attachment";
	protected static final String UNSTRUCTURED_DATA_ENCOUNTER_TYPE = "Unstructured Data";
	protected static final String NULL_PROVIDER_ID = "Null-Provider";
	
	protected static final String ENCOUNTERROLE_UUID_GLOBAL_PROP = "shr.contenthandler.encounterrole.uuid";
	protected static final String UNSTRUCTURED_DATA_HANDLER_GLOBAL_PROP = "shr.contenthandler.unstructureddatahandler.key";
	
	private String contentType;
	
	public UnstructuredDataHandler(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @see ContentHandler#saveContent(Patient, String)
	 */
	@Override
	public void saveContent(Patient patient, String content) {
		Context.getEncounterService().saveEncounter( createEncounter(patient, content) );
	}

	/**
	 * @see ContentHandler#saveContent(Patient, String, String)
	 */
	@Override
	public void saveContent(Patient patient, String documentUniqueId, String content) {
		Encounter enc = createEncounter(patient, content);
		
		//TODO associate encounter with documentUniqueId
		//...
		
		Context.getEncounterService().saveEncounter(enc);
	}
	
	/**
	 * Create a new encounter object with a complex obs for storing the specified content. 
	 * @should create a new encounter object using the current time
	 * @should contain a complex obs containing the content
	 */
	protected Encounter createEncounter(Patient patient, String content) {
		Encounter enc = new Encounter();
		
		enc.setEncounterType(getUnstructuredDataEncounterType());
		Obs obs = createUnstructuredDataObs(content);
		obs.setPerson(patient);
		obs.setEncounter(enc);
		enc.addObs(obs);
		enc.setEncounterDatetime(obs.getObsDatetime());
		enc.setPatient(patient);
		enc.setProvider(getDefaultEncounterRole(), getNullProvider());
		
		return enc;
	}
	
	private Obs createUnstructuredDataObs(String content) {
		Obs res = new Obs();
		ComplexData cd = new ComplexData(contentType, content);
		
		res.setConcept(getUnstructuredAttachmentConcept());
		res.setComplexData(cd);
		res.setObsDatetime(new Date());
		
		return res;
	}

	private Concept getUnstructuredAttachmentConcept() {
		Concept res = Context.getConceptService().getConceptByName(UNSTRUCTURED_ATTACHMENT_CONCEPT);
		if (res==null) {
			res = createUnstructuredAttachmentConcept();
		}
		return res;
	}
	
	private Concept createUnstructuredAttachmentConcept() {
		ConceptService cs = Context.getConceptService();
		ConceptComplex c = new ConceptComplex();
		ConceptName cn = new ConceptName(UNSTRUCTURED_ATTACHMENT_CONCEPT, Locale.ENGLISH);
		ConceptDescription cd = new ConceptDescription("Represents a generic unstructured data attachment", Locale.ENGLISH);
		
		c.setFullySpecifiedName(cn);
		c.setPreferredName(cn);
		c.addDescription(cd);
		c.setDatatype(cs.getConceptDatatypeByName("Complex"));
		c.setConceptClass(cs.getConceptClassByName("Misc"));
		
		String handlerKey = Context.getAdministrationService().getGlobalProperty(UNSTRUCTURED_DATA_HANDLER_GLOBAL_PROP);
		c.setHandler(handlerKey);
		
		return c;
	}
	
	private EncounterType getUnstructuredDataEncounterType() {
		EncounterType res = Context.getEncounterService().getEncounterType(UNSTRUCTURED_DATA_ENCOUNTER_TYPE);
		if (res==null) {
			res = new EncounterType(UNSTRUCTURED_DATA_ENCOUNTER_TYPE, "Represents an encounter that consists of unstructured data.");
			Context.getEncounterService().saveEncounterType(res);
		}
		return res;
	}
	
	@SuppressWarnings("deprecation")
	private EncounterRole getDefaultEncounterRole() {
		//Delicious encounter rolls just like Suranga used to make
		//https://github.com/jembi/rhea-shr-adapter/blob/3e25fa0cd276327ca83127283213b6658af9e9ef/api/src/main/java/org/openmrs/module/rheashradapter/util/RHEA_ORU_R01Handler.java#L422
		String uuid = Context.getAdministrationService().getGlobalProperty(ENCOUNTERROLE_UUID_GLOBAL_PROP);
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(uuid);

		if(encounterRole == null) {
			encounterRole = new EncounterRole();
			encounterRole.setName("Default Unstructured Data Encounter Role");
			encounterRole.setDescription("Created by the OpenHIE SHR");
	
			encounterRole = Context.getEncounterService().saveEncounterRole(encounterRole);
			Context.getAdministrationService().setGlobalProperty(ENCOUNTERROLE_UUID_GLOBAL_PROP, encounterRole.getUuid());
		} 
		
		return encounterRole;
	}

	private Provider getNullProvider() {
		Provider p = Context.getProviderService().getProviderByIdentifier(NULL_PROVIDER_ID);
		
		if (p==null) {
			p = new Provider();
			p.setIdentifier(NULL_PROVIDER_ID);
			p.setName(NULL_PROVIDER_ID);
			Context.getProviderService().saveProvider(p);
		}
		
		return p;
	}
	

	/**
	 * @see ContentHandler#fetchDocument(String)
	 */
	@Override
	public String fetchDocument(String documentUniqueId) {
		//TODO
		throw new NotImplementedException();
	}

	/**
	 * @see ContentHandler#queryEncounters(Patient, Date, Date)
	 */
	@Override
	public List<String> queryEncounters(Patient patient, Date from, Date to) {
		List<Encounter> encs = Context.getEncounterService().getEncounters(
			patient, null, from, to, null, Collections.singleton(getUnstructuredDataEncounterType()), null, null, null, false
			);
		if (encs==null || encs.isEmpty())
			return Collections.emptyList();
		
		List<String> res = new ArrayList<String>(encs.size());
		
		for (Encounter enc : encs) {
			for (Obs obs : enc.getAllObs()) {
				if (obs.isComplex() && contentType.equals(obs.getComplexData().getTitle())) {
					res.add(encodeComplexData(obs));
				}
			}
		}
		
		return res;
	}
	
	private String encodeComplexData(Obs obs) {
		Object data = obs.getComplexData().getData();
		String payload = null;
		
		if (data instanceof String) {
			payload = (String)data;
		} else if (data instanceof byte[]) {
			payload = Base64.encodeBase64String((byte[])data);
		} else {
			payload = Base64.encodeBase64String(data.toString().getBytes());
		}
		
		return payload;
	}

	@Override
	public ContentHandler cloneHandler() {
		return new UnstructuredDataHandler(contentType);
	}
}
