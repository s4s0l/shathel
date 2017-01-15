package org.s4s0l.shathel.deployer

import org.s4s0l.shathel.deployer.shell.SpringShellApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
class Main {

    static void main(String[] args) {
        SpringShellApplication.run(Main.class, args);
    }


    @Bean
    SampleCommand simpleCommand() {
        return new SampleCommand();
    }

    @Bean
    CustomPrompt customPrompt() { new CustomPrompt() }

    @Bean
    CustomBanner CustomBanner() { new CustomBanner() }
}

