package com.zb.jogakjogak.security.config;

import com.nimbusds.oauth2.sdk.auth.Secret;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class PhoneNumberEncryptor implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try{
            String key = EncryptionKeyHolder.phoneKey;
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e){
            throw new RuntimeException("전화번호 암호화 실패", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try{
            String key = EncryptionKeyHolder.phoneKey;
            Cipher cipher= Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e){
            throw new RuntimeException("전화번호 복호화 실패", e);
        }
    }
}
