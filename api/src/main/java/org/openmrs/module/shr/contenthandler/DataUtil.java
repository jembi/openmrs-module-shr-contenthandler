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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Various data utility methods.
 */
public class DataUtil {

	public static byte[] decodeBase64(byte[] content) {
		return Base64.decodeBase64(content);
	}
	
	public static byte[] fetchPayloadFromURL(String url) throws MalformedURLException, IOException {
		InputStream in = new URL(url).openStream();
		try {
			return IOUtils.toByteArray(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	public static byte[] uncompressDeflate(byte[] content) throws IOException {
		InflaterInputStream deflateIn = new InflaterInputStream(new ByteArrayInputStream(content), new Inflater(true));
		return copyToByteArray(deflateIn);
	}
	
	public static byte[] uncompressGZip(byte[] content) throws IOException {
		GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(content));
		return copyToByteArray(gzipIn);
	}
	
	public static byte[] uncompressZLib(byte[] content) throws IOException {
		InflaterInputStream deflateIn = new InflaterInputStream(new ByteArrayInputStream(content));
		return copyToByteArray(deflateIn);
	}
	
	private static byte[] copyToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		return out.toByteArray();
	}
}
