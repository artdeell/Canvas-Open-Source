package com.tgc.sky.io;

import android.content.Context;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.math.BigInteger;
import java.util.Arrays;

public class DeviceKey {
    private static final BigInteger CURVE_A = new BigInteger("3", 10);
    private static final BigInteger CURVE_B = new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);
    private static final BigInteger MODULUS = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private static final String PRIVATE_KEY_FILE = "device_private.key";
    private static final String PUBLIC_KEY_FILE = "device_public.key";
    private static Context sContext;

    public static void setContext(Context context) {
        sContext = context;
        git.artdeell.skymodloader.AccountStorage.sync(context);
    }

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
        if (sContext == null) {
            return CreateKeyPairLegacy();
        }
        
        try {
            File filesDir = sContext.getFilesDir();
            File privateKeyFile = new File(filesDir, PRIVATE_KEY_FILE);
            File publicKeyFile = new File(filesDir, PUBLIC_KEY_FILE);
            
            if (privateKeyFile.exists() && publicKeyFile.exists()) {
                FileInputStream privateFis = new FileInputStream(privateKeyFile);
                byte[] privateKeyBytes = new byte[(int) privateKeyFile.length()];
                privateFis.read(privateKeyBytes);
                privateFis.close();
                
                FileInputStream publicFis = new FileInputStream(publicKeyFile);
                byte[] publicKeyBytes = new byte[(int) publicKeyFile.length()];
                publicFis.read(publicKeyBytes);
                publicFis.close();
                
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKeyBytes);
                
                PrivateKey privateKey = keyFactory.generatePrivate(privSpec);
                PublicKey publicKey = keyFactory.generatePublic(pubSpec);
                
                return new KeyPair(publicKey, privateKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DeleteKeyPair();
        }
        
        return CreateKeyPair();
    }

    private static KeyPair CreateKeyPair() {
        if (sContext == null) {
            return CreateKeyPairLegacy();
        }
        
        try {
            KeyPairGenerator instance = KeyPairGenerator.getInstance("EC");
            instance.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair kp = instance.generateKeyPair();
            
            File filesDir = sContext.getFilesDir();
            
            FileOutputStream privateFos = new FileOutputStream(new File(filesDir, PRIVATE_KEY_FILE));
            privateFos.write(kp.getPrivate().getEncoded());
            privateFos.close();
            
            FileOutputStream publicFos = new FileOutputStream(new File(filesDir, PUBLIC_KEY_FILE));
            publicFos.write(kp.getPublic().getEncoded());
            publicFos.close();
            
            git.artdeell.skymodloader.AccountStorage.saveActive(sContext);
            return kp;
        } catch (Exception e) {
            e.printStackTrace();
            return CreateKeyPairLegacy();
        }
    }

    private static KeyPair CreateKeyPairLegacy() {
        try {
            KeyPairGenerator instance = KeyPairGenerator.getInstance("EC");
            instance.initialize(new ECGenParameterSpec("secp256r1"));
            return instance.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void DeleteKeyPair() {
        try {
            if (sContext != null) {
                File filesDir = sContext.getFilesDir();
                File privateKeyFile = new File(filesDir, PRIVATE_KEY_FILE);
                File publicKeyFile = new File(filesDir, PUBLIC_KEY_FILE);
                if (privateKeyFile.exists()) {
                    privateKeyFile.delete();
                }
                if (publicKeyFile.exists()) {
                    publicKeyFile.delete();
                }
                git.artdeell.skymodloader.AccountStorage.saveActive(sContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String GetPublicKeyAsBase64(PublicKey publicKey) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BigInteger sqrtMod(BigInteger bigInteger) {
        return bigInteger.modPow(MODULUS.add(BigInteger.ONE).shiftRight(2), MODULUS);
    }

    public static KeyPair CreateKeyPairInDir(File dir) {
        try {
            KeyPairGenerator instance = KeyPairGenerator.getInstance("EC");
            instance.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair kp = instance.generateKeyPair();
            if (dir != null) {
                if (!dir.exists()) dir.mkdirs();
                FileOutputStream privateFos = new FileOutputStream(new File(dir, PRIVATE_KEY_FILE));
                privateFos.write(kp.getPrivate().getEncoded());
                privateFos.close();
                FileOutputStream publicFos = new FileOutputStream(new File(dir, PUBLIC_KEY_FILE));
                publicFos.write(kp.getPublic().getEncoded());
                publicFos.close();
            }
            return kp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String GetPublicKeyFromDir(File dir) {
        if (dir == null) return null;
        File publicKeyFile = new File(dir, PUBLIC_KEY_FILE);
        if (!publicKeyFile.exists()) return null;
        try (FileInputStream fis = new FileInputStream(publicKeyFile)) {
            byte[] bytes = new byte[(int) publicKeyFile.length()];
            fis.read(bytes);
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(pubSpec);
            return GetPublicKeyAsBase64(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String GetMaskedKey(String base64) {
        if (base64 == null || base64.length() < 10) return "N/A";
        return base64.substring(0, 6) + "..." + base64.substring(base64.length() - 4);
    }
}
