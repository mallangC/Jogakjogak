package com.zb.jogakjogak.security.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class EmailEncryptor implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {

        try{
            String key = EncryptionKeyHolder.emailKey;
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e){
            throw new RuntimeException("이메일 암호화 실패", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try{
            String key = EncryptionKeyHolder.emailKey;
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e){
            throw new RuntimeException("이메일 복호화 실패", e);
        }
    }
}
