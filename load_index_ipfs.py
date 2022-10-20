import http.client
import json
import sys
import requests
import os
import json


#si existe el archivo borrarlo
os.system("rm solrvalues.json")
#si existe el archivo borrarlo
os.system("rm myfile.json")


os.system("curl 'http://host.docker.internal:8983/solr/discovery/select?indent=on&q=*:*&wt=json' -o solrvalues.json")

# Opening JSON file
file = open('solrvalues.json') 
# returns JSON object as 
# a dictionary
data = json.load(file)
# Closing file
file.close()

print(data)
aux = data

aux1 = json.dumps(aux)
file = open('myfile.json', 'w+', encoding="utf-8")
file.write(aux1)
file.close()

os.system( "ipfs add myfile.json >> MyroutesIpfs.txt")
os.system( "cat MyroutesIpfs.txt")

