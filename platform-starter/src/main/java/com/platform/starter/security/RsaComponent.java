package com.platform.starter.security;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * RSA 非对称加密组件
 *
 * <p>构造时生成 RSA 2048 密钥对，私钥仅驻留内存（不落盘、不进仓库、不打日志），
 * 公钥通过 {@link #getPublicKeyBase64()} 对外提供，供前端加密登录密码 / 用户名。</p>
 *
 * <p>用途：在网关已终结 TLS 的前提下，作为应用层纵深防御 / 合规兜底，
 * 避免凭据以明文出现在网络链路与后端日志中。</p>
 *
 * <p>加解密算法固定为 {@code RSA/ECB/PKCS1Padding}，前端须使用一致算法
 * （如 jsencrypt 默认 RSAES-PKCS1-V1_5）加密，否则解密失败。</p>
 *
 * @author platform
 */
@Slf4j
public class RsaComponent {

    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    @Getter
    private final String publicKeyBase64;
    private final PrivateKey privateKey;

    public RsaComponent() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(KEY_SIZE, new SecureRandom());
            KeyPair pair = generator.generateKeyPair();
            this.privateKey = pair.getPrivate();
            PublicKey pub = pair.getPublic();
            this.publicKeyBase64 = Base64.getEncoder().encodeToString(pub.getEncoded());
            log.info("[RSA] 密钥对生成成功，密钥长度 {} 位（私钥仅驻留内存）", KEY_SIZE);
        } catch (Exception e) {
            log.error("[RSA] 密钥对生成失败", e);
            throw new IllegalStateException("RSA 密钥对生成失败", e);
        }
    }

    /**
     * 用私钥解密前端用公钥加密的密文（Base64 编码）
     *
     * @param base64Cipher 前端加密后的 Base64 密文
     * @return 解密后的明文
     */
    public String decryptByPrivateKey(String base64Cipher) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decoded = Base64.getDecoder().decode(base64Cipher);
            byte[] plain = cipher.doFinal(decoded);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[RSA] 解密失败", e);
            throw new IllegalStateException("RSA 解密失败", e);
        }
    }
}
