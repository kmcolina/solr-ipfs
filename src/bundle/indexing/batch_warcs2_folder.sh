#!/bin/bash

pushd ${BASH_SOURCE%/*} > /dev/null

FILES=warcs2/*
for f in $FILES
do
  echo "Processing $f file..."
java -Xmx1024M -Djava.io.tmpdir=tika_tmp -jar warc-indexer-3.2.0-SNAPSHOT-jar-with-dependencies.jar -c config3.conf -s  "http://localhost:8983/solr/netarchivebuilder"  $f

done

wget  -qO- "http://localhost:8983/solr/netarchivebuilder/update?commit=true&openSearcher=true"  > /dev/null

popd > /dev/null
