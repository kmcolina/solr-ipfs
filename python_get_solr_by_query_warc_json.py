import http.client
import json
import sys
import requests
import os



#conexion a solr
os.system("curl 'http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json' -o solrvalues.json")


#leo el archivo

# Opening JSON file
file = open('solrvalues.json') 
# returns JSON object as 
# a dictionary
data = json.load(file)
# Closing file
f.close()

print(data)


aux = data

#bytes to string
aux1 = json.dumps(aux)
#genero un archivo para enviar a ipfs 
#open text file
text_file = open("/solrvalues1.json", "w")
 
#write string to file
text_file.write(aux1)
 
#close file
text_file.close()


#ejecuto en el bash el ipfs 

os.system("echo sending file indexed to ipfs ")
##con este comando no reescribo la data del archivo
os.system( "ipfs add solrvalues1.json >> MyroutesIpfs.txt")
os.system("echo saved on ipfs, open local routes files ")
os.system( "cat MyroutesIpfs.txt")









##otra forma haciendi find 
z1 = '"docs":[\n'
index = aux1.find(z1) 
sal = aux1[index+9:]


#elimino el final haciendo find
fin = ']\n  }}\n'
indexF = sal.find(fin) 
sal1 = sal[:indexF]

#guardo el json indexado
jsonIndexado = sal1


#genero un archivo para enviar a ipfs 
#open text file
text_file = open("/myFile.json", "w")
 
#write string to file
text_file.write(jsonIndexado)
 
#close file
text_file.close()


#ejecuto en el bash el ipfs 

os.system("echo sending file indexed to ipfs ")
##con este comando no reescribo la data del archivo
os.system( "ipfs add myFile.json >> MyroutesIpfs.txt")
os.system("echo saved on ipfs, open local routes files ")
os.system( "cat MyroutesIpfs.txt")


