package org.s4s0l.shathel.commons.remoteswarm

enum ProcessorCommand {
    APPLY,
    START,
    STOP,
    DESTROY,
    STARTED,
    INITED;

    static Optional<ProcessorCommand> toCommand(String value) {
        if (value == null) {
            return Optional.empty()
        }
        def commands = values()
        for (ProcessorCommand c : commands) {
            if (c.toString().toLowerCase().equals(value.toLowerCase())) {
                return Optional.of(c)
            }
        }
        return Optional.empty()
    }
}