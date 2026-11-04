package io.github.luversof.boot.security.crypto.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.encrypt.Encryptors;

class TextEncryptorFactoriesTests {

	/** 기본 키로 예전에 만든 값. 기본값을 바꾸면 이미 저장된 값이 모두 안 풀린다. */
	private static final String GOLDEN_TEXT = "{text}db48a52c359ca7a1fba8c9bd4d6f1cfdadadfccd17c66d83fdb775336d32bb0c";

	private static final String GOLDEN_DELUX = "{delux}6f333f61b4b2d103dad28d9f148e67c16de31a5c0e316e0341048aa4944b60261dd9a925f526d16b97003cb17e65";

	@Test
	void defaultKeyStillDecryptsExistingValues() {
		var encryptor = TextEncryptorFactories.getDelegatingTextEncryptor();

		assertThat(encryptor.decrypt(GOLDEN_TEXT)).isEqualTo("bluesky-golden");
		assertThat(encryptor.decrypt(GOLDEN_DELUX)).isEqualTo("bluesky-golden");
	}

	@Test
	void sharedInstanceKeepsAddedEncryptors() {
		// 예전에는 매번 새로 만들어 main 에서 addTextEncryptor 한 것이 사라졌다
		TextEncryptorFactories.getDelegatingTextEncryptor().addTextEncryptor("factoriesTestNoOp", Encryptors.noOpText());

		assertThat(TextEncryptorFactories.getDelegatingTextEncryptor()).isSameAs(TextEncryptorFactories.getDelegatingTextEncryptor());
		assertThat(TextEncryptorFactories.getDelegatingTextEncryptor().decrypt("{factoriesTestNoOp}plain")).isEqualTo("plain");
	}

	@Test
	void passwordAndSaltCanBeOverridden() {
		System.setProperty("bluesky-boot.crypto.password", "other-pass");
		System.setProperty("bluesky-boot.crypto.salt", "0123456789abcdef");
		try {
			var overridden = TextEncryptorFactories.createDelegatingTextEncryptor();
			var encrypted = overridden.encrypt("secret");

			assertThat(overridden.decrypt(encrypted)).isEqualTo("secret");
			// 다른 키라 기본 키로 만든 값은 못 푼다
			assertThatThrownBy(() -> overridden.decrypt(GOLDEN_TEXT)).isInstanceOf(IllegalStateException.class);
		} finally {
			System.clearProperty("bluesky-boot.crypto.password");
			System.clearProperty("bluesky-boot.crypto.salt");
		}
	}

	@Test
	void emptySettingFallsBackToDefault() {
		System.setProperty("bluesky-boot.crypto.password", "");
		try {
			assertThat(TextEncryptorFactories.setting("password", "fallback")).isEqualTo("fallback");
		} finally {
			System.clearProperty("bluesky-boot.crypto.password");
		}
	}
}
