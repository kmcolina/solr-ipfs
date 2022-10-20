import http.client
import json
import sys
import requests
import os
import json



#conexion a solr
connection = http.client.HTTPConnection("localhost",8983)

headers = {'Content-type': 'application/json'}

#foo = {'add': {'doc': {'id': "leid", 'compName_s': "compo"}, 'boost': 1, 'overwrite': False, 'commitWithin': 1000}}

#descargo el archivo que esta en solr
#ruta solr 
#'http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json'

os.system("curl 'https://ipfs.io/ipfs/QmaHPaQEbNVim1Bev9ndfcwGTrkC8VCK7GEAv7R9vaLF4Z'")
#obtengo la info
os.system("curl 'http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json' -o solrIndexP.json")


#leo el archivo

# Opening JSON file
file = open('solrIndexP.json') 
# returns JSON object as 
# a dictionary
data = json.load(file)
# Closing file
f.close()

print(data)


out_file = open("solrIndexP1.json", "w")
  
json.dump(data , out_file)
  
out_file.close()

print(out_file)



#elimino la info no deseada
my_diccy ="soy un texto de prueba dentro del archivo con w+ utf88 y un rezo"

file = open('myfile.txt', 'w+', encoding="utf-8")

#write string to file
file.write(my_diccy)
 
#close file
file.close()

my_diccy ="desde el python soy un texto de prueba dentro del archivo con w+ utf88 y un rezo"
command = 'echo probando la var '+ str(my_diccy) + ' > tt.txt'

os.system(command)

"curl 'http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json' -o solrout.json"

os.system("echo soy un texto de prueba dentro del archivo con w+ utf88 y un rezo py > ppy.txt")
#guardo el archivo

out_file = open("solrIndexP1.txt", "w")
  
json.dump(my_diccy , out_file,indent=2)
  
out_file.close()

print(out_file)

#ejecuto en el bash el ipfs 

os.system("echo sending file indexed to ipfs ")
##con este comando no reescribo la data del archivo
os.system( "ipfs add tt.txt >> MyroutesIpfs.txt")
os.system("echo saved on ipfs, open local routes files ")
os.system( "cat MyroutesIpfs.txt")





my_diccy =""
command = 'echo qqqqq probando la var desde el python soy un texto de prueba dentro del archivo con w+ utf88 y un rezo > tt.txt'

os.system(command)
