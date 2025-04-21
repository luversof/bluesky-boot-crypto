package io.github.luversof.boot.security.crypto.encrypt;

import org.springframework.security.crypto.encrypt.TextEncryptor;

public interface BlueskyTextEncryptor extends TextEncryptor {

	String getEncryptorId();
	
	default boolean isDefaultEncryptor() {
		return false;
	}

}
