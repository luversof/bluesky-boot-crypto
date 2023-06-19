package io.github.luversof.boot.security.crypto.factory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import io.github.luversof.boot.security.crypto.encrypt.DelegatingTextEncryptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextEncryptorFactories {
	
	private static DelegatingTextEncryptor delegatingTextEncryptor;
	
	private static String defaultTextEncryptorId = "text";
	
	public static DelegatingTextEncryptor getDelegatingTextEncryptor() {
		if (delegatingTextEncryptor == null) {
			return createDelegatingTextEncryptor();
		}
		return delegatingTextEncryptor;
	}

	/**
	 * 별도 Encryptor를 지정하지 않은 경우 기본 샘플 Encryptor 를 사용
	 * @return
	 */
	public static DelegatingTextEncryptor createDelegatingTextEncryptor() {
		return new DelegatingTextEncryptor(defaultTextEncryptorId, getDefaultTextEncryptorMap());
	}
	
	public static DelegatingTextEncryptor createDelegatingTextEncryptor(String defaultTextEncryptorId, TextEncryptor textEncryptor) {
		return new DelegatingTextEncryptor(defaultTextEncryptorId, Map.of(defaultTextEncryptorId, textEncryptor));
	}
	
	public static DelegatingTextEncryptor createDelegatingTextEncryptor(String defaultTextEncryptorId, Map<String, TextEncryptor> textEncryptorMap) {
		return new DelegatingTextEncryptor(defaultTextEncryptorId, textEncryptorMap);
	}
	
	private static Map<String, TextEncryptor> getDefaultTextEncryptorMap() {
		var textEncryptorMap = new HashMap<String, TextEncryptor>();
		textEncryptorMap.put("text", Encryptors.text("pass", "8560b4f4b3"));
		textEncryptorMap.put("delux", Encryptors.delux("pass", "8560b4f4b3"));
		return textEncryptorMap;
	}
}
