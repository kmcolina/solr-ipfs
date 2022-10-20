#usar un compose diferente 

docker-compose -f docker-compose-ubuntu-solr.yml up 


#genero un nueva imagen desde un contenedor 

docker commit kind_perlman ubuntu-python-ipfs

kind_perlman sale del docker ps -a  la ultima columna names


docker commit app_populate_1 solr-populate
docker commit app_solr_1 solr-master

docker commit solrwayback-ipfs-ubuntupython-1 kmcolina/ubuntu-python-ipfs

#conocer version de ubuntu que estoy usando

lsb_release -a

actual delc ontendedor: DISTRIB_DESCRIPTION="Ubuntu 22.04 LTS"


#install curl on ubuntu 
#first update system
apt-get update
apt install curl

#probando obtener el indexado de solr

    #ruta de solr formato json query all 

    http://localhost:18983/solr/discovery/select?indent=on&q=*:*&wt=json

#obtener el contenido indexado en solr

curl 'http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json' -o solrIndex4.json

#cargar en ipfs el file json del indexado 

ipfs add solrIndex1.json > Myroutes1.txt


#guardo las urls

#ver archivo de ipfs 
#funciona con el contenedor down

https://ipfs.io/ipfs/QmXNmUgMeJd4KHepoRAkhxaf2QPUtzQRrfswwSoaV9UKfC

added QmNzYXtpALHeHm2AADNAc1BffjXf3BXevxJmafzYG4KN7e dae.txt
https://ipfs.io/ipfs/QmTFZmt5uYX6EjEQGFpcmqEhYbNFDt9DtiWrktGxf93YT7

##algunas rutas ipfs - puede que no funcione alguna 

# cat Myroutes.txt
added QmXqdRCwk8Gs8sTYjhsUbbcPQAi57jYqeYePhNSbMFx2sK solrIndex.json
# cat Myroutes1.txt
added QmZed39qaDwhZhEUj8A4Gh6mXiGc49EZiWGYLcXqgePeri solrIndex1.json