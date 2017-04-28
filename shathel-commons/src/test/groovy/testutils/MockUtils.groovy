package testutils

import org.s4s0l.shathel.commons.core.environment.ShathelNode

/**
 * @author Marcin Wielgus
 */
class MockUtils {
    static def shathelManagerNode(int num){
        new ShathelNode("manager-$num", "1.1.1.$num", "2.2.1.$num", "manager")
    }
    static def shathelWorkerNode(int num){
        new ShathelNode("worker-$num", "1.1.2.$num", "2.2.2.$num", "worker")
    }
}
