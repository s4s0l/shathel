package org.s4s0l.shathel.deployer

import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption

/**
 * @author Matcin Wielgus
 */

class SampleCommand implements CommandMarker {
    @CliCommand(value = "hw", help = "Print a simple hello world message")
    public String simple(
            @CliOption(key = ["message"], mandatory = true, help = "The hello world message")
            final String message,
            @CliOption(key = ["location"], mandatory = false, help = "Where you are saying hello", specifiedDefaultValue = "At work")
            final String location) { return "Message = [" + message + "] Location = [" + location + "]";
    }
}
