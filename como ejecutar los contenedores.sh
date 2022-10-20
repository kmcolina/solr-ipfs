como ejecutar los contenedores 

#levantar todos los contenedores
docker-compose up 

#ejecutar en la terminal para levantar el demonio de ipfs
docker exec -it solrwayback-ipfs-ubuntupython-1 ipfs daemon

#ejecutar en la terminal para cargar el archivo indexado en ipfs
docker exec -it solrwayback-ipfs-ubuntupython-1 python3 load_index_ipfs.py




