package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.environment.StackCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Marcin Wielgus
 */
public class StackOperations {


    public static class Builder {
        private final List<StackCommand> commands = new ArrayList<>();
        private final Environment environment;

        public Builder(Environment environment) {
            this.environment = environment;
        }

        public Builder add(StackCommand x) {
            commands.add(x);
            return this;
        }

        public Builder add(List<StackCommand> x) {
            commands.addAll(x);
            return this;
        }

        public StackOperations build() {
            return new StackOperations(Collections.unmodifiableList(commands), environment);

        }
    }

    public static Builder builder(Environment environment) {
        return new Builder(environment);
    }

    private final List<StackCommand> commands;
    private final Environment environment;

    StackOperations(List<StackCommand> commands, Environment environment) {
        this.commands = commands;
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public List<StackCommand> getCommands() {
        return commands;
    }
}
