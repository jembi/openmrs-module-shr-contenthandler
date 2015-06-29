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

import java.util.*;

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
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.contenthandler.api.CodedValue;
import org.openmrs.module.shr.contenthandler.api.Content;
import org.openmrs.module.shr.contenthandler.api.ContentHandler;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.OpenmrsConstants;

/**
 * A content handler for storing data as unstructured <i>blobs</i>.
 */
public class UnstructuredDataHandler implements ContentHandler {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	protected static final String UNSTRUCTURED_ATTACHMENT_CONCEPT_BASE_NAME = "Unstructured Attachment";
	protected static final String UNSTRUCTURED_DATA_HANDLER_GLOBAL_PROP = "shr.contenthandler.unstructureddatahandler.key";
	
	/**
	 * @see ContentHandler#saveContent(String, Patient, Provider, EncounterRole, EncounterType, Content)
	 * @should create a new encounter object using the current time
	 * @should contain a complex obs containing the content
	 */
	@Override
	public Encounter saveContent(Patient patient, Map<EncounterRole, Set<Provider>> providersByRole, EncounterType encounterType, Content content) {
		Encounter enc = createEncounter(patient, providersByRole, encounterType, content);
		Context.getEncounterService().saveEncounter(enc);
		return enc;
	}
	
	/**
	 * Create a new encounter object with a complex obs for storing the specified content. 
	 */
	private Encounter createEncounter(Patient patient, Map<EncounterRole, Set<Provider>> providersByRole, EncounterType encounterType, Content content) {
		Encounter enc = new Encounter();
		
		enc.setEncounterType(encounterType);
		Obs obs = createUnstructuredDataObs(content);
		obs.setPerson(patient);
		obs.setEncounter(enc);
		enc.addObs(obs);
		enc.setEncounterDatetime(obs.getObsDatetime());
		enc.setPatient(patient);
		
		// Add all providers to encounter
		for (EncounterRole role : providersByRole.keySet()) {
			Set<Provider> providers = providersByRole.get(role);
			for (Provider provider : providers) {
				enc.addProvider(role, provider);
			}
		}
		
		return enc;
	}
	
	private Obs createUnstructuredDataObs(Content content) {
		Obs res = new Obs();
		ComplexData cd = new ComplexData(content.getContentId(), content);
		
		res.setConcept(getUnstructuredAttachmentConcept(content.getFormatCode()));
		res.setComplexData(cd);
		res.setObsDatetime(new Date());
		res.setAccessionNumber(content.getContentId());
		
		return res;
	}

    private static final Map<String, Integer> conceptCache = Collections.synchronizedMap(new HashMap<String, Integer>());
    private static final String GP_CACHE_CONCEPTS_BY_NAME = "shr.contenthandler.cacheConceptsByName";
    private static Boolean cacheConceptsByName = null;

	private Concept getUnstructuredAttachmentConcept(CodedValue formatCode) {
        synchronized (this) {
            if (cacheConceptsByName == null) {
                if (Context.getAdministrationService().getGlobalProperty(GP_CACHE_CONCEPTS_BY_NAME).equalsIgnoreCase("true")) {
                    cacheConceptsByName = true;
                } else {
                    cacheConceptsByName = false;
                }
            }
        }

        ConceptService cs = Context.getConceptService();
        String conceptName = getUnstructuredAttachmentConceptName(formatCode);

        if (cacheConceptsByName) {
            Integer conceptId = conceptCache.get(conceptName);

            if (conceptId != null) {
                return cs.getConcept(conceptId);
            }
        }

		Concept res = cs.getConceptByName(conceptName);
		if (res == null) {
			res = buildUnstructuredAttachmentConcept(conceptName);
			res = cs.saveConcept(res);
		}

        if (cacheConceptsByName) {
            conceptCache.put(conceptName, res.getConceptId());
        }

		return res;
	}
	
	private static String getUnstructuredAttachmentConceptName(CodedValue formatCode) {
		return String.format("%s (%s-%s)", UNSTRUCTURED_ATTACHMENT_CONCEPT_BASE_NAME, formatCode.getCodingScheme(), formatCode.getCode());
	}
	
	private Concept buildUnstructuredAttachmentConcept(String name) {
		ConceptService cs = Context.getConceptService();
		ConceptComplex c = new ConceptComplex();
		ConceptName cn = new ConceptName(name, Locale.ENGLISH);
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
	 * @see ContentHandler#fetchContent(String)
	 * @should return a Content object for the encounter if found
	 * @should return null if the encounter doesn't contain an unstructured data obs
	 * @should return null if the encounter isn't found
	 */
	@Override
	public Content fetchContent(String contentId) {
		ObsService os = Context.getObsService();
		// TODO update this to search by accession number, this can be done from OpenMRS 1.12
		List<Obs> obsList = os.getObservations(null, null, null, null, null, null, null, null, null, null, null, false);
		
		for (Obs obs : obsList) {
			if (obs.getAccessionNumber() != null && obs.getAccessionNumber().equals(contentId) && obs.isComplex() && isConceptAnUnstructuredDataType(obs.getConcept())) {
				Obs complexObs = os.getComplexObs(obs.getObsId(), OpenmrsConstants.TEXT_VIEW);
				Object data = complexObs.getComplexData()!=null ? complexObs.getComplexData().getData() : null;
				
				if (data==null || !(data instanceof Content)) {
					log.warn("Unprocessable content found in unstructured data obs (obsId = " + obs.getId() + ")");
					continue;
				} else {
					return (Content) data;
				}
			}
		}
		
		return null;
	}

	private boolean isConceptAnUnstructuredDataType(Concept c) {
		return c.getName().getName().startsWith(UNSTRUCTURED_ATTACHMENT_CONCEPT_BASE_NAME);
	}
	
	/**
	 * @see ContentHandler#cloneHandler()
	 * @should return an UnstructuredDataHandler instance with the same content type
	 */
	@Override
	public UnstructuredDataHandler cloneHandler() {
		return new UnstructuredDataHandler();
	}
}
