package com.tgc.sky.io;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import javax.security.auth.x500.X500Principal;

/* renamed from: com.tgc.sky.io.DeviceKey */
public class DeviceKey {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final BigInteger CURVE_A = new BigInteger("3", 10);
    private static final BigInteger CURVE_B = new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);
    private static final BigInteger MODULUS = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private static final String kDeviceKeyAlias = "com.tgc.sky.devicekey";

    public static boolean Delete() {
        DeleteKeyPair();
        return true;
    }

    public static String GetPublicKeyAsBase64() {
        KeyPair GetKeyPair = GetKeyPair();
        if (GetKeyPair != null) {
            return GetPublicKeyAsBase64(GetKeyPair.getPublic());
        }
        return null;
    }

    public static String Sign(String str) {
        KeyPair GetKeyPair = GetKeyPair();
        if (GetKeyPair == null) {
            return null;
        }
        try {
            Signature instance = Signature.getInstance("SHA256withECDSA");
            instance.initSign(GetKeyPair.getPrivate(), new SecureRandom());
            instance.update(str.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(instance.sign(), 2);
        } catch (NullPointerException | InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean VerifySignature(String str, String str2) {
        KeyPair GetKeyPair = GetKeyPair();
        if (GetKeyPair == null) {
            return false;
        }
        byte[] decode = Base64.decode(str2.getBytes(), 2);
        try {
            Signature instance = Signature.getInstance("SHA256withECDSA");
            instance.initVerify(GetKeyPair.getPublic());
            instance.update(str.getBytes(StandardCharsets.UTF_8));
            return instance.verify(decode);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean VerifyWithPublicKeyAndSignature(String str, String str2, String str3) {
        PublicKey GetPublicKeyFromBase64 = GetPublicKeyFromBase64(str);
        byte[] decode = Base64.decode(str3.getBytes(), 2);
        try {
            Signature instance = Signature.getInstance("SHA256withECDSA");
            instance.initVerify(GetPublicKeyFromBase64);
            instance.update(str2.getBytes(StandardCharsets.UTF_8));
            return instance.verify(decode);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static KeyPair GetKeyPair() {
        PrivateKey GetPrivateKey = GetPrivateKey();
        PublicKey GetPublicKey = GetPublicKey();
        if (GetPrivateKey != null && GetPublicKey != null) {
            return new KeyPair(GetPublicKey, GetPrivateKey);
        }
        DeleteKeyPair();
        return CreateKeyPair();
    }

    private static KeyPair CreateKeyPair() {
        try {
            KeyPairGenerator instance = KeyPairGenerator.getInstance("EC", ANDROID_KEY_STORE);
            long currentTimeMillis = System.currentTimeMillis();
            instance.initialize(new KeyGenParameterSpec.Builder(kDeviceKeyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_SIGN).setRandomizedEncryptionRequired(false).setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1")).setDigests(new String[]{"SHA-256", "SHA-512"}).setKeySize(256).setSignaturePaddings(new String[]{"PKCS1"}).setCertificateSubject(new X500Principal("CN=Android, O=Android Authority")).setCertificateSerialNumber(new BigInteger(256, new Random())).setCertificateNotBefore(new Date(currentTimeMillis - (currentTimeMillis % 1000))).setCertificateNotAfter(new Date(new Date(currentTimeMillis - (currentTimeMillis % 1000)).getTime() + 3155673600000L)).build());
            return instance.generateKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void DeleteKeyPair() {
        try {
            KeyStore instance = KeyStore.getInstance(ANDROID_KEY_STORE);
            instance.load((KeyStore.LoadStoreParameter) null);
            instance.deleteEntry(kDeviceKeyAlias);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    }

    private static PrivateKey GetPrivateKey() {
        try {
            KeyStore instance = KeyStore.getInstance(ANDROID_KEY_STORE);
            instance.load((KeyStore.LoadStoreParameter) null);
            if (Build.VERSION.SDK_INT >= 28) {
                return (PrivateKey) instance.getKey(kDeviceKeyAlias, (char[]) null);
            }
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) instance.getEntry(kDeviceKeyAlias, (KeyStore.ProtectionParameter) null);
            if (privateKeyEntry != null) {
                return privateKeyEntry.getPrivateKey();
            }
            return null;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PublicKey GetPublicKey() {
        Certificate certificate;
        try {
            KeyStore instance = KeyStore.getInstance(ANDROID_KEY_STORE);
            instance.load((KeyStore.LoadStoreParameter) null);
            if (Build.VERSION.SDK_INT < 28) {
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) instance.getEntry(kDeviceKeyAlias, (KeyStore.ProtectionParameter) null);
                if (privateKeyEntry != null) {
                    return privateKeyEntry.getCertificate().getPublicKey();
                }
                return null;
            } else if (!instance.containsAlias(kDeviceKeyAlias) || (certificate = instance.getCertificate(kDeviceKeyAlias)) == null) {
                return null;
            } else {
                return certificate.getPublicKey();
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String GetPublicKeyAsBase64(PublicKey publicKey) {
        byte[] bArr = new byte[33];
        ECPublicKey eCPublicKey = (ECPublicKey) publicKey;
        byte[] byteArray = eCPublicKey.getW().getAffineX().toByteArray();
        byte[] byteArray2 = eCPublicKey.getW().getAffineY().toByteArray();
        if ((byteArray2[byteArray2.length - 1] & 1) == 0) {
            bArr[0] = 2;
        } else {
            bArr[0] = 3;
        }
        if (byteArray.length >= 32) {
            System.arraycopy(byteArray, byteArray.length - 32, bArr, 1, 32);
        } else {
            System.arraycopy(byteArray, 0, bArr, (32 - byteArray.length) + 1, byteArray.length);
        }
        return Base64.encodeToString(bArr, 2);
    }

    private static PublicKey GetPublicKeyFromBase64(String str) {
        byte[] decode = Base64.decode(str.getBytes(), 2);
        boolean z = false;
        byte[] copyOfRange = Arrays.copyOfRange(decode, 0, decode.length);
        copyOfRange[0] = 0;
        BigInteger bigInteger = new BigInteger(1, copyOfRange);
        BigInteger sqrtMod = sqrtMod(bigInteger.pow(2).subtract(CURVE_A).multiply(bigInteger).add(CURVE_B));
        boolean testBit = sqrtMod.testBit(0);
        if (decode[0] == 3) {
            z = true;
        }
        if (testBit != z) {
            sqrtMod = sqrtMod.negate().mod(MODULUS);
        }
        try {
            ECPoint eCPoint = new ECPoint(bigInteger, sqrtMod);
            AlgorithmParameters instance = AlgorithmParameters.getInstance("EC");
            instance.init(new ECGenParameterSpec("secp256r1"));
            return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(eCPoint, (ECParameterSpec) instance.getParameterSpec(ECParameterSpec.class)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidParameterSpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BigInteger sqrtMod(BigInteger bigInteger) {
        return bigInteger.modPow(MODULUS.add(BigInteger.ONE).shiftRight(2), MODULUS);
    }
}
