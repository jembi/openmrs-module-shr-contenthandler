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

import java.io.Serializable;

public final class CodedValue implements Comparable<CodedValue>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4638562168981161880L;
	
	
	private final String code;
	private final String codingScheme;
	private final String codingSchemeName;
	
	public CodedValue(String code, String codingScheme) {
		this(code, codingScheme, null);
	}
	
	public CodedValue(String code, String codingScheme, String codingSchemeName) {
		if (code==null || codingScheme==null) {
			throw new NullPointerException();
		}
		
		this.code = code;
		this.codingScheme = codingScheme;
		this.codingSchemeName = codingSchemeName;
	}

	public String getCode() {
		return code;
	}

	public String getCodingScheme() {
		return codingScheme;
	}

	public String getCodingSchemeName() {
		return codingSchemeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result
				+ ((codingScheme == null) ? 0 : codingScheme.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodedValue other = (CodedValue) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (codingScheme == null) {
			if (other.codingScheme != null)
				return false;
		} else if (!codingScheme.equals(other.codingScheme))
			return false;
		return true;
	}

	@Override
	public int compareTo(CodedValue o) {
		int schemeComp = codingScheme.compareTo(o.codingScheme);
		if (schemeComp!=0) {
			return schemeComp;
		}
		return code.compareTo(o.code);
	}

	@Override
	public String toString() {
		return "CodedValue [code=" + code + ", codingScheme=" + codingScheme
				+ ", codingSchemeName=" + codingSchemeName + "]";
	}
}
