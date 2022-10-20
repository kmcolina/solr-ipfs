# SolrWayback

## SolrWayback 4.3.0 software bundle has been released
SolrWayback bundle release 4.3.0 can be downloaded here: https://github.com/netarchivesuite/solrwayback/releases/tag/4.3.0

## See also
Warc-indexer: https://github.com/ukwa/webarchive-discovery/tree/master/warc-indexer<br>
The Warc indexer is the indexing engine for documents in SolrWayback. It is maintained by the British Library.

Netsearch(Archon/Arctika): https://github.com/netarchivesuite/netsearch<br>
Archon/Actika is a book keeping application for warc-files and can start multiple concurrent warc-indexer jobs for large scale netarchives. 


 
## Build and usage
 * Open data base interface: http://localhost:8983/solr
 * Open search interface: http://localhost:5000


## Build and test with Docker
There is a `Dockerfile` that builds the SolrWayback WAR and deploys it via a containerised Tomcat server. This has been set up to allow configuration via environment variables as an alternative to supplying the properties file directly. There is also a `docker-compose` file that is intended to help with local building and testing of the SolrWayback Docker container. To use it:

* Run `docker-compose build` to build (or re-build) the container.
* Run `docker-compose up` to run the SolrWayback container along with a Solr instance that contains some test data.

After running `docker-compose up` you should see logs from three services (`solrwayback`, `solr` and `populate`).

The Solr instance runs `ukwa/webarchive-discovery-solr`, which contains a suitable collection with the right schema.  The Solr service itself should be available on port 18983, i.e. http://localhost:18983/solr/ 

The `docker-compose up` command also runs a helper service to populate the instance with some test data.  This waits a few seconds (for Solr to start up) before sending 1000 records to it, and then exits. If needed, the test Solr instance can be run separately via `docker-compose up solr populate`.

Note that if you do not have direct internet access, you will need to set proxy variables including `MAVEN_OPTS` appropriately in order for the build to work.

### 3) INDEXING
SolrWayback uses a Solr index of WARC files to support freetext search and more complex queries.  
If you do not have existing WARC files, see steps below on harvesting with wget.        

The script `warc-indexer.sh` in the `indexing`-folder allows for multi processing and keeps track of already
indexed files, so the collection can be extended by adding more WARCs and running the script again.


Call `indexing/warc-indexer.sh -h` for usage and how to adjust the number of processes to use for indexing.
Example usage:
```
THREADS=20 ./warc-indexer.sh warcs1
```

This will start indexing files from the warcs1 folder using 20 threads. Assigning a higher number of threads than CPU
cores available will result in slower indexing.  Each indexing job require 1GB ram, so this can also be a limiting factor.

The script keeps track of processed files by checking if a log from a previous analysis is available. The logs are stored
in the `status`-folder (this can be changed using the `STATUS_ROOT` variable). To re-index a WARC file, delete the
corresponding log file.

The script `warc-indexer.sh` is not available for Windows. For that platform only a more primitive script is provided that also works for Linux/MacOs.
1. Copy ARC/WARC files into folder: `indexing/warcs1`  
2. Start indexing:  call `indexing/batch_warcs1_folder.sh` (or batch_warcs1_folder.bat for windows)


Indexing can take up to 20 minutes for 1GB warc-files. After indexing, the warc-files must stay in the same folder since SolrWayback is using them during playback etc.  

Having whitespace characters in WARC file names can result in pagepreviews and playback not working on some systems.
There can be up to 5 minutes delay before the indexed files are visible from search. Visit this url after index job have finished to commit them instantly: http://localhost:8983/solr/netarchivebuilder/update?commit=true  
There is a batch_warcs2_folder.sh similar script to show how to easily add new WARC files to the collection without indexing the old ones again.

For more information about the warc-indexer see: https://github.com/ukwa/webarchive-discovery/wiki/Quick-Start

## Scaling and using SolrWayback in production environment.
The stand alone Solr-server and indexing workflow using warc-indexer.sh can scale up to 20000 WARC files of size 1GB. Using 20 threads
indexing a collection of this size  can take up to 3 weeks.  This will result in
an index about 1TB having 500M documents and this will require to changing the Solr memory allocation to at least 12GB.
Storing the index on a SSD drive is required to reach acceptable performance for searches.
For collections larger than this limit Solr Cloud is required instead of the stand alone Solr that comes with the SolrWayback Bundle.
A more advanced distributed indexing flow can handled by the archon/arctika index workflow. See: https://github.com/netarchivesuite/netsearch 

### Faster indexing
A powerful laptop can handle up to 8 simultaneous indexing processes with Solr running on the same laptop. 
Using an SSD for the Solr-index will speed up indexing and also improve search/playback performance drastically.

# solr-ipfs
