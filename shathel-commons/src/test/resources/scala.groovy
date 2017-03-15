#!/usr/bin/env groovy
@Grapes(
        @Grab(group = 'org.scala-lang', module = 'scala-library', version = '2.12.1')
)
import scala.Option

output << ['groovy': input('groovy'),
        'scala' : Option.class.getName()]