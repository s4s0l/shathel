package org.s4s0l.shathel.commons.core.security;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.slf4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * TODO split it to CipherProvider and IO facade
 *
 * @author Matcin Wielgus
 */
public class SafeStorageImpl implements SafeStorage {
    private static final Logger LOGGER = getLogger(SafeStorageImpl.class);

    static {
        if (!"Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"))) {
            LOGGER.debug("Cryptography restrictions removal not needed");
        } else {
            try {
                Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
                field.setAccessible(true);
                field.set(null, java.lang.Boolean.FALSE);
            } catch (Exception ex) {
                LOGGER.warn("Unable to remove Cryptography restrictions", ex);
            }
        }
    }

//    private static final String CRYPTO_TYPE = "AES/GCM/NoPadding";
        private static final String CRYPTO_TYPE = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 256; // in bits
    private static final int GCM_TAG_LENGTH = 16; // in bytes
    private static final byte[] DEFAULT_SALT = new byte[]{124, -21, -54, -120, 56, 27, -2, -67, -8, 121, -113, -21, -72, -53, 125, 124};
    //    private static final byte[] IV = new byte[]{-80, -93, -12, 116, 53, -9, 118, 77, 24, 0, 66, -76, -70, 91, 38, -82};
    private final File rootDir;
    private final SecretKey secret;
    private final byte[] iv;


    public SafeStorageImpl(File rootDir, char[] masterPassword) {
        this(rootDir, masterPassword, DEFAULT_SALT);
    }

    public AlgorithmParameterSpec getParameter() {
        return new IvParameterSpec(iv);
//
//
//        return new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
    }


    public SafeStorageImpl(File rootDir, char[] masterPassword, byte[] salt) {
        this.rootDir = rootDir;
        rootDir.mkdirs();
        new File(rootDir, "files").mkdirs();
        new File(rootDir, "values").mkdirs();
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(masterPassword, salt, 65536, AES_KEY_SIZE);
            SecretKey tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            clean(masterPassword);

            File ivFile = new File(rootDir, "/.iv");
            if (ivFile.exists()) {
                iv = ResourceGroovyMethods.getBytes(ivFile);
            } else {
                iv = new byte[16];
                new SecureRandom(DEFAULT_SALT).nextBytes(iv);
                ResourceGroovyMethods.setBytes(ivFile, iv);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create Safe Storage", e);
        }
    }

    private Cipher getEncryptCipher() {
        try {
            Cipher cipher = Cipher.getInstance(CRYPTO_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, secret, getParameter());
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create cipher", e);
        }
    }


    private Cipher getDecriptionCipher() {
        try {
            Cipher cipher = Cipher.getInstance(CRYPTO_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, secret, getParameter());
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create cipher", e);
        }
    }

    private static void clean(char[] masterPassword) {
        for (int i = 0; i < masterPassword.length; i++) {
            masterPassword[i] = ' ';
        }
    }


    public Optional<InputStream> inputStream(String key) {
        File f = new File(rootDir, key);
        return Optional.ofNullable(f)
                .filter(File::exists)
                .map(file -> {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(file -> new BufferedInputStream(file))
                .map(file -> new CipherInputStream(file, getDecriptionCipher()));
    }

    public OutputStream outputStream(String key) {
        File f = new File(rootDir, key);
        if (f.exists()) {
            f.delete();
        }
        try {
            return new CipherOutputStream(new FileOutputStream(f), getEncryptCipher());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readFile(String key, File copyTo) {
        inputStream("files/" + key).map(x -> {
            try {
                try (FileOutputStream fos = new FileOutputStream(copyTo)) {
                    IOUtils.copy(x, fos);
                    return true;
                } finally {
                    x.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).orElseThrow(() -> new RuntimeException("No key in storage " + key));
    }


    @Override
    public void writeFile(String key, File f) {
        try {
            try (InputStream fos = new FileInputStream(f);
                 OutputStream xsas = outputStream("files/" + key)) {
                IOUtils.copy(fos, xsas);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> readValue(String key) {
        return inputStream("values/" + key).map(x -> {
            try {
                return IOUtils.toString(x, "UTF8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(x);
            }
        });
    }

    @Override
    public void writeValue(String key, String value) {
        try {
            try (OutputStream x = outputStream("values/" + key)) {
                x.write(value.getBytes("UTF8"));
                x.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
