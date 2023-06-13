package io.github.luversof.boot.security.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.encrypt.Encryptors;

import io.github.luversof.boot.security.crypto.factory.TextEncryptorFactories;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class TextEncryptorTests {

	@Test
	void encryptTest() {
		var text = "test text!!!";
		
		{
			var encryptor = Encryptors.text("pass", "8560b4f4b3");
			var encryptText = encryptor.encrypt(text);
			log.debug("encryptText : {}", encryptText);
			var decryptText = encryptor.decrypt(encryptText);
			log.debug("decryptText : {}, {}", text.equals(decryptText), decryptText);
		}
		
		{
			var encryptor = Encryptors.delux("pass", "8560b4f4b3");
			var encryptText = encryptor.encrypt(text);
			log.debug("encryptText : {}", encryptText);
			var decryptText = encryptor.decrypt(encryptText);
			log.debug("decryptText : {}, {}", text.equals(decryptText), decryptText);
		}
		
		{
			var encryptor = Encryptors.noOpText();
			var encryptText = encryptor.encrypt(text);
			log.debug("encryptText : {}", encryptText);
			var decryptText = encryptor.decrypt(encryptText);
			log.debug("decryptText : {}, {}", text.equals(decryptText), decryptText);
		}
		
		{
			var encryptor = TextEncryptorFactories.createDelegatingTextEncryptor();
			var encryptText = encryptor.encrypt(text);
			log.debug("encryptText : {}", encryptText);
			var decryptText = encryptor.decrypt(encryptText);
			log.debug("decryptText : {}, {}", text.equals(decryptText), decryptText);
		}
		
	}
}
