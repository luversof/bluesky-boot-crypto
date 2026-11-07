package io.github.luversof.boot.security.crypto.encrypt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

class DelegatingTextEncryptorTests {

  private final DelegatingTextEncryptor encryptor =
      new DelegatingTextEncryptor(
          "a",
          Map.of("a", Encryptors.text("pw", "0123456789abcdef"), "noop", Encryptors.noOpText()));

  @Test
  void roundTripWithIdPrefix() {
    var encrypted = encryptor.encrypt("hello");

    assertThat(encrypted).startsWith("{a}");
    assertThat(encryptor.isEncrypted(encrypted)).isTrue();
    assertThat(encryptor.decrypt(encrypted)).isEqualTo("hello");
  }

  @Test
  void plainOrUnknownIdIsReturnedAsIs() {
    assertThat(encryptor.decrypt("plain")).isEqualTo("plain");
    assertThat(encryptor.decrypt("{unknown}abc")).isEqualTo("{unknown}abc");
    assertThat(encryptor.isEncrypted("{unknown}abc")).isFalse();
    assertThat(encryptor.decrypt(null)).isNull();
  }

  @Test
  void multiCharacterSuffixIsCutCompletely() {
    // 예전에는 접미사를 1글자로 가정해 "]" 하나가 값 앞에 남았다
    var bracketed =
        new DelegatingTextEncryptor("noop", Map.of("noop", Encryptors.noOpText()), "[[", "]]");

    assertThat(bracketed.decrypt("[[noop]]value")).isEqualTo("value");
    assertThat(bracketed.encrypt("value")).isEqualTo("[[noop]]value");
  }

  @Test
  void rejectsNullId() {
    var map = new HashMap<String, TextEncryptor>();
    map.put("a", Encryptors.noOpText());
    map.put(null, Encryptors.noOpText());

    assertThatThrownBy(() -> new DelegatingTextEncryptor("a", map))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void addedEncryptorIsUsedForDecrypt() {
    var mutable = new DelegatingTextEncryptor("noop", Map.of("noop", Encryptors.noOpText()));
    mutable.addTextEncryptor("other", Encryptors.noOpText());

    assertThat(mutable.decrypt("{other}x")).isEqualTo("x");
  }
}
