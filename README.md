# bluesky-boot-crypto

<!--
bluesky-boot-cryto는 spring boot 기반 프로젝트에서 properties의 값을 암호화 하기 위한 기능을 제공하는 라이브러리입니다.

사용자가 각자가 구현한 TextEncryptor를  추가하여 사용할 수 있으며 이전에 사용한 TextEncryptor의 암호화 처리도 유지하여 사용이 가능하도록 여러 개의 TextEncryptor를 관리할 수 있습니다.

암호화된 값은 `{textEncryptorId}암호화된값`  과 같은 형태로 저장되며 prefix 값으로 어떤 textEncryptor를 사용하여 복호화 할지 판단합니다.

spring-security에서 사용하는 DelegatingPasswordEncoder의 암호화 방식 그대로 DelegatingTextEncryptor로 구현하였습니다.
-->

bluesky-boot-cryto is a library that provides functionality for encrypting values of properties in spring boot based projects.

It allows users to add their own implemented TextEncryptor and use it, and it can manage multiple TextEncryptors for use by preserving the encryption processing of previously used `TextEncryptors` .

The encrypted value is stored in a form like `{textEncryptorId}encryptedvalue` and a prefix value determines which textEncryptor to use to decrypt it.

The encryption method of DelegatingPasswordEncoder used by spring-security is implemented as DelegatingTextEncryptor.

**Prerequisites**

- [Java 25](https://openjdk.java.net/)
- [Spring Boot 4.1](https://spring.io/)

## settings

### maven dependencies

<!--
maven dependencies를 추가하면 제공되는 `DecryptEnvironmentPostProcessor` 가 설정됩니다.
-->

Adding the maven dependencies will set up the provided `DecryptEnvironmentPostProcessor` .


```pom.xml
<dependencies>
    <dependency>
        <groupId>io.github.luversof</groupId>
        <artifactId>bluesky-boot-crypto</artifactId> 
        <version>${currentVersion}</version> 
    </dependency>
</dependencies>
```

## usage

<!--
별다른 설정을 하지 않은  경우 기본 샘플로 제공되는 2개의 Encryptor가 사용됩니다.
-->

If you don't set anything else, the two encryptors provided in the default sample will be used.

```java
private static Map<String, TextEncryptor> getDefaultTextEncryptorMap() {
	var textEncryptorMap = new HashMap<String, TextEncryptor>();
	textEncryptorMap.put("text", Encryptors.text("pass", "076e1bf7569c999e"));
	textEncryptorMap.put("delux", Encryptors.delux("pass", "076e1bf7569c999e"));
	return textEncryptorMap;
}
```

<!--
기본 비밀번호와 salt 는 코드에 공개되어 있어 암호화라기보다 가리기 수준입니다.
시스템 프로퍼티나 환경변수로 바꿀 수 있습니다. 바꾸면 기존 기본 키로 암호화한 값은 풀리지 않으니 다시 암호화해야 합니다.
-->

The default password and salt are public in the source code, so they only obscure values.
You can override them with a system property or an environment variable. After changing them, values encrypted with the old key no longer decrypt and must be re-encrypted.

| system property | environment variable | default |
| ------------- | ------------- | ------------- |
| `bluesky-boot.crypto.password` | `BLUESKY_BOOT_CRYPTO_PASSWORD` | `pass` |
| `bluesky-boot.crypto.salt` | `BLUESKY_BOOT_CRYPTO_SALT` | `076e1bf7569c999e` (hex) |

<!--
사용하려는 `TextEncryptor` 가 있다면 application 시작 전에 앱 전체가 함께 쓰는 `TextEncryptorFactories.getDelegatingTextEncryptor()` 에 추가하면 됩니다.
설정 복호화, `DecryptPropertySourceFactory`, connection-info, dev-check 화면이 모두 같은 인스턴스를 씁니다.

기본 encryptor 를 바꾸려면 `META-INF/spring.factories` 에 `BlueskyTextEncryptor` 구현을 등록하고 하나에 `isDefaultEncryptor()` 를 true 로 두면 됩니다.
-->

If you have a `TextEncryptor` that you want to use, add it to the shared `TextEncryptorFactories.getDelegatingTextEncryptor()` before starting your application.
Property decryption, `DecryptPropertySourceFactory`, connection-info and the dev-check pages all use that same instance.

To change the default encryptor, register `BlueskyTextEncryptor` implementations in `META-INF/spring.factories` and return true from `isDefaultEncryptor()` on one of them.


```java
@SpringBootApplication
public class Application {
	public static void main(String[] args) throws Throwable {
		TextEncryptorFactories.getDelegatingTextEncryptor().addTextEncryptor("encryptorId", encryptor);
		SpringApplication.run(Application.class, args);
	}
}
```


<!--
properties에 textEncryptor로 암호화된 값을 추가하면 됩니다.

위에서 만든 textEncryptor로 암호화합니다.
-->

You can do this by adding the value encrypted with textEncryptor to properties.

Encrypt it with the textEncryptor you created above.

```java
var delegatingTextEncryptor = TextEncryptorFactories.getDelegatingTextEncryptor();
var encryptedStr = delegatingTextEncryptor.encrypt("someValue");
```

<!--
delegatingTextEncryptor로 암호화한 값은 `{encryptorId}암호화된값` 과 같은 형태로 되어있습니다.

예를 들어 아래와 같이 테스트를 하면
-->

The value encrypted with delegatingTextEncryptor looks like this: `{encryptorId}encrypted value` .

For example, if you run the test below:

```java
@Test
void encryptTest() {
	var text = "test text!!!";
	var encryptor = TextEncryptorFactories.createDelegatingTextEncryptor();
	var encryptText = encryptor.encrypt(text);
	log.debug("encryptText : {}", encryptText);
	var decryptText = encryptor.decrypt(encryptText);
	log.debug("decryptText : {}, {}", text.equals(decryptText), decryptText);
}
```

<!--
아래와 같은 결과를 확인할 수 있습니다.
-->

You can see the results below.

```
encryptText : {text}07d9e2cf09288e43b8d706a2bdcfcee95490fb91f022056fc6d523ff3fc1d3f50aa4c1ae078761c285a03257049cd62d
decryptText : true, test text!!!
```

<!--
이렇게 암호화된 값을 properties에 사용하면 됩니다.
-->

You can use these encrypted values in properties.

```properties
someValue={text}07d9e2cf09288e43b8d706a2bdcfcee95490fb91f022056fc6d523ff3fc1d3f50aa4c1ae078761c285a03257049cd62d
```
<!--
각 properties의 암호화된 value는 그대로 유지되며 복호화된 값은 별도의 properties로 저장되어 우선 호출되어 사용됩니다.

actuator를 사용한 경우 `/actuator/env` 에서 암호화 값의 처리를 확인할 수 있습니다.
-->

The encrypted value of each property remains intact, and the decrypted value is stored as a separate property and is called and used first.

If you used actuator, you can see the handling of encrypted values in `/actuator/env`.

<!--

Spring Boot application이 시작될때 `DecryptEnvironmentPostProcessor` 가 실행되는 시점(prepareEnvironment 단계)에 각 개별 프로젝트가 `@PropertySource` 로 지정한 properties는 로드되지 않습니다.
(prepareContext 단계에 로드되며 prepareEnvironment 단계 이후입니다.)

따라서 `@PropertySource` 로 호출된 properties 에서 암호화를 사용하는 경우 복호화를 위해 bluesky-boot-crypto 가 제공하는 `DecryptPropertySourceFactory` 를 지정해야 합니다.
-->


```java
@Configuration
@PropertySource(value = "classpath:data-mongodb-webcontents-${net-profile}.properties", factory = DecryptPropertySourceFactory.class)
public class SomeConfiguration {

}
```

## version history

| version | prerequisites |
| ------------- | ------------- |
| 3.1.0 | Java 17, Spring Boot 3.1.0 |
| 3.4.0 | Java 17, Spring Boot 3.4.0 |
| 4.1.0 | Java 25, Spring Boot 4.1 |