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
