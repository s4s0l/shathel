package org.s4s0l.shathel.deployer.shell;

import org.slf4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.shell.CommandLine;
import org.springframework.shell.ShellException;
import org.springframework.shell.SimpleShellCommandLineOptions;
import org.springframework.shell.core.ExitShellRequest;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Spring Boot Shim
 * From https://github.com/linux-china/spring-boot-starter-shell because it is released as snapshot
 *
 * @author jeffellin
 * @author linux_china
 */
public class BootShim {
    private static StopWatch sw = new StopWatch("Spring Shell");
    private static CommandLine commandLine;
    private ConfigurableApplicationContext ctx;
    private static final Logger LOGGER = getLogger(BootShim.class);

    public BootShim(String[] args, ConfigurableApplicationContext context) {
        this.ctx = context;
        try {
            commandLine = SimpleShellCommandLineOptions.parseCommandLine(args);
        } catch (IOException var5) {
            throw new ShellException(var5.getMessage(), var5);
        }
        this.configureApplicationContext(this.ctx);
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner((BeanDefinitionRegistry) this.ctx);
        if (commandLine.getDisableInternalCommands()) {
            scanner.scan("org.springframework.shell.converters", "org.springframework.shell.plugin.support");
        } else {
            scanner.scan("org.springframework.shell.commands", "org.springframework.shell.converters", "org.springframework.shell.plugin.support");
        }
    }

    private void configureApplicationContext(ConfigurableApplicationContext annctx) {
        this.createAndRegisterBeanDefinition(annctx, JLineShellComponent.class, "shell");
        annctx.getBeanFactory().registerSingleton("commandLine", commandLine);
    }

    private void createAndRegisterBeanDefinition(ConfigurableApplicationContext annctx, Class<?> clazz, String name) {
        RootBeanDefinition rbd = new RootBeanDefinition();
        rbd.setBeanClass(clazz);
        DefaultListableBeanFactory bf = (DefaultListableBeanFactory) annctx.getBeanFactory();
        if (name != null) {
            bf.registerBeanDefinition(name, rbd);
        } else {
            bf.registerBeanDefinition(clazz.getSimpleName(), rbd);
        }

    }

    public ExitShellRequest run() {
        sw.start();
        String[] commandsToExecuteAndThenQuit = commandLine.getShellCommandsToExecute();
        JLineShellComponent shell = this.ctx.getBean("shell", JLineShellComponent.class);
        //TODO handle --wrkDir argument and change user.home, see WorkingDirectoryCommand
        ExitShellRequest exitShellRequest;
        if (null != commandsToExecuteAndThenQuit) {
            boolean successful = false;
            exitShellRequest = ExitShellRequest.FATAL_EXIT;
            for (String cmd : commandsToExecuteAndThenQuit) {
                successful = shell.executeCommand(cmd).isSuccess();
                if (!successful) {
                    exitShellRequest = ExitShellRequest.FATAL_EXIT;
                    break;
                }
            }
            if (successful) {
                exitShellRequest = ExitShellRequest.NORMAL_EXIT;
            }
        } else {
            shell.start();
            shell.promptLoop();
            exitShellRequest = shell.getExitShellRequest();
            if (exitShellRequest == null) {
                exitShellRequest = ExitShellRequest.NORMAL_EXIT;
            }
            shell.waitForComplete();
        }
        sw.stop();
        if (shell.isDevelopmentMode()) {
            LOGGER.debug("Total execution time: " + sw.getLastTaskTimeMillis() + " ms");
        }
        return exitShellRequest;
    }

}
