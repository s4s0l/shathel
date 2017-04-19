package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.core.environment.ShathelNode
import org.s4s0l.shathel.commons.utils.ExecutableResults

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface SshOperations {
    ExecutableResults ssh(ShathelNode node, String command)

    ExecutableResults sudo(ShathelNode node, String command)

    void scp(ShathelNode node, File from, String to)

    String getScpUser()
}