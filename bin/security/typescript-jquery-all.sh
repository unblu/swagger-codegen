#!/bin/sh

SCRIPT="$0"

while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

if [ ! -d "${APP_DIR}" ]; then
  APP_DIR=`dirname "$SCRIPT"`/..
  APP_DIR=`cd "${APP_DIR}"; pwd`
fi

executable="./modules/swagger-codegen-cli/target/swagger-codegen-cli.jar"

if [ ! -f "$executable" ]
then
  mvn clean package
fi

# if you've executed sbt assembly previously it will use that instead.
export JAVA_OPTS="${JAVA_OPTS} -XX:MaxPermSize=256M -Xmx1024M -DloggerPath=conf/log4j.properties"

echo "Typescript jquery Petstore API client (default setting)"
ags="$@ generate -i modules/swagger-codegen/src/test/resources/2_0/petstore-security-test.yaml -l typescript-jquery -o samples/client/petstore-security-test/typescript-jquery/default"
# java $JAVA_OPTS -jar $executable $ags

echo "Typescript jquery Petstore API client with npm setting"
ags="$@ generate -i modules/swagger-codegen/src/test/resources/2_0/petstore-security-test.yaml -l typescript-jquery -c bin/typescript-petstore-npm.json -o samples/client/petstore-security-test/typescript-jquery/npm"
# java $JAVA_OPTS -jar $executable $ags
