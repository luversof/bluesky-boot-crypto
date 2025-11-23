package io.github.luversof.boot.security.crypto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TextEncryptorTestsData {

	T1("test", ""),
	;
	
	private String text;
	private String encryptedText;
}
