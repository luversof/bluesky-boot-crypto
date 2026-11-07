package io.github.luversof.boot.security.crypto.env;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.security.crypto.encrypt.Encryptors;

import io.github.luversof.boot.security.crypto.factory.TextEncryptorFactories;
import io.github.luversof.boot.security.crypto.support.DecryptPropertySourceFactory;

class DecryptEnvironmentPostProcessorTests {

  @Test
  void decryptsEncryptedPropertiesAndLeavesOthers() {
    var encrypted = TextEncryptorFactories.getDelegatingTextEncryptor().encrypt("db-password");
    var environment = new StandardEnvironment();
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "test",
                Map.of(
                    "app.password",
                    encrypted,
                    "app.plain",
                    "as-is",
                    "app.list[0]",
                    encrypted,
                    "app.list[1]",
                    "second")));

    new DecryptEnvironmentPostProcessor()
        .postProcessEnvironment(environment, new SpringApplication());

    assertThat(environment.getProperty("app.password")).isEqualTo("db-password");
    assertThat(environment.getProperty("app.plain")).isEqualTo("as-is");
    assertThat(environment.getProperty("app.list[0]")).isEqualTo("db-password");
    assertThat(environment.getProperty("app.list[1]")).isEqualTo("second");
  }

  @Test
  void usesEncryptorsAddedToTheSharedInstance() {
    // 예전에는 이 후처리기가 자기 인스턴스를 따로 만들어 main 에서 추가한 encryptor 를 못 봤다
    TextEncryptorFactories.getDelegatingTextEncryptor()
        .addTextEncryptor("eppTestNoOp", Encryptors.noOpText());
    var environment = new StandardEnvironment();
    environment
        .getPropertySources()
        .addFirst(new MapPropertySource("test", Map.of("app.value", "{eppTestNoOp}plain")));

    new DecryptEnvironmentPostProcessor()
        .postProcessEnvironment(environment, new SpringApplication());

    assertThat(environment.getProperty("app.value")).isEqualTo("plain");
  }

  @Test
  void brokenCipherTextFailsWithTheKeyName() {
    var environment = new StandardEnvironment();
    environment
        .getPropertySources()
        .addFirst(new MapPropertySource("test", Map.of("app.broken", "{text}zz")));

    var processor = new DecryptEnvironmentPostProcessor();
    var application = new SpringApplication();

    assertThatThrownBy(() -> processor.postProcessEnvironment(environment, application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("app.broken");
  }

  @Test
  void propertySourceFactoryDecrypts() throws Exception {
    var encrypted = TextEncryptorFactories.getDelegatingTextEncryptor().encrypt("mongo-password");
    var resource =
        new EncodedResource(
            new ByteArrayResource(
                ("k1=" + encrypted + "\nk2=plain").getBytes(StandardCharsets.UTF_8)));

    var source = new DecryptPropertySourceFactory().createPropertySource("test", resource);

    assertThat(source.getProperty("k1")).isEqualTo("mongo-password");
    assertThat(source.getProperty("k2")).isEqualTo("plain");
  }
}
