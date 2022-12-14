# Solr IPFS - indexing and search for AWV


## Build and test with Docker

* Run `docker-compose build` to build (or re-build) the container.
* Run `docker-compose up` to run the Solr-IPFS container along with a Solr instance

After running `docker-compose up` you should see logs from four services (`ubuntu-python-ipfs`, `solr`, `flask-app` and `populate`).

The Solr service should be available on port 8983, i.e. http://localhost:8983/solr/ 
The flask-app service for search interface should be available on port 500, i.e. http://localhost:5000
the populate service is which executes the populate script to index the warc files and send them to solr, after this the service stops automatically

## solr-ipfs
You must follow the following steps to make correct use of the services

### 1 - Run all containers
```
docker-compose up 
```
### 2- Run in terminal to start the ipfs daemon
```
docker exec -it solrwayback-ipfs-ubuntupython-1 ipfs daemon
```
### 3- Run in terminal to load solr indexed file into ipfs
```
docker exec -it solrwayback-ipfs-ubuntupython-1 python3 load_index_ipfs.py
```


###  INDEXING
Solr-IPFS uses a Solr index of WARC files development for 
United Kingdom Web Archive available on https://github.com/ukwa/webarchive-discovery/tree/master/warc-indexer

An implementation of how to use the indexer is available at https://github.com/ukwa/webarchive-discovery this was taken as a basis to develop the services used in the `docker-compose.yml` file

Example usage:
```
java -jar /jars/warc-indexer.jar -s ${SOLR_URL} /docker/test-warcs/TEST-20220304210400500-00000-80~h3w~8443.warc /docker/test-warcs/IAH-20080430204825-00000-blackbook.warc

```


the previous command is the one that is executed in the populate service, if you want to add new warc files for indexing it is necessary to copy them to the container using the docker commands. the file that must change is the `populate.sh`, it should be noted that it is also necessary to copy the warc file that you want to index inside the container

