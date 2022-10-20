# Solr IPFS - indexing and search for AWV


## Build and test with Docker

* Run `docker-compose build` to build (or re-build) the container.
* Run `docker-compose up` to run the Solr-IPFS container along with a Solr instance that contains some test data.

After running `docker-compose up` you should see logs from four services (`ubuntu-python-ipfs`, `solr`, `flask-app` and `populate`).

The Solr instance runs `kmcolina/solr`, which contains a suitable collection with the right schema.  The Solr service itself should be available on port 8983, i.e. http://localhost:8983/solr/ 


### 3) INDEXING
Solr-IPFS uses a Solr index of WARC files to support freetext search and more complex queries.  

The file `populate.sh` in the `indexing`-folder allows for multi processing and keeps track of already
indexed files, so the collection can be extended by adding more WARCs and running the script again.

Example usage:
```
java -jar /jars/warc-indexer.jar -s ${SOLR_URL} /docker/test-warcs/TEST-20220304210400500-00000-80~h3w~8443.warc /docker/test-warcs/IAH-20080430204825-00000-blackbook.warc

```

The script `warc-indexer.sh` is not available for Windows. For that platform only a more primitive script is provided that also works for Linux/MacOs.
1. Copy ARC/WARC files into folder: `indexing/warcs1`  
2. Start indexing:  call `indexing/batch_warcs1_folder.sh` (or batch_warcs1_folder.bat for windows)

# solr-ipfs
