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
package org.openmrs.module.shr.contenthandler.api.impl;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.shr.contenthandler.UnstructuredDataHandler;
import org.openmrs.module.shr.contenthandler.api.AlreadyRegisteredException;
import org.openmrs.module.shr.contenthandler.api.CodedValue;
import org.openmrs.module.shr.contenthandler.api.ContentHandler;
import org.openmrs.module.shr.contenthandler.api.ContentHandlerService;
import org.openmrs.module.shr.contenthandler.api.InvalidCodedValueException;
import org.openmrs.module.shr.contenthandler.api.InvalidContentTypeException;

/**
 * It is a default implementation of {@link ContentHandlerService}.
 */
public class ContentHandlerServiceImpl extends BaseOpenmrsService implements ContentHandlerService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	protected final Map<String, ContentHandler> contentTypeHandlers = new HashMap<String, ContentHandler>();
	protected final Map<TypeFormatCode, ContentHandler> typeFormatCodeHandlers = new HashMap<TypeFormatCode, ContentHandler>();
	

	@Override
	public ContentHandler getContentHandler(String contentType) {
		if (contentType==null || contentType.isEmpty() || !contentTypeHandlers.containsKey(contentType)) {
			return getDefaultHandler(contentType);
		}
		
		return contentTypeHandlers.get(contentType).cloneHandler();
	}

	@Override
	public void registerContentHandler(String contentType,
			ContentHandler prototype) throws AlreadyRegisteredException, InvalidContentTypeException {
		
		if (prototype==null) {
			throw new NullPointerException();
		}
		
		if (!isValidContentType(contentType)) {
			throw new InvalidContentTypeException();
		}
		
		if (contentTypeHandlers.containsKey(contentType)) {
			throw new AlreadyRegisteredException();
		}
		
		contentTypeHandlers.put(contentType, prototype);
	}
	
	private static boolean isValidContentType(String contentType) {
		//Relaxed validation; just look for something/something
		return contentType!=null && !contentType.isEmpty() &&
			contentType.matches("[\\w\\+\\-\\.]+\\/[\\w\\+\\-\\.]+");
	}

	@Override
	public void deregisterContentHandler(String contentType) {
		contentTypeHandlers.remove(contentType);
	}

	@Override
	public ContentHandler getContentHandler(CodedValue typeCode, CodedValue formatCode) {
		if (typeCode==null || typeCode.getCode().isEmpty() || typeCode.getCodingScheme().isEmpty() ||
			formatCode==null || formatCode.getCode().isEmpty() || formatCode.getCodingScheme().isEmpty() ||
			!typeFormatCodeHandlers.containsKey(new TypeFormatCode(typeCode, formatCode))) {
			return getDefaultHandler(typeCode, formatCode);
		}
		
		return typeFormatCodeHandlers.get(new TypeFormatCode(typeCode, formatCode)).cloneHandler();
	}

	@Override
	public void registerContentHandler(CodedValue typeCode, CodedValue formatCode,
			ContentHandler prototype) throws AlreadyRegisteredException, InvalidCodedValueException {
		if (prototype==null) {
			throw new NullPointerException();
		}
		
		if (typeCode==null || typeCode.getCode().isEmpty() || typeCode.getCodingScheme().isEmpty() ||
			formatCode==null || formatCode.getCode().isEmpty() || formatCode.getCodingScheme().isEmpty()) {
			throw new InvalidCodedValueException();
		}
		
		TypeFormatCode codes = new TypeFormatCode(typeCode, formatCode);
		
		if (typeFormatCodeHandlers.containsKey(codes)) {
			throw new AlreadyRegisteredException();
		}
		
		typeFormatCodeHandlers.put(codes, prototype);
		
	}

	@Override
	public void deregisterContentHandler(CodedValue typeCode, CodedValue formatCode) {
		typeFormatCodeHandlers.remove(new TypeFormatCode(typeCode, formatCode));
		
	}
	
	private static class TypeFormatCode {
		CodedValue typeCode;
		CodedValue formatCode;
		
		TypeFormatCode(CodedValue typeCode, CodedValue formatCode) {
			this.typeCode = typeCode;
			this.formatCode = formatCode;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((formatCode == null) ? 0 : formatCode.hashCode());
			result = prime * result
					+ ((typeCode == null) ? 0 : typeCode.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			TypeFormatCode other = (TypeFormatCode) obj;
			if (formatCode == null) {
				if (other.formatCode != null) {
					return false;
				}
			} else if (!formatCode.equals(other.formatCode))
				return false;
			if (typeCode == null) {
				if (other.typeCode != null) {
					return false;
				}
			} else if (!typeCode.equals(other.typeCode)) {
				return false;
			}
			return true;
		}
	}

	
	@Override
	public ContentHandler getDefaultHandler(String contentType) {
		return new UnstructuredDataHandler(contentType);
	}

	@Override
	public ContentHandler getDefaultHandler(CodedValue typeCode, CodedValue formatCode) {
		return new UnstructuredDataHandler(typeCode, formatCode);
	}
}