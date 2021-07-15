package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties("jwt")
@Data
public class JwtProperties {
    private String pubFilePath;
    private String cookieName;
    private String userKey;
    private Integer expire;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
           this.publicKey = RsaUtils.getPublicKey(pubFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

