#!/bin/sh
echo "Running with JAVA_OPTS=$JAVA_OPTS and argsuments $@"
exec java $JAVA_OPTS -jar ./service.jar $@
                            