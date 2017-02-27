package org.s4s0l.shathel.commons.core.environment;

import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executable;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class StackCommand {
    public enum Type {
        START(true),
        NOOP(false),
        STOP(false),
        UPDATE(true);

        Type(boolean willRun) {
            this.willRun = willRun;
        }

        public final boolean willRun;
    }

    private final Type type;
    private final ComposeFileModel mutableModel;
    private final StackDescription description;
    private final List<Executable> provisioners;


    public StackCommand(Type type, ComposeFileModel mutableModel, StackDescription description, List<Executable> provisioners) {
        this.type = type;
        this.mutableModel = mutableModel;
        this.description = description;
        this.provisioners = provisioners;
    }

    public Type getType() {
        return type;
    }

    public StackDescription getDescription() {
        return description;
    }

    public ComposeFileModel getComposeModel() {
        return mutableModel;
    }

    public List<Executable> getEnricherPreProvisioners() {
        return provisioners;
    }
}
