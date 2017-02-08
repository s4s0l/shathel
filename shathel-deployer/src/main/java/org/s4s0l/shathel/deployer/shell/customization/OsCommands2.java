/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.s4s0l.shathel.deployer.shell.customization;

import org.springframework.core.annotation.Order;
import org.springframework.shell.commands.OsOperations;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Command type to allow execution of native OS commands from the Spring Shell.
 * 
 * @author Stefan Schmidt
 * @since 1.2.0
 */
@Component
@Order
public class OsCommands2 implements CommandMarker {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(OsCommands2.class);

    private OsOperations osOperations = new OsOperationsImpl();

    @CliCommand(value = "~", help = "Allows execution of operating system (OS) commands")
    public void command(
            @CliOption(key = { "", "command" }, mandatory = false, specifiedDefaultValue = "", unspecifiedDefaultValue = "", help = "The command to execute") final String command) {

    	System.out.println("command2 is:" + command);
        if (command != null && command.length() > 0) {
            try {
                osOperations.executeCommand(command);
            }
            catch (final IOException e) {
                LOGGER.severe("Unable to execute command " + command + " ["
                        + e.getMessage() + "]");
            }
        }
    }
}