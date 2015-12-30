#!/bin/bash

# if we are linked, use that info
if [ "$MONGO_PORT" != "" ]; then
  # Sample: MONGO_PORT=tcp://172.17.0.20:27017
  export SPRING_DATA_MONGODB_HOST=`echo $MONGO_PORT|sed 's;.*://\([^:]*\):\(.*\);\1;'`
  export SPRING_DATA_MONGODB_PORT=`echo $MONGO_PORT|sed 's;.*://\([^:]*\):\(.*\);\2;'`
fi

echo "SPRING_DATA_MONGODB_HOST: $SPRING_DATA_MONGODB_HOST"
echo "SPRING_DATA_MONGODB_PORT: $SPRING_DATA_MONGODB_PORT"


cat > application.properties <<EOF
#Database Name - default is test
dbname=${SPRING_DATA_MONGODB_DATABASE:-dashboard}

#Database HostName - default is localhost
dbhost=${SPRING_DATA_MONGODB_HOST:-10.0.1.1}

#Database Port - default is 27017
dbport=${SPRING_DATA_MONGODB_PORT:-9999}

#Database Username - default is blank
dbusername=${SPRING_DATA_MONGODB_USERNAME:-db}

#Database Password - default is blank
dbpassword=${SPRING_DATA_MONGODB_PASSWORD:-dbpass}

#Collector schedule (required)
sonar.cron=0 0/5 * * * *

#Sonar server(s) (required) - Can provide multiple
sonar.servers[0]=${SERVER_URL:-}

#Sonar Metrics
sonar.metrics=ncloc,line_coverage,violations,critical_violations,major_violations,blocker_violations,sqale_index,test_success_density,test_failures,test_errors,tests

EOF
