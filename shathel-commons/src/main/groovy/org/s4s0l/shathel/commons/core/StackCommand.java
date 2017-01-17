package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.files.model.ComposeFileModel;

import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class StackCommand {
    public enum Type {
        START,
        NOOP,
        STOP,
        UPDATE
    }

    private final Type type;
    private final ComposeFileModel mutableModel;
    private final StackDescription description;
    private final List<StackProvisionerDefinition> provisioners;


    public StackCommand(Type type, ComposeFileModel mutableModel, StackDescription description, List<StackProvisionerDefinition> provisioners) {
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

    public ComposeFileModel getMutableModel() {
        return mutableModel;
    }

    public List<StackProvisionerDefinition> getProvisioners() {
        return provisioners;
    }
}
