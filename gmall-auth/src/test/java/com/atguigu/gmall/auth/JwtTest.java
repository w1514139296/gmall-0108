package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
    private static final String pubKeyPath = "D:\\1130SGG\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\1130SGG\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "123456");
    }


    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "gouge");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MjYyNDI3ODh9.dK-gCnQE9ugr3p7_U6OISk4SN2Uhc4IF7vvgaM2BlUqXBEJJj7d1A36BcjzhGfxABBis-wEGZFaO76ERjTEU_LECMemBeXKOmfYXfnUmsJ0tp89Hz-qnCBp6r5SHjFN2TPmEa0fsJZTn6QKj9oJNOqv2x94MVYtzeN1FvB9R75AcjX4k92V-cEdwVLXSaqYeVrHWC_pKyS0FOW6Z5s5VYassZEWO_Dkk2URajgILeLvQ_NQ63GjI3UZW0wlUBQFEMzkRmQ2zEkwjFc3OekFywBYXzJMZbprhiuFuOM_SXjdPVrH_F0eienKc2xGTOmn7iMiNsdWmAl8UcLH-JZ2cEw";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }


}