package org.s4s0l.shathel.commons.remoteswarm.provider

import com.google.common.io.Files
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.cert.CertificateManagerImpl
import org.s4s0l.shathel.commons.core.DefaultSettingsImporterExporter
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.EnvironmentProvider
import org.s4s0l.shathel.commons.core.model.EnvironmentFileModel
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironment
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentAccessManager
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentAccessManagerImpl
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentApiFacade
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentController
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentInventoryFile
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageContext
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageContextImpl
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageDescription

import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentProcessorsFactory
import org.s4s0l.shathel.commons.remoteswarm.downloader.EnvironmentPackageDownloader
import org.s4s0l.shathel.commons.ssh.SshKeyProvider
import org.s4s0l.shathel.commons.ssh.SshTunelManagerImpl
import org.s4s0l.shathel.commons.utils.ExtensionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentProvider implements EnvironmentProvider {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(RemoteEnvironmentProvider.class)
    private ExtensionContext extensionContext

    @Override
    void setExtensionContext(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext
    }

    @Override
    String getType() {
        return "remote"
    }

    @Override
    Environment getEnvironment(EnvironmentContext environmentContext) {
        RemoteEnvironmentPackageContext packageContext = createPackageContext(environmentContext)
        DefaultSettingsImporterExporter machineSettingsImporterExporter = new DefaultSettingsImporterExporter();
        RemoteEnvironmentAccessManager accessManager = createAccessManager(packageContext)
        RemoteEnvironmentApiFacade facade = new RemoteEnvironmentApiFacade(accessManager, packageContext)
        RemoteEnvironmentProcessorsFactory factory = new RemoteEnvironmentProcessorsFactory(facade, extensionContext, packageContext)
        RemoteEnvironmentController controller = new RemoteEnvironmentController(packageContext, factory,
                facade, accessManager)
        RemoteEnvironment re = new RemoteEnvironment(machineSettingsImporterExporter, packageContext, controller, facade)
        return re;
    }

    private RemoteEnvironmentPackageContext createPackageContext(EnvironmentContext environmentContext) {
        String gav = environmentContext.getEnvironmentDescription().getParameter("gav").orElseThrow {
            new RuntimeException("Remote environment needs gav parameter")
        }
        Optional<Boolean> forceful = environmentContext.getEnvironmentDescription().getParameterAsBoolean("forceful");
        File packagerRoot = extensionContext.lookupAll(EnvironmentPackageDownloader).map {
            it.download(new StackLocator(gav), environmentContext.dependencyCacheDirectory, forceful.orElse(false))
        }.filter {
            it.isPresent()
        }.findFirst().orElseThrow {
            new RuntimeException("Unable to find environment package $gav")
        }.get()
        def packageDesc = new RemoteEnvironmentPackageDescription(EnvironmentFileModel.load(new File(packagerRoot, "shthl-env.yml")), packagerRoot)
        RemoteEnvironmentPackageContext packageContext = new RemoteEnvironmentPackageContextImpl(environmentContext, packageDesc)
        packageContext
    }

    private RemoteEnvironmentAccessManager createAccessManager(RemoteEnvironmentPackageContext packageContext) {
        def email = packageContext.environmentDescription.getParameter("email").orElse("someone@${packageContext.contextName}".toString())

        File controlSocketsLocation = packageContext.tempDirectory
        if (controlSocketsLocation.absolutePath.size() >= (108 - 18)) {
            controlSocketsLocation = new File(new File(System.getProperty("java.io.tmpdir")), packageContext.contextName)
            controlSocketsLocation.mkdirs()
            LOGGER.warn("Control socket location is too long, assumed max is 108 characters.\n" +
                    " Change the location of shathel solution to shorter path.\n " +
                    "See cat /usr/include/linux/un.h | grep \"define UNIX_PATH_MAX\" to see your limits.\n" +
                    "Will use temp file in: ${controlSocketsLocation.absolutePath}. Files in this dir can be used to hack you:)")
        }

        RemoteEnvironmentAccessManager accessManager = new RemoteEnvironmentAccessManagerImpl(
                new RemoteEnvironmentInventoryFile(packageContext.ansibleInventoryFile),
                new CertificateManagerImpl(packageContext.certsDirectory, "TodoRemoveIt".bytes, packageContext.contextName),
                new SshKeyProvider(packageContext.keysDirectory, email),
                new SshTunelManagerImpl(controlSocketsLocation, packageContext.remoteUser, 33333, packageContext.knownHostsFile)
        )
        accessManager
    }
}
