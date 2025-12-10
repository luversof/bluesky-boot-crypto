package io.github.luversof.boot.security.crypto;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.security.crypto.encrypt.Encryptors;

import io.github.luversof.boot.security.crypto.encrypt.BlueskyTextEncryptor;
import io.github.luversof.boot.security.crypto.factory.TextEncryptorFactories;

class TextEncryptorTests {
	
	private static final Logger log = LoggerFactory.getLogger(TextEncryptorTests.class);

	@ParameterizedTest
	@EnumSource(TextEncryptorTestsData.class)
	void encrypTest(TextEncryptorTestsData data) {
		var text = TextEncryptorFactories.createDelegatingTextEncryptor().encrypt(data.getEncryptedText());
		log.debug("text : {}", text);
		
	}
	
	
	@Test
	void textEncryptorFactoriesTest() {
		var delegatingTextEncryptor = TextEncryptorFactories.getDelegatingTextEncryptor();
		var encryptedText = delegatingTextEncryptor.encrypt("text", "암호화할 값");
		log.debug("encryptedText : {}", encryptedText);
	}
	
	@Test
	void test() {
		var encryptor = Encryptors.text("password", "076e1bf7569c999e");
		var encryptText = encryptor.encrypt("true, test text!!!");
		log.debug("encryptText : {}", encryptText);
		var decryptText = encryptor.decrypt("07d9e2cf09288e43b8d706a2bdcfcee95490fb91f022056fc6d523ff3fc1d3f50aa4c1ae078761c285a03257049cd62d");
		log.debug("decryptText : {}", decryptText);
	}
	
	@Test
	void springFactoriesLoaderTest() {
		List<BlueskyTextEncryptor> blueskyTextEncryptorList = SpringFactoriesLoader.loadFactories(BlueskyTextEncryptor.class, BlueskyTextEncryptor.class.getClassLoader());
		log.debug("factories : {}", blueskyTextEncryptorList);
	}
}
