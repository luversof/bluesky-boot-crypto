package io.github.luversof.boot.security.crypto.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import io.github.luversof.boot.security.crypto.encrypt.BlueskyTextEncryptor;
import io.github.luversof.boot.security.crypto.encrypt.DelegatingTextEncryptor;

/**
 * 앱 전체가 함께 쓰는 {@link DelegatingTextEncryptor} 를 제공한다.
 *
 * <p>설정 복호화({@code DecryptEnvironmentPostProcessor}), {@code DecryptPropertySourceFactory},
 * connection-info 리더, dev-check 화면이 모두 같은 인스턴스를 쓴다. 따라서 {@code main} 에서 {@code
 * getDelegatingTextEncryptor().addTextEncryptor(...)} 로 추가한 encryptor 를 어디서나 쓸 수 있다.
 *
 * <p>encryptor 를 고르는 순서:
 *
 * <ol>
 *   <li>{@code META-INF/spring.factories} 에 등록한 {@link BlueskyTextEncryptor} 가 있으면 그것들 ({@link
 *       BlueskyTextEncryptor#isDefaultEncryptor()} 인 것이 기본, 없으면 첫 번째)
 *   <li>없으면 기본 encryptor {@code text}·{@code delux}. 비밀번호와 salt 는 시스템 프로퍼티 {@code
 *       bluesky-boot.crypto.password}·{@code bluesky-boot.crypto.salt} 또는 환경변수 {@code
 *       BLUESKY_BOOT_CRYPTO_PASSWORD}·{@code BLUESKY_BOOT_CRYPTO_SALT} 로 바꿀 수 있다. 지정하지 않으면 코드에 적힌
 *       공개 기본값을 쓰므로 암호화라기보다 가리기 수준이다.
 * </ol>
 */
public final class TextEncryptorFactories {

  static final String DEFAULT_PASSWORD = "pass";

  static final String DEFAULT_SALT = "076e1bf7569c999e";

  private static final String DEFAULT_TEXT_ENCRYPTOR_ID = "text";

  private static volatile DelegatingTextEncryptor
      delegatingTextEncryptor; // NOSONAR java:S3077 double-checked locking 용 참조이고

  // DelegatingTextEncryptor 는 final 필드와 ConcurrentHashMap 으로 스레드 안전함

  private TextEncryptorFactories() {}

  /** 앱 전체가 함께 쓰는 인스턴스. 처음 부를 때 한 번 만든다(키 유도 비용이 커서 매번 만들지 않는다). */
  public static DelegatingTextEncryptor getDelegatingTextEncryptor() {
    var cached = delegatingTextEncryptor;
    if (cached != null) {
      return cached;
    }
    synchronized (TextEncryptorFactories.class) {
      if (delegatingTextEncryptor == null) {
        delegatingTextEncryptor = createFromSpringFactoriesOrDefault();
      }
      return delegatingTextEncryptor;
    }
  }

  /** 기본 encryptor({@code text}·{@code delux})로 새 인스턴스를 만든다. 함께 쓰는 인스턴스와는 별개다. */
  public static DelegatingTextEncryptor createDelegatingTextEncryptor() {
    return new DelegatingTextEncryptor(DEFAULT_TEXT_ENCRYPTOR_ID, getDefaultTextEncryptorMap());
  }

  public static DelegatingTextEncryptor createDelegatingTextEncryptor(
      String defaultTextEncryptorId, TextEncryptor textEncryptor) {
    return new DelegatingTextEncryptor(
        defaultTextEncryptorId, Map.of(defaultTextEncryptorId, textEncryptor));
  }

  public static DelegatingTextEncryptor createDelegatingTextEncryptor(
      String defaultTextEncryptorId, Map<String, TextEncryptor> textEncryptorMap) {
    return new DelegatingTextEncryptor(defaultTextEncryptorId, textEncryptorMap);
  }

  private static DelegatingTextEncryptor createFromSpringFactoriesOrDefault() {
    List<BlueskyTextEncryptor> blueskyTextEncryptorList =
        SpringFactoriesLoader.loadFactories(
            BlueskyTextEncryptor.class, BlueskyTextEncryptor.class.getClassLoader());
    if (blueskyTextEncryptorList.isEmpty()) {
      return createDelegatingTextEncryptor();
    }
    var defaultEncryptor =
        blueskyTextEncryptorList.stream()
            .filter(BlueskyTextEncryptor::isDefaultEncryptor)
            .findFirst()
            .orElse(blueskyTextEncryptorList.getFirst());
    Map<String, TextEncryptor> textEncryptorMap =
        blueskyTextEncryptorList.stream()
            .collect(
                Collectors.toMap(
                    BlueskyTextEncryptor::getEncryptorId,
                    blueskyTextEncryptor -> blueskyTextEncryptor));
    return createDelegatingTextEncryptor(defaultEncryptor.getEncryptorId(), textEncryptorMap);
  }

  private static Map<String, TextEncryptor> getDefaultTextEncryptorMap() {
    var password = setting("password", DEFAULT_PASSWORD);
    var salt = setting("salt", DEFAULT_SALT);
    var textEncryptorMap = new HashMap<String, TextEncryptor>();
    textEncryptorMap.put("text", Encryptors.text(password, salt));
    textEncryptorMap.put("delux", Encryptors.delux(password, salt));
    return textEncryptorMap;
  }

  /** Spring Environment 가 만들어지기 전에 쓰이므로 시스템 프로퍼티와 환경변수를 직접 읽는다. */
  static String setting(String name, String defaultValue) {
    var value = System.getProperty("bluesky-boot.crypto." + name);
    if (value == null || value.isEmpty()) {
      value = System.getenv("BLUESKY_BOOT_CRYPTO_" + name.toUpperCase(Locale.ROOT));
    }
    return value == null || value.isEmpty() ? defaultValue : value;
  }
}
