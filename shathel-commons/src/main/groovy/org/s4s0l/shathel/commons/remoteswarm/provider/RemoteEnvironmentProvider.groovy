package org.s4s0l.shathel.commons.remoteswarm.provider

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
import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentPackageDescription

import org.s4s0l.shathel.commons.remoteswarm.RemoteEnvironmentProcessorsFactory
import org.s4s0l.shathel.commons.remoteswarm.downloader.EnvironmentPackageDownloader
import org.s4s0l.shathel.commons.ssh.SshKeyProvider
import org.s4s0l.shathel.commons.ssh.SshTunelManagerImpl

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class RemoteEnvironmentProvider implements EnvironmentProvider {
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
        RemoteEnvironmentController controller = new RemoteEnvironmentController(packageContext, new RemoteEnvironmentProcessorsFactory(),
                facade, accessManager)
        RemoteEnvironment re = new RemoteEnvironment(machineSettingsImporterExporter, packageContext, controller, facade)
        return re;
    }

    private RemoteEnvironmentPackageContext createPackageContext(EnvironmentContext environmentContext) {
        String gav = environmentContext.getEnvironmentDescription().getParameter("gav").orElseThrow {
            new RuntimeException("Remote environment needs gav parameter")
        }
        Optional<Boolean> forceful = environmentContext.getEnvironmentDescription().getParameterAsBoolean("forceful");
        File packagerRoot = environmentContext.extensionContext.lookupAll(EnvironmentPackageDownloader).map {
            it.download(new StackLocator(gav), environmentContext.dependencyCacheDirectory, forceful.orElse(false))
        }.filter {
            it.isPresent()
        }.findFirst().orElseThrow {
            new RuntimeException("Unable to find environment package $gav")
        }.get()
        def packageDesc = new RemoteEnvironmentPackageDescription(EnvironmentFileModel.load(new File(packagerRoot, "shtl-env.yml")), packagerRoot)
        RemoteEnvironmentPackageContext packageContext = new RemoteEnvironmentPackageContext(environmentContext, packageDesc)
        packageContext
    }

    private RemoteEnvironmentAccessManager createAccessManager(RemoteEnvironmentPackageContext packageContext) {
        def email = packageContext.environmentDescription.getParameter("email").orElse("someone@${packageContext.contextName}".toString())

        RemoteEnvironmentAccessManager accessManager = new RemoteEnvironmentAccessManagerImpl(
                new RemoteEnvironmentInventoryFile(packageContext.ansibleInventoryFile),
                new CertificateManagerImpl(packageContext.certsDirectory, "TodoRemoveIt".bytes, packageContext.contextName),
                new SshKeyProvider(packageContext.keysDirectory, email),
                new SshTunelManagerImpl(packageContext.tempDirectory, packageContext.packageDescription.remoteUser, 33333, packageContext.knownHostsFile)
        )
        accessManager
    }
}
