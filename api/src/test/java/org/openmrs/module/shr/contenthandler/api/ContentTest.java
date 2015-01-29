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

import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.openmrs.module.shr.contenthandler.api.Content.CompressionFormat;
import org.openmrs.module.shr.contenthandler.api.Content.Representation;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ContentTest {
	
	private static final byte[] TEST_DATA = "<test>This is a test string. It is awesome.</test>".getBytes();
	private static final CodedValue TEST_CODE = new CodedValue("xml", "xml", "xml");
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8001);
	
	private void setupContentMockService(byte[] body) {
		stubFor(get(urlEqualTo("/resource"))
			.willReturn(aResponse()
				.withStatus(200)
				.withBody(body)
			)
		);
	}
	
	

	/**
	 * @see Content#getRawData()
	 * @verifies return the payload string as a byte array if it's not encoded or compressed
	 */
	@Test
	public void getRawData_shouldReturnThePayloadStringAsAByteArrayIfItsNotEncodedOrCompressed()
			throws Exception {
		Content content = new Content("testId", TEST_DATA, TEST_CODE, TEST_CODE, "text/xml");
		assertEquals(TEST_DATA, content.getPayload());
	}
	
	
	private static byte[] compressDeflate(String content) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DeflaterOutputStream deflateOut = new DeflaterOutputStream(out, new Deflater(0, true));
		deflateOut.write(content.getBytes());
		IOUtils.closeQuietly(deflateOut);
		return out.toByteArray();
	}
	
	private static byte[] compressGZip(String content) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut = new GZIPOutputStream(out);
		gzipOut.write(content.getBytes());
		IOUtils.closeQuietly(gzipOut);
		return out.toByteArray();
	}
	
	private static byte[] compressZLib(String content) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DeflaterOutputStream zlibOut = new DeflaterOutputStream(out);
		zlibOut.write(content.getBytes());
		IOUtils.closeQuietly(zlibOut);
		return out.toByteArray();
	}
}