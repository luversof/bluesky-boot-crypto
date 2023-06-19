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

- [Java 17](https://openjdk.java.net/)
- [Spring Boot 3.1.0](https://spring.io/)

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
        <version>3.1.0-SNAPSHOT</version> 
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
	textEncryptorMap.put("text", Encryptors.text("pass", "8560b4f4b3"));
	textEncryptorMap.put("delux", Encryptors.delux("pass", "8560b4f4b3"));
	return textEncryptorMap;
}
```

<!--
사용하려는 `TextEncryptor` 가 있다면 application 시작 전에 해당 `TextEncryptor` 를 `TextEncryptorFactories` 에 추가하면 됩니다.

여러 개의 `TextEncrpytor` 를 추가할 수 있으며 하나만 추가한 경우 해당 `TextEncrptor` 가 기본 textEncryptor가 되며 여러 개를 추가하는 경우 그 중 하나를 default `TextEncryptor` 로 지정하면 됩니다.
-->

If you have an `TextEncryptor` that you want to use, you can add it to the `TextEncryptorFactories` before starting your application.

You can add multiple TextEncrpytors, and if you only add one, that `TextEncrptor` will be the default textEncryptor; if you add multiple, you can specify one of them as the default `TextEncryptor` .


```java
@SpringBootApplication
public class Application {
	public static void main(String[] args) throws Throwable {
		TextEncryptorFactories.createDelegatingTextEncryptor("encryptorId", encryptor);
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
encryptText : {text}98300b76125b1badd91745b15ef542c385a0df80837410b3c854df34a93ff351
decryptText : true, test text!!!
```

<!--
이렇게 암호화된 값을 properties에 사용하면 됩니다.
-->

You can use these encrypted values in properties.

```properties
someValue={text}98300b76125b1badd91745b15ef542c385a0df80837410b3c854df34a93ff351
```

## version history

| version | prerequisites |
| ------------- | ------------- |
| 3.1.0 | Java 17, Spring Boot 3.1.0 |