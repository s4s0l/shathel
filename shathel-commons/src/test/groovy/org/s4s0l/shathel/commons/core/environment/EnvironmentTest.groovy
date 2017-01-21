package org.s4s0l.shathel.commons.core.environment

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.Solution
import org.s4s0l.shathel.commons.core.Stack
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.core.stack.StackTreeDescription
import org.s4s0l.shathel.commons.core.stack.StackTreeDescriptionTest
import org.s4s0l.shathel.commons.core.storage.Storage
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class EnvironmentTest extends Specification {

    def "sample api usage"(){
        File f;
        Shathel x;
        Storage s;
//        Environment e;
//
        x.verify()
//
        s = x.getStorage(f)//pobieramy istniejace
        s = x.initStorage(f)
//
        s.verify()

        s.isModified()

        s.save()
        //kasuje tymczasowe katalogi i np sciaga z gita zmiany
        s.restore()
//
        Solution solution = x.getSolution(s)

        Environment e = solution.getEnvironment('DEV')

        //czy jest katalog z ustawieniami docker machine
        //czu isnieja definicje maszyn zgodne z konfigiem
        e.isInitialized()

        //zalozenie maszyn
        //pobiera configi ze storage jak ich nie ma
        e.initialize()

        //upewnienie sie ze maszyny dzialaja
        e.start()

        //czy dzialaja
        e.isStarted()


        e.stop()

        //kasuje maszyny
        e.destroy()

        //czy mozna sie podlaczyc do kazdej z maszyn
        //czy na kazdej z maszyn dziala docker
        //czy na managerze info mówi to samo co my wiemy
        e.verify()

        Stack stack = solution.openStack(e,new StackReference())

        StackTreeDescription std = stack.getDependencies();

        stack.createStartCommand();




    }
}
