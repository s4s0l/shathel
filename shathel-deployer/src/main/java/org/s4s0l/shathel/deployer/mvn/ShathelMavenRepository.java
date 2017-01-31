package org.s4s0l.shathel.deployer.mvn;

/**
 * @author Matcin Wielgus
 */

import lombok.Builder;
import lombok.Data;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.crypto.DefaultSettingsDecrypter;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.*;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.ant.Names;
import org.eclipse.aether.internal.ant.types.Authentication;
import org.eclipse.aether.internal.ant.types.Mirror;
import org.eclipse.aether.internal.ant.types.Proxy;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.classpath.ClasspathTransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.*;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Heavily based on:
 * http://git.eclipse.org/c/aether/aether-ant.git/plain/src/main/java/org/eclipse/aether/internal/ant/AntRepoSys.java
 */
public class ShathelMavenRepository {
    @Builder
    @Data
    public static class ShathelMavenSettings {

        private String m2SecuritySetting;

        private String m2Settings;

        private String localRepo;

        private RemoteRepository repository;


        public static class ShathelMavenSettingsBuilder {

            private String m2SecuritySetting = getDefaultM2SecuritySetting();

            private String m2Settings = getDefaultUserSettingsFile();

            private String localRepo = getDefaultLocalRepo();

            private RemoteRepository repository = new RemoteRepository.Builder("central", "default",
                    getMavenCentrajUrl()).build();

            public static String getMavenCentrajUrl() {
                return "http://repo1.maven.org/maven2/";
            }

            public static String getDefaultM2SecuritySetting() {
                return "~/.m2/settings-security.xml";
            }

            public static String getDefaultUserSettingsFile() {
                return new File(new File(new File(System.getProperty("user.home")), ".m2"), Names.SETTINGS_XML).getAbsolutePath();
            }
            public static String getDefaultLocalRepo() {
                return new File(new File(System.getProperty("user.home")), ".m2/repository").getAbsolutePath();
            }
        }


        public void setRepository(String id, String repositoryUrl) {
            this.repository = new RemoteRepository.Builder(id, "default",
                    repositoryUrl).build();
        }

        public File getUserSettingsFile() {
            return new File(m2Settings);
        }

        public File getGlobalSettingsFile() {
            String mavenHome = getMavenHome();
            if (mavenHome != null) {
                return new File(new File(mavenHome, "conf"), Names.SETTINGS_XML);
            }
            return null;
        }

        public static String getMavenHome() {
            return System.getenv("M2_HOME");
        }

        private Properties getSystemProperties() {
            Properties props = new Properties();
            getEnvProperties(props);
            props.putAll(System.getProperties());
            return props;
        }

        private Properties getEnvProperties(Properties props) {
            if (props == null) {
                props = new Properties();
            }
            boolean envCaseInsensitive = OS_WINDOWS;
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                String key = entry.getKey();
                if (envCaseInsensitive) {
                    key = key.toUpperCase(Locale.ENGLISH);
                }
                key = "env." + key;
                props.put(key, entry.getValue());
            }
            return props;
        }
    }

    static class AntSecDispatcher extends DefaultSecDispatcher {

        public AntSecDispatcher(ShathelMavenSettings s) {
            _configurationFile = s.getM2SecuritySetting();
            try {
                _cipher = new DefaultPlexusCipher();
            } catch (PlexusCipherException e) {
                e.printStackTrace();
            }
        }

    }

    static DefaultSettingsDecrypter createDecryptorFactorynewInstance(ShathelMavenSettings s) {
        AntSecDispatcher secDispatcher = new AntSecDispatcher(s);
        DefaultSettingsDecrypter decrypter = new DefaultSettingsDecrypter();
        try {
            Field field = decrypter.getClass().getDeclaredField("securityDispatcher");
            field.setAccessible(true);
            field.set(decrypter, secDispatcher);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return decrypter;
    }

    private static boolean OS_WINDOWS = Os.isFamily("windows");

    private final SettingsDecrypter settingsDecrypter;

    private final DefaultServiceLocator locator;

    private final Settings settings;

    private final ShathelMavenSettings shathelMavenSettings;

    private final List<Mirror> mirrors = new CopyOnWriteArrayList<Mirror>();

    private final List<Proxy> proxies = new CopyOnWriteArrayList<Proxy>();

    private final List<Authentication> authentications = new CopyOnWriteArrayList<Authentication>();

    private final List<RemoteRepository> repositories = new ArrayList<>();

    public void addProxy(Proxy proxy) {
        proxies.add(proxy);
    }

    public void addMirror(Mirror mirror) {
        mirrors.add(mirror);
    }

    public void addAuthentication(Authentication authentication) {
        authentications.add(authentication);
    }

    public boolean addRepository(RemoteRepository remoteRepository) {
        return repositories.add(remoteRepository);
    }

    private static <T> boolean eq(T o1, T o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }

    public ShathelMavenRepository() {
        this(ShathelMavenSettings.builder().build());
    }

    public ShathelMavenRepository(ShathelMavenSettings settings) {
        this.shathelMavenSettings = settings;
        locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.setServices(ModelBuilder.class, new DefaultModelBuilderFactory()
                .newInstance());
        locator.addService(RepositoryConnectorFactory.class,
                BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.addService(TransporterFactory.class, ClasspathTransporterFactory.class);

        addRepository(settings.getRepository());
        settingsDecrypter = createDecryptorFactorynewInstance(shathelMavenSettings);


        DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setUserSettingsFile(this.shathelMavenSettings.getUserSettingsFile());
        request.setGlobalSettingsFile(this.shathelMavenSettings.getGlobalSettingsFile());
        request.setSystemProperties(this.shathelMavenSettings.getSystemProperties());

        SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
        try {
            this.settings = settingsBuilder.build(request).getEffectiveSettings();
        } catch (SettingsBuildingException e) {
            throw new RuntimeException("Could not process settings.xml: " + e.getMessage(), e);
        }

        SettingsDecryptionResult result = settingsDecrypter
                .decrypt(new DefaultSettingsDecryptionRequest(this.settings));
        this.settings.setServers(result.getServers());
        this.settings.setProxies(result.getProxies());
    }

    public RepositorySystemSession getSession() {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        Map<Object, Object> configProps = new LinkedHashMap<>();
        configProps.put(ConfigurationProperties.USER_AGENT, getUserAgent());
        processServerConfiguration(configProps);
        session.setConfigProperties(configProps);
        session.setOffline(isOffline());
        session.setProxySelector(getProxySelector());
        session.setMirrorSelector(getMirrorSelector());
        session.setAuthenticationSelector(getAuthSelector());
        session.setCache(new DefaultRepositoryCache());
        session.setLocalRepositoryManager(getLocalRepoMan(session));
        return session;
    }


    public DefaultServiceLocator getLocator() {
        return locator;
    }

    public DependencyNode resolveDependency(Dependency dependency) throws DependencyCollectionException, DependencyResolutionException {
        RepositorySystemSession session = getSession();
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        for (RemoteRepository repository : repositories) {
            collectRequest.addRepository(repository);
        }

        DependencyNode node = getLocator().getService(RepositorySystem.class).collectDependencies(session, collectRequest).getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(node);
        getLocator().getService(RepositorySystem.class).resolveDependencies(session, dependencyRequest);
        return node;
    }


    protected String getUserAgent() {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append("Shathel/").append("1.x");
        buffer.append(" (");
        buffer.append("Java ").append(System.getProperty("java.version"));
        buffer.append("; ");
        buffer.append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version"));
        buffer.append(")");
        buffer.append(" Aether");
        return buffer.toString();
    }

    protected boolean isOffline() {
        return getSettings().isOffline();
    }

    protected void processServerConfiguration(Map<Object, Object> configProps) {
        Settings settings = getSettings();
        for (Server server : settings.getServers()) {
            if (server.getConfiguration() != null) {
                Xpp3Dom dom = (Xpp3Dom) server.getConfiguration();
                for (int i = dom.getChildCount() - 1; i >= 0; i--) {
                    Xpp3Dom child = dom.getChild(i);
                    if ("wagonProvider".equals(child.getName())) {
                        dom.removeChild(i);
                    } else if ("httpHeaders".equals(child.getName())) {
                        configProps.put(ConfigurationProperties.HTTP_HEADERS + "."
                                + server.getId(), getHttpHeaders(child));
                    }
                }

                configProps.put("aether.connector.wagon.config." + server.getId(), dom);
            }

            configProps.put("aether.connector.perms.fileMode." + server.getId(),
                    server.getFilePermissions());
            configProps.put("aether.connector.perms.dirMode." + server.getId(),
                    server.getDirectoryPermissions());
        }
    }

    protected Map<String, String> getHttpHeaders(Xpp3Dom dom) {
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < dom.getChildCount(); i++) {
            Xpp3Dom child = dom.getChild(i);
            Xpp3Dom name = child.getChild("name");
            Xpp3Dom value = child.getChild("value");
            if (name != null && name.getValue() != null) {
                headers.put(name.getValue(), (value != null) ? value.getValue() : null);
            }
        }
        return Collections.unmodifiableMap(headers);
    }

    protected File getDefaultLocalRepoDir() {
        String dir = shathelMavenSettings.getLocalRepo();
        if (dir != null) {
            return new File(dir);
        }

        Settings settings = getSettings();
        if (settings.getLocalRepository() != null) {
            return new File(settings.getLocalRepository());
        }

        return new File(new File(System.getProperty("user.home"), ".m2"), "repository");
    }

    protected LocalRepositoryManager getLocalRepoMan(RepositorySystemSession session) {

        File repoDir = getDefaultLocalRepoDir();
//        LocalRepositoryManager localRepositoryManager = new SimpleLocalRepoManagerMetadataFix(repoDir);

        LocalRepository repo = new LocalRepository(
                repoDir);
        LocalRepositoryManager localRepositoryManager = locator.getService(RepositorySystem.class).newLocalRepositoryManager(session, repo);
        return localRepositoryManager;
    }

    protected Settings getSettings() {
        return this.settings;
    }

    protected org.eclipse.aether.repository.Authentication toAuthentication(
            Authentication auth) {
        if (auth == null) {
            return null;
        }
        AuthenticationBuilder authBuilder = new AuthenticationBuilder();
        authBuilder.addUsername(auth.getUsername()).addPassword(auth.getPassword());
        authBuilder.addPrivateKey(auth.getPrivateKeyFile(), auth.getPassphrase());
        return authBuilder.build();
    }

    protected org.eclipse.aether.repository.Proxy toProxy(Proxy proxy) {
        if (proxy == null) {
            return null;
        }
        return new org.eclipse.aether.repository.Proxy(proxy.getType(), proxy.getHost(),
                proxy.getPort(), toAuthentication(proxy.getAuthentication()));
    }

    protected ProxySelector getProxySelector() {
        DefaultProxySelector selector = new DefaultProxySelector();

        for (Proxy proxy : proxies) {
            selector.add(toProxy(proxy), proxy.getNonProxyHosts());
        }

        Settings settings = getSettings();
        for (org.apache.maven.settings.Proxy proxy : settings.getProxies()) {
            AuthenticationBuilder auth = new AuthenticationBuilder();
            auth.addUsername(proxy.getUsername()).addPassword(proxy.getPassword());
            selector.add(
                    new org.eclipse.aether.repository.Proxy(proxy.getProtocol(),
                            proxy.getHost(), proxy.getPort(), auth.build()),
                    proxy.getNonProxyHosts());
        }

        return selector;
    }

    protected MirrorSelector getMirrorSelector() {
        DefaultMirrorSelector selector = new DefaultMirrorSelector();

        for (Mirror mirror : mirrors) {
            selector.add(mirror.getId(), mirror.getUrl(), mirror.getType(), false,
                    mirror.getMirrorOf(), null);
        }

        Settings settings = getSettings();
        for (org.apache.maven.settings.Mirror mirror : settings.getMirrors()) {
            selector.add(String.valueOf(mirror.getId()), mirror.getUrl(),
                    mirror.getLayout(), false, mirror.getMirrorOf(),
                    mirror.getMirrorOfLayouts());
        }

        return selector;
    }

    protected AuthenticationSelector getAuthSelector() {
        DefaultAuthenticationSelector selector = new DefaultAuthenticationSelector();

        Collection<String> ids = new HashSet<String>();
        for (Authentication auth : authentications) {
            List<String> servers = auth.getServers();
            if (!servers.isEmpty()) {
                org.eclipse.aether.repository.Authentication a = toAuthentication(auth);
                for (String server : servers) {
                    if (ids.add(server)) {
                        selector.add(server, a);
                    }
                }
            }
        }

        Settings settings = getSettings();
        for (Server server : settings.getServers()) {
            AuthenticationBuilder auth = new AuthenticationBuilder();
            auth.addUsername(server.getUsername()).addPassword(server.getPassword());
            auth.addPrivateKey(server.getPrivateKey(), server.getPassphrase());
            selector.add(server.getId(), auth.build());
        }

        return new ConservativeAuthenticationSelector(selector);
    }


}