package io.github.luversof.boot.security.crypto;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
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
		var encryptText = encryptor.encrypt("test");
		log.debug("encryptText : {}", encryptText);
		var decryptText = encryptor.decrypt(encryptText);
		log.debug("decryptText : {}", decryptText);
	}
	
	@Test
	void springFactoriesLoaderTest() {
		List<TextEncryptor> factories = SpringFactoriesLoader.loadFactories(TextEncryptor.class, null);
		
		log.debug("factories : {}", factories);
	}
}
