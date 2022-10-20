import http.client
import json
import sys
import requests
import os
import json


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


z1 = '"response"'

index = aux1.find(z1) 
#dejo el docs para mantener el formato correcto
sal = aux1[index:]

##inserto caracter inicia { para dar el formato json
sal ='{' + aux1[index:]


#elimino el final haciendo find
fin = '}\n'
fin = '}'
indexF = sal.rfind(fin) 
sal1 = sal[:indexF]

#guardo el json indexado
jsonIndexado = sal1
out_file = open("solrIndexaux.txt", "w")
  
json.dump(jsonIndexado , out_file)
  
out_file.close()

print(out_file)



os.system( "ipfs add solrIndexaux.txt >> MyroutesIpfs.txt")
os.system("echo saved on ipfs, open local routes files ")
os.system( "cat MyroutesIpfs.txt")



