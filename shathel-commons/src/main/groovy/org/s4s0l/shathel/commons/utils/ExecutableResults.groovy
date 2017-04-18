package org.s4s0l.shathel.commons.utils

/**
 * @author Marcin Wielgus
 */
class ExecutableResults {
    String output = ""
    boolean status = true
    int retcode = 0

    ExecutableResults() {
    }

    ExecutableResults(String output, boolean status, int retcode) {
        this.output = output
        this.status = status
        this.retcode = retcode
    }


}
