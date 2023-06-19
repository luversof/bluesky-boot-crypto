package io.github.luversof.boot.security.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;

import io.github.luversof.boot.security.crypto.factory.TextEncryptorFactories;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class TextEncryptorTests {

	@Test
	void encrypTest() {
		var text = "test text!!!";
		
		log.debug("keyGenerator : {}", KeyGenerators.string().generateKey());
		
		{
			var encryptor = Encryptors.text("password", "8560b4f4b3");
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
	
	@Test
	void test() {
		var encryptor = Encryptors.text("password", "c2174fcfa78656f5");
		var decryptText = encryptor.decrypt("66e3bfc1f5bcfb2a3b7b1d44f1db416b6c9a38571233cc139a64d358fe0e20a0");
		log.debug("decryptText : {}", decryptText);
	}
}
