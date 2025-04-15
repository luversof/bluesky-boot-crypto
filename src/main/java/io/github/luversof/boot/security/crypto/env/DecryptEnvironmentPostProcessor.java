package io.github.luversof.boot.security.crypto.env;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import io.github.luversof.boot.security.crypto.encrypt.DelegatingTextEncryptor;
import io.github.luversof.boot.security.crypto.factory.TextEncryptorFactories;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DecryptEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
	
	/**
	 * Name of the decrypted property source.
	 */
	public static final String DECRYPTED_PROPERTY_SOURCE_NAME = "blueskyDecrypted";
	
	private static final Pattern COLLECTION_PROPERTY = Pattern.compile("(\\S+)?\\[(\\d+)\\](\\.\\S+)?");	// NOSONAR java:S5852 분리하여 패턴 개선을 할 수 있으나 Spring Cloud의 코드를 그대로 사용한 부분이기 때문에 수정하지 않음

	private int order = Ordered.LOWEST_PRECEDENCE;
	
	private DelegatingTextEncryptor textEncryptor;
	
	public DecryptEnvironmentPostProcessor() {
		textEncryptor = TextEncryptorFactories.getDelegatingTextEncryptor();
	}

	@Override
	public int getOrder() {
		return this.order;
	}
	
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		
		if (textEncryptor == null || textEncryptor.isTextEncryptorMapEmpty()) {
			return;
		}
		
		var propertySources = environment.getPropertySources();
		environment.getPropertySources().remove(DECRYPTED_PROPERTY_SOURCE_NAME);
		
		var properties = merge(propertySources);
		decrypt(properties);
		
		if (!properties.isEmpty()) {
			propertySources.addFirst(new SystemEnvironmentPropertySource(DECRYPTED_PROPERTY_SOURCE_NAME, properties));
		}
	}
	
	protected Map<String, Object> merge(PropertySources propertySources) {
		var properties = new LinkedHashMap<String, Object>();
		var sources = new ArrayList<PropertySource<?>>();
		for (var source : propertySources) {
			sources.add(0, source);
		}
		for (var source : sources) {
			merge(source, properties);
		}
		return properties;
	}
	
	protected void merge(PropertySource<?> source, Map<String, Object> properties) {
		if (source instanceof CompositePropertySource compositePropertySource) {
			mergeCompositePropertySource(compositePropertySource, properties);
		}
		else if (source instanceof EnumerablePropertySource<?> enumerablePropertySource) {
			mergeEnumerablePropertySource(enumerablePropertySource, properties);
		}
	}
	
	private void mergeCompositePropertySource(CompositePropertySource source, Map<String, Object> properties) {
		var sources = new ArrayList<PropertySource<?>>(source.getPropertySources());
		for (var nested : sources.reversed()) {
			merge(nested, properties);
		}
	}
	
	private void mergeEnumerablePropertySource(EnumerablePropertySource<?> source, Map<String, Object> properties) {
		var otherCollectionProperties = new LinkedHashMap<String, Object>();
		boolean sourceHasDecryptedCollection = false;

		for (String key : source.getPropertyNames()) {
			var property = source.getProperty(key);
			if (property != null) {
				String value = property.toString();
				
				if (textEncryptor.isEncrypted(value)) {
					properties.put(key, value);
					if (COLLECTION_PROPERTY.matcher(key).matches()) {
						sourceHasDecryptedCollection = true;
					}
				}
				else if (COLLECTION_PROPERTY.matcher(key).matches()) {
					// put non-encrypted properties so merging of index properties
					// happens correctly
					otherCollectionProperties.put(key, value);
				}
				else {
					// override previously encrypted with non-encrypted property
					properties.remove(key);
				}
			}
		}
		// copy all indexed properties even if not encrypted
		if (sourceHasDecryptedCollection && !otherCollectionProperties.isEmpty()) {
			properties.putAll(otherCollectionProperties);
		}
	}
	
	protected void decrypt(Map<String, Object> properties) {
		properties.replaceAll((key, value) -> {
			String valueString = value.toString();
			if (!textEncryptor.isEncrypted(valueString)) {
				return value;
			}
			return decrypt(key, valueString);
		});
	}
	
	protected String decrypt(String key, String original) {
		String value = original;
		try {
			value = textEncryptor.decrypt(value);
			log.debug("Decrypted: key=" + key);
			return value;
		}
		catch (Exception e) {
			String message = "Cannot decrypt: key=" + key;
			log.warn(message, e);
			throw new IllegalStateException(message, e);
		}
	}

}
