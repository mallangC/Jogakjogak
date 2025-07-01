package com.zb.jogakjogak.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionKeyHolder {
    public static String emailKey;

    @Value("${security.encrypt.email-key}")
    public void setEmailKey(String key){
        emailKey = key;
    }
}
