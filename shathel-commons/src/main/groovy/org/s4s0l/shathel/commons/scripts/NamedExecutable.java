package org.s4s0l.shathel.commons.scripts;

/**
 * @author Marcin Wielgus
 */
public interface NamedExecutable extends Executable {


    default String getName(){
        return getClass().getSimpleName();
    }
}
