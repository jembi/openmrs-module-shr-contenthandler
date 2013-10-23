package org.openmrs.module.shr.contenthandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.module.shr.contenthandler.api.Content;
import org.openmrs.module.shr.contenthandler.api.ContentHandler;
import org.openmrs.obs.ComplexData;

/**
 * A content handler for storing data as unstructured "blobs".
 */
public class UnstructuredDataHandler implements ContentHandler {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	protected static final String UNSTRUCTURED_ATTACHMENT_CONCEPT = "Unstructured Attachment";
	
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
	public Encounter saveContent(Patient patient, Provider provider, EncounterRole role, EncounterType encounterType, Content content) {
		Encounter enc = createEncounter(patient, provider, role, encounterType, content);
		Context.getEncounterService().saveEncounter(enc);
		return enc;
	}
	
	/**
	 * Create a new encounter object with a complex obs for storing the specified content. 
	 * @should create a new encounter object using the current time
	 * @should contain a complex obs containing the content
	 */
	protected Encounter createEncounter(Patient patient, Provider provider, EncounterRole role, EncounterType encounterType, Content content) {
		Encounter enc = new Encounter();
		
		enc.setEncounterType(encounterType);
		Obs obs = createUnstructuredDataObs(content);
		obs.setPerson(patient);
		obs.setEncounter(enc);
		enc.addObs(obs);
		enc.setEncounterDatetime(obs.getObsDatetime());
		enc.setPatient(patient);
		enc.setProvider(role, provider);
		
		return enc;
	}
	
	private Obs createUnstructuredDataObs(Content content) {
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
	
	

	/**
	 * @see ContentHandler#fetchDocument(String)
	 */
	@Override
	public Content fetchDocument(String documentUniqueId) {
		//TODO
		throw new NotImplementedException();
	}

	/**
	 * @see ContentHandler#fetchDocument(int)
	 */
	@Override
	public Content fetchDocument(int encounterId) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @see ContentHandler#queryEncounters(Patient, Date, Date)
	 */
	@Override
	public List<Content> queryEncounters(Patient patient, Date from, Date to) {
		List<Encounter> encs = Context.getEncounterService().getEncounters(
			patient, null, from, to, null, null, null, null, null, false
			);
		if (encs==null || encs.isEmpty())
			return Collections.emptyList();
		
		List<Content> res = new ArrayList<Content>(encs.size());
		Concept unstructuredConcept = getUnstructuredAttachmentConcept();
		
		for (Encounter enc : encs) {
			for (Obs obs : enc.getAllObs()) {
				if (obs.isComplex() && obs.getConcept().getConceptId().equals(unstructuredConcept.getConceptId())) {
					Object data = obs.getComplexData().getData();
					
					if (data==null || !(data instanceof Content)) {
						log.warn("Unprocessable content found in unstructured data obs");
						continue;
					}
					
					res.add((Content)data);
				}
			}
		}
		
		return res;
	}

	@Override
	public ContentHandler cloneHandler() {
		return new UnstructuredDataHandler(contentType);
	}
}
