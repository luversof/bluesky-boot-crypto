package io.github.luversof.boot.security.crypto;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;

import io.github.luversof.boot.security.crypto.encrypt.BlueskyTextEncryptor;
import io.github.luversof.boot.security.crypto.factory.TextEncryptorFactories;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class TextEncryptorTests {

	@Test
	void encrypTest() {
		var text = "test text!!!";
		
		// create random salt
		var salt = KeyGenerators.string().generateKey();
		log.debug("salt : {}", salt);
		
		log.debug("keyGenerator : {}", KeyGenerators.string().generateKey());
		
		{
			var encryptor = Encryptors.text("password", "076e1bf7569c999e");
			var encryptText = encryptor.encrypt(text);
			log.debug("encryptText : {}", encryptText);
			var decryptText = encryptor.decrypt(encryptText);
			log.debug("decryptText : {}, {}", text.equals(decryptText), decryptText);
		}
		
		{
			var encryptor = Encryptors.delux("pass", "076e1bf7569c999e");
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
	void textEncryptorFactoriesTest() {
		var delegatingTextEncryptor = TextEncryptorFactories.getDelegatingTextEncryptor();
		var encryptedText = delegatingTextEncryptor.encrypt("text", "암호화할 값");
		log.debug("encryptedText : {}", encryptedText);
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
		List<BlueskyTextEncryptor> blueskyTextEncryptorList = SpringFactoriesLoader.loadFactories(BlueskyTextEncryptor.class, BlueskyTextEncryptor.class.getClassLoader());
		log.debug("factories : {}", blueskyTextEncryptorList);
	}
}
