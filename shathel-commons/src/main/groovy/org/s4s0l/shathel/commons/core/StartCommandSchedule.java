package org.s4s0l.shathel.commons.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class StartCommandSchedule {


    public static class Builder {
        private final List<StackCommand> commands = new ArrayList<>();

        public Builder add(StackCommand x) {
            commands.add(x);
            return this;
        }

        public StartCommandSchedule build() {
            return new StartCommandSchedule(Collections.unmodifiableList(commands));

        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final List<StackCommand> commands;


    StartCommandSchedule(List<StackCommand> commands) {
        this.commands = commands;
    }


    public List<StackCommand> getCommands() {
        return commands;
    }
}
