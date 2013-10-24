package org.openmrs.module.shr.contenthandler.api;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang.NotImplementedException;

/**
 * A text based data payload.
 */
public final class Content implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2101280624666285488L;


	public static enum Representation {
		/**
		 * Text
		 */
		TXT,
		/**
		 * Base64
		 */
		B64
	}
	
	public static enum CompressionFormat {
		/**
		 * Deflate
		 */
		DF,
		/**
		 * GZip
		 */
		GZ,
		/**
		 * ZLib
		 */
		ZL,
		/**
		 * Compress
		 * @deprecated
		 */
		Z
	}
	
	private final String payload;
	private final String formatCode;
	private final String contentType;
	private final String encoding;
	private final Representation representation;
	private final CompressionFormat compressionFormat;
	private final Locale language;
	private final boolean payloadIsUrl;
	
	
	/**
	 * Creates a new Content object with a simple text payload. Good if the payload is an XML document for example.
	 * 
	 * @see #Content(String, boolean, String, String, Representation, CompressionFormat, Locale)
	 */
	public Content(String payload, String formatCode, String contentType) {
		this(payload, false, formatCode, contentType, null, Representation.TXT, null, null);
	}
	
	/**
	 * Creates a new Content object.
	 * 
	 * @param payload			The payload can either contain the content or a url referencing the content's location
	 * @param payloadIsUrl		The payload contains a URL string referencing a location where the content can be retrieved from
	 * @param formatCode		The content format code
	 * @param contentType		The content MIME type
	 * @param encoding			(Nullable) The character set used for the content
	 * @param representation	Indicates that the content is either text or base64 encoded
	 * @param compressionFormat (Nullable) The compression algorithm used by the content
	 * @param language			(Nullable) The content language
	 */
	public Content(String payload, boolean payloadIsUrl, String formatCode, String contentType, String encoding, Representation representation, CompressionFormat compressionFormat, Locale language) {
		this.payload = payload;
		this.formatCode = formatCode;
		this.contentType = contentType;
		this.encoding = encoding;
		this.representation = representation;
		this.compressionFormat = compressionFormat;
		this.language = language;
		this.payloadIsUrl = payloadIsUrl;
	}
	
	
	public String getPayload() {
		return payload;
	}
	
	public byte[] getUncompressedPayload() {
		//TODO
		throw new NotImplementedException();
	}
	
	public String getFormatCode() {
		return formatCode;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	/**
	 * Can be null
	 */
	public String getEncoding() {
		return encoding;
	}
	
	public Representation getRepresentation() {
		return representation;
	}
	
	public boolean isCompressed() {
		return compressionFormat!=null;
	}
	
	/**
	 * Can be null
	 */
	public CompressionFormat getCompressionFormat() {
		return compressionFormat;
	}
	
	/**
	 * Can be null
	 */
	public Locale getLanguage() {
		return language;
	}
	
	public boolean payloadIsUrl() {
		return payloadIsUrl;
	}
}
