package io.github.luversof.boot.security.crypto.encrypt;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class NoOpBlueskyTextEncryptor2 implements BlueskyTextEncryptor {
	
	static final TextEncryptor INSTANCE = Encryptors.noOpText();

	@Override
	public String encrypt(String text) {
		return INSTANCE.encrypt(text);
	}

	@Override
	public String decrypt(String encryptedText) {
		return INSTANCE.decrypt(encryptedText);
	}

	@Override
	public String getEncryptorId() {
		return "noOpText";
	}

}
