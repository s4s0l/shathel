import org.s4s0l.shathel.commons.core.security.SafeStorageImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.security.AlgorithmParameters
import java.security.Permission
import java.security.PermissionCollection
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.logging.Level

/**
 * @author Marcin Wielgus
 */
class CryptoSpec extends Specification {
    private static final Logger logger = LoggerFactory.getLogger(CryptoSpec.class);

    private static void removeCryptographyRestrictions() {
        if (!isRestrictedCryptography()) {
            logger.info("Cryptography restrictions removal not needed");
            return;
        }
        try {
            /*
             * Do the following, but with reflection to bypass access checks:
             *
             * JceSecurity.isRestricted = false;
             * JceSecurity.defaultPolicy.perms.clear();
             * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
             */
            final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
            final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
            final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

            final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
            isRestrictedField.setAccessible(true);
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(isRestrictedField, isRestrictedField.getModifiers() & ~Modifier.FINAL);
            isRestrictedField.set(null, false);

            final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
            defaultPolicyField.setAccessible(true);
            final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

            final Field perms = cryptoPermissions.getDeclaredField("perms");
            perms.setAccessible(true);
            ((Map<?, ?>) perms.get(defaultPolicy)).clear();

            final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            defaultPolicy.add((Permission) instance.get(null));

            logger.info("Successfully removed cryptography restrictions");
        } catch (final Exception e) {
            logger.info(Level.WARNING, "Failed to remove cryptography restrictions", e);
        }
    }

    static void xxx() {
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (Exception ex) {
        }
    }

    private static boolean isRestrictedCryptography() {
        // This simply matches the Oracle JRE, but not OpenJDK.
        return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
    }

    def "SODD"() {
        given:
        def password = "12334567890qwerty"
        def salt = (1..16) as byte[]
        new SecureRandom([1, 2, 3, 4, 5, 6] as byte[]).nextBytes(salt)

        def iv = (1..16) as byte[]
        new SecureRandom([1, 2, 3, 4, 5, 6] as byte[]).nextBytes(iv)

        println salt
        removeCryptographyRestrictions()
        SafeStorageImpl.class.getName()
        xxx()

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.getChars(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES")


        when:
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal("Hello, World!".getBytes("UTF-8"));




        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");

        then:
//        CipherOutputStream!!!
        plaintext == "Hello, World!"

    }
}
