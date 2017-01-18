package org.s4s0l.shathel.commons.core.enricher

import org.mockito.Mockito
import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import org.s4s0l.shathel.commons.core.stack.StackDescription
import org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition
import spock.lang.Specification

import static org.s4s0l.shathel.commons.core.stack.StackEnricherDefinition.Target.ALL

/**
 * @author Matcin Wielgus
 */
class GroovishEnricherTest extends Specification {


    def "should evaluate scripts"() {
        given:
        StackDescription origin = Mockito.mock(StackDescription)
        StackDescription modified = Mockito.mock(StackDescription)
        Mockito.when(modified.name).thenReturn("xxx")
        GroovishEnricher e = new GroovishEnricher(new StackEnricherDefinition(origin, ALL, "x",
                """
                    println s.compose.version 
                    s.compose.setVersion('1') 
                    return s.stack.name
                """, "groovy"));
        ComposeFileModel model = ComposeFileModel.load("""
            version: '2'
            services:
                x:
        """)

        when:
        e.enrich(modified, model)

        then:
        model.version == "1"
    }
}
