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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        Cuntinue cuntinue = carryOn(commandsToExecuteAndThenQuit);


        if (!cuntinue.commands.isEmpty()) {
            boolean successful;
            exitShellRequest = ExitShellRequest.FATAL_EXIT;
            for (String cmd : cuntinue.commands) {
                successful = shell.executeCommand(cmd).isSuccess();
                if (!successful) {
                    exitShellRequest = ExitShellRequest.FATAL_EXIT;
                    break;
                }else{
                    exitShellRequest = ExitShellRequest.NORMAL_EXIT;
                }
            }
            if (exitShellRequest == ExitShellRequest.NORMAL_EXIT
                    && cuntinue.carryOn) {
                exitShellRequest = regularStart(shell);
                shell.waitForComplete();
            }
        } else {
            exitShellRequest = regularStart(shell);
            shell.waitForComplete();
        }
        if (exitShellRequest == null) {
            exitShellRequest = ExitShellRequest.NORMAL_EXIT;
        }
        sw.stop();
        if (shell.isDevelopmentMode()) {
            LOGGER.debug("Total execution time: " + sw.getLastTaskTimeMillis() + " ms");
        }
        return exitShellRequest;
    }

    private ExitShellRequest regularStart(JLineShellComponent shell) {
        ExitShellRequest exitShellRequest;
        shell.start();
        shell.promptLoop();
        exitShellRequest = shell.getExitShellRequest();
        return exitShellRequest;
    }

    private Cuntinue carryOn(String[] commandsToExecuteAndThenQuit) {
        if (commandsToExecuteAndThenQuit == null) {
            return new Cuntinue(Collections.emptyList(), false);
        } else {

            List<String> asList = new ArrayList<>(Arrays.asList(commandsToExecuteAndThenQuit));
            int wrk = asList.indexOf("--continue");
            if (wrk >= 0) {
                asList.remove(wrk);
                return new Cuntinue(asList, true);
            }
            return new Cuntinue(asList, false);
        }
    }

    class Cuntinue {
        List<String> commands;
        boolean carryOn;

        public Cuntinue(List<String> commands, boolean carryOn) {
            this.commands = commands;
            this.carryOn = carryOn;
        }
    }

}
