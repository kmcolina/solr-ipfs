import os



#levando el demonio de ipfs
#es necesario prender el daemon
#os.system("ipfs daemon")

#si existe el archivo borrarlo

os.system("rm solrvalues.json")

#conexion a solr
os.system("curl 'http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json' -o solrvalues.json")

#almaceno en ipfs el archivo
os.system("echo sending file indexed to ipfs ")
os.system( "ipfs add solrvalues.json >> MyroutesIpfs.txt")
os.system("echo saved on ipfs, open local routes files ")
os.system( "cat MyroutesIpfs.txt")