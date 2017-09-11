package org.s4s0l.shathel.commons.core.security;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.slf4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * TODO split it to CipherProvider and IO facade
 *
 * @author Marcin Wielgus
 */
public class SafeStorageImpl implements SafeStorage {
    private static final Logger LOGGER = getLogger(SafeStorageImpl.class);

    static {
        String RUNTIME = System.getProperty("java.runtime.name");
        int maxKeyLen = 0;
        try {
            maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        } catch (Exception e) {
            throw new RuntimeException("wtf??", e);
        }
        if (maxKeyLen > 512 ||
                (!"Java(TM) SE Runtime Environment".equals(RUNTIME) && !"OpenJDK Runtime Environment".equals(RUNTIME))) {
            LOGGER.debug("Cryptography restrictions removal not needed");
        } else {
            try {
                try {
                    Class jceSecurity = Class.forName("javax.crypto.JceSecurity");
                    Field isRestricted = jceSecurity.getDeclaredField("isRestricted");
                    isRestricted.setAccessible(true);
                    isRestricted.set(null, false);
                    isRestricted.setAccessible(false);
                } catch (ClassNotFoundException e) { // not an OpenJDK ~ JVM
                    LOGGER.debug("unable to enable unlimited-strength crypto " + e);
                } catch (Exception e) {
                    Class jceSecurity = Class.forName("javax.crypto.JceSecurity");
                    Field isRestricted = jceSecurity.getDeclaredField("isRestricted");
                    isRestricted.setAccessible(true);
                    if (Boolean.TRUE.equals(isRestricted.get(null))) {
                        if (Modifier.isFinal(isRestricted.getModifiers())) {
                            Field modifiers = Field.class.getDeclaredField("modifiers");
                            modifiers.setAccessible(true);
                            modifiers.setInt(isRestricted, isRestricted.getModifiers() & ~Modifier.FINAL);
                        }
                        Field isRestricted2 = jceSecurity.getDeclaredField("isRestricted");
                        isRestricted2.setAccessible(true);
                        isRestricted2.setAccessible(true);
                        isRestricted2.setBoolean(null, false); // isRestricted = false;
                        isRestricted2.setAccessible(false);
                    }
                }
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
                try {
                    Optional<String> s = readValue("password-verification");
                    if (!s.isPresent()) {
                        writeValue("password-verification", "ok");
                    } else {
                        if (!s.get().equals("ok")) {
                            throw new RuntimeException("Seems like password has changed");
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("provided safe password seems invalid...", e);
                }
            } else {
                iv = new byte[16];
                new SecureRandom(DEFAULT_SALT).nextBytes(iv);
                ResourceGroovyMethods.setBytes(ivFile, iv);
                writeValue("password-verification", "ok");
            }


        } catch (Exception e) {
            throw new RuntimeException("Unable to create Safe Storage: " + e.getMessage(), e);
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

    private byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    @Override
    public String crypt(char[] value) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, getEncryptCipher());
            cipherOutputStream.write(toBytes(value));
            cipherOutputStream.flush();
            cipherOutputStream.close();
            byte[] src = byteArrayOutputStream.toByteArray();
            return "{enc}" + java.util.Base64.getEncoder().encodeToString(src);
        } catch (Exception e) {
            throw new RuntimeException("Unable to crypt value", e);
        }
    }

    @Override
    public String decrypt(String value) {
        try {
            if (isCrypted(value)) {
                byte[] decoded = java.util.Base64.getDecoder().decode(value.substring(5));
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decoded);
                CipherInputStream cipherInputStream = new CipherInputStream(byteArrayInputStream, getDecriptionCipher());
                byte[] decrypted = IOUtils.toByteArray(cipherInputStream);
                return new String(decrypted, "utf8");
            } else {
                return value;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to decrypt value", e);
        }
    }


}
