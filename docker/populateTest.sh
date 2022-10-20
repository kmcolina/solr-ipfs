#!/bin/bash
echo Populate testing enter
echo Will add sample data to ${SOLR_URL} after waiting for ${DELAY:-15} seconds for Solr to start up.
sleep ${DELAY:-15}
java -jar /jars/warc-indexer.jar -s ${SOLR_URL} /docker/test-warcs/TEST-20220304210400500-00000-80~h3w~8443.warc 

java -jar /jars/warc-indexer.jar -s ${SOLR_URL} /docker/test-warcs/TEST-20220304210400500-00000-80~h3w~8443.warc >salidaTest

bash