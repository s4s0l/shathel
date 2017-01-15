package org.s4s0l.shathel.deployer

import org.s4s0l.shathel.deployer.shell.SpringShellApplication
import org.s4s0l.shathel.deployer.shell.customization.CustomBanner
import org.s4s0l.shathel.deployer.shell.customization.CustomPrompt
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan


@SpringBootApplication
@ComponentScan(['org.s4s0l.shathel.deployer.shell.customization'])
class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static void main(String[] args) {
        int exitCode = SpringShellApplication.run(Main.class, args)
        System.exit(exitCode)
    }

    @Bean
    SampleCommand SampleCommand() { new SampleCommand() }
}

