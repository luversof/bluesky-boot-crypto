package io.github.luversof.boot.security.crypto.support;

import java.io.IOException;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.StringUtils;

import io.github.luversof.boot.security.crypto.encrypt.DelegatingTextEncryptor;
import io.github.luversof.boot.security.crypto.factory.TextEncryptorFactories;

public class DecryptPropertySourceFactory implements PropertySourceFactory {

	private DelegatingTextEncryptor textEncryptor = TextEncryptorFactories.getDelegatingTextEncryptor();

	@Override
	public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
		var properties = PropertiesLoaderUtils.loadProperties(resource);
		properties.replaceAll((key, value) -> {
			String valueString = value.toString();
			if (!textEncryptor.isEncrypted(valueString)) {
				return value;
			}
			return textEncryptor.decrypt(valueString);
		});
		
		String targetName = name;
		
		if (targetName == null) {
			targetName = getNameForResource(resource.getResource());
		}
		
		return new PropertiesPropertySource(targetName, properties);
	}
	
	private static String getNameForResource(Resource resource) {
		String name = resource.getDescription();
		if (!StringUtils.hasText(name)) {
			name = resource.getClass().getSimpleName() + "@" + System.identityHashCode(resource);
		}
		return name;
	}

}