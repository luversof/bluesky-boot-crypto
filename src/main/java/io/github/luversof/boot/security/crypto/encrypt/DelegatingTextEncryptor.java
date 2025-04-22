package io.github.luversof.boot.security.crypto.encrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 암복호화 처리를 복수로 사용할 수 있도록 제공하기 위해 사용
 * 암호화된 text의 경우 prefix로 {암호화키}값  의 형태로 복호화 대상 textEncryptor를 추즉할 수 있지만
 * 복호화의 경우 별도의 지정이 없다면 여러 encryptor중 기본 사용 encryptor가 지정되어 있어야 한고 해당 encryptor로 암호화를 처리한다.
 * Spring 
 * @author bluesky
 *
 */
@Slf4j
public class DelegatingTextEncryptor implements TextEncryptor {
	
	private static final String DEFAULT_ID_PREFIX = "{";

	private static final String DEFAULT_ID_SUFFIX = "}";
	
	private final String idPrefix;
	
	private final String idSuffix;
	
	private final String defaultTextEncryptorId;
	
	private final Map<String, TextEncryptor> textEncryptorMap;
	
	public DelegatingTextEncryptor(String defaultTextEncryptorId, Map<String, TextEncryptor> textEncryptorMap) {
		this(defaultTextEncryptorId, textEncryptorMap, DEFAULT_ID_PREFIX, DEFAULT_ID_SUFFIX);
	}
	
	public DelegatingTextEncryptor(String defaultTextEncryptorId, Map<String, TextEncryptor> textEncryptorMap, String idPrefix, String idSuffix) {
		
		if (defaultTextEncryptorId == null) {
			throw new IllegalArgumentException("defaultId cannot be null");
		}
		
		if (idPrefix == null) {
			throw new IllegalArgumentException("prefix cannot be null");
		}
		
		if (idSuffix == null || idSuffix.isEmpty()) {
			throw new IllegalArgumentException("suffix cannot be empty");
		}
		
		if (idPrefix.contains(idSuffix)) {
			throw new IllegalArgumentException("idPrefix " + idPrefix + " cannot contain idSuffix " + idSuffix);
		}
		
		if (!textEncryptorMap.containsKey(defaultTextEncryptorId)) {
			throw new IllegalArgumentException("defaultTextEncryptorId " + defaultTextEncryptorId + "is not found in textEncryptorMap" + textEncryptorMap);
		}
		
		for (String id : textEncryptorMap.keySet()) {
			if (id == null) {
				continue;
			}
			if (!idPrefix.isEmpty() && id.contains(idPrefix)) {
				throw new IllegalArgumentException("id " + id + " cannot contain " + idPrefix);
			}
			if (id.contains(idSuffix)) {
				throw new IllegalArgumentException("id " + id + " cannot contain " + idSuffix);
			}
		}
		
		this.defaultTextEncryptorId = defaultTextEncryptorId;
		this.textEncryptorMap = new HashMap<>(textEncryptorMap);
		this.idPrefix = idPrefix;
		this.idSuffix = idSuffix;
	}

	@Override
	public String encrypt(String text) {
		return encrypt(defaultTextEncryptorId, text);
	}
	
	public String encrypt(String textEncryptorId, String text) {
		return this.idPrefix + textEncryptorId + this.idSuffix + this.textEncryptorMap.get(textEncryptorId).encrypt(text);
	}
	
	/**
	 * encryptedText는 textEncryptorId와 조합된 형태인 경우 해당 textEncryptor를 찾아 decrypt 처리
	 */
	@Override
	public String decrypt(String encryptedText) {
		
		String textEncryptorId = extractTextEncryptorId(encryptedText);
		if (textEncryptorId == null) {
			return encryptedText;
		}
		
		if (!this.textEncryptorMap.containsKey(textEncryptorId)) {
			return encryptedText;
		}
		
		return this.textEncryptorMap.get(textEncryptorId).decrypt(extractEncryptedText(encryptedText));
	}
	
	public boolean isEncrypted(String encryptedText) {
		return extractTextEncryptorId(encryptedText) != null;
	}
	
	private String extractTextEncryptorId(String encryptedText) {
		if (encryptedText == null) {
			return null;
		}
		int start = encryptedText.indexOf(this.idPrefix);
		if (start != 0) {
			return null;
		}
		
		int end = encryptedText.indexOf(this.idSuffix, start);
		if (end < 0) {
			return null;
		}
		
		var textEncryptorId = encryptedText.substring(start + this.idPrefix.length(), end);
		if (!this.textEncryptorMap.containsKey(textEncryptorId)) {
			log.debug("The textEncryptorId does not exist in the textEncryptorMap : {}", textEncryptorId);
			return null;
		}
		return textEncryptorId;
	}
	
	private String extractEncryptedText(String encryptedText) {
		if (encryptedText == null) {
			return encryptedText;
		}
		int start = encryptedText.indexOf(this.idPrefix);
		if (start != 0) {
			return encryptedText;
		}
		
		int end = encryptedText.indexOf(this.idSuffix, start);
		if (end < 0) {
			return encryptedText;
		}
		
		return encryptedText.substring(end + 1);
	}
	
	/**
	 * Encryptor를 직접 사용하는 경우
	 * @param textEncryptorId
	 * @return
	 */
	public TextEncryptor getEncryptor(String textEncryptorId) {
		if (!textEncryptorMap.containsKey(textEncryptorId)) {
			throw new IllegalArgumentException("textEncryptorId " + textEncryptorId + "is not found in textEncryptorMap" + textEncryptorMap);
		}
		
		return this.textEncryptorMap.get(textEncryptorId);
	}
	
	public boolean isTextEncryptorMapEmpty() {
		return CollectionUtils.isEmpty(this.textEncryptorMap);
	}
	
	public Set<String> textEncryptorMapKeySet() {
		return this.textEncryptorMap.keySet();
	}
	
	public DelegatingTextEncryptor addTextEncryptor(String textEncryptorId, TextEncryptor textEncryptor) {
		this.textEncryptorMap.put(textEncryptorId, textEncryptor);
		return this;
	}
	
	public DelegatingTextEncryptor addTextEncryptor(Map<String, TextEncryptor> textEncryptorMap) {
		this.textEncryptorMap.putAll(textEncryptorMap);
		return this;
	}	

}
