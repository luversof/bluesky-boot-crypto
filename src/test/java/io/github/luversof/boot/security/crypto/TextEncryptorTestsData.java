package io.github.luversof.boot.security.crypto;

public enum TextEncryptorTestsData {

	T1("test", ""),
	;
	
	private String text;
	private String encryptedText;
	
	private TextEncryptorTestsData(String text, String encryptedText) {
		this.text = text;
		this.encryptedText = encryptedText;
	}
	
	public String getText() {
		return text;
	}
	public String getEncryptedText() {
		return encryptedText;
	}
	
	
}
