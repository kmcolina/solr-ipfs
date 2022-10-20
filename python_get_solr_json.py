import http.client
import json
import sys
import requests
import os



#conexion a solr
connection = http.client.HTTPConnection("localhost",8983)

headers = {'Content-type': 'application/json'}

#foo = {'add': {'doc': {'id': "leid", 'compName_s': "compo"}, 'boost': 1, 'overwrite': False, 'commitWithin': 1000}}

#descargo el archivo que esta en solr
#ruta solr 
#'http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json'



url ="http://host.docker.internal:18983/solr/discovery/select?indent=on&q=*:*&wt=json"
print ("la url es ")
print (url)

resp = requests.get(url,headers=headers)
resp_content = resp.content
print(resp_content)


#elimino lo que no hace falta del archivo

""" {
  "responseHeader":{
    "status":0,
    "QTime":0,
    "params":{
      "q":"*:*",
      "indent":"on",
      "wt":"json"}},
  "response":{"numFound":272,"start":0,"docs":[ 
    
    .................

    }]
  }}
    """

  #deberia quedar luego de limpiar 
  #{ ......... }    

#######
##efectivo para limpieza parte delantera de la lista

aux=resp_content

#bytes to string
aux1 = aux.decode("utf-8") 


##otra forma haciendi find 
z1 = '"docs":[\n'

z1 = '"response":{'

index = aux1.find(z1) 
#dejo el docs para mantener el formato correcto
sal = aux1[index:]

##inserto caracter inicia { para dar el formato json
sal ='{\n' + aux1[index:]


#elimino el final haciendo find
fin = '}\n'
indexF = sal.find(fin) 
sal1 = sal[:indexF]

#guardo el json indexado
jsonIndexado = sal1


#indexo a utf-8
jsonIndexado= jsonIndexado.encode('utf-8')


#genero un archivo para enviar a ipfs 
#open text file
file = open('myfile.txt', 'w+', encoding="utf-8")

#write string to file
text_file.write(jsonIndexado)
 
#close file
text_file.close()


#ejecuto en el bash el ipfs 

os.system("echo sending file indexed to ipfs ")
##con este comando no reescribo la data del archivo
os.system( "ipfs add myFile.txt >> MyroutesIpfs.txt")
os.system("echo saved on ipfs, open local routes files ")
os.system( "cat MyroutesIpfs.txt")




""" 

with open('jsonTest.json', 'r') as myfile:
    data=myfile.read()

# parse file
obj = json.loads(data)


#json_foo = json.dumps(foo)

foo1 = {'add': {'doc': obj, 'boost': 1, 'overwrite': False, 'commitWithin': 1000}}
json_foo1 = json.dumps(foo1)


#print(obj)
#print(json_foo )
#print(json_foo1 )


connection.request('POST', '/solr/demo/update?wt=json', json_foo1, headers)

response = connection.getresponse()
print(response.read().decode())

#run script
#python3 add_document.py "test_id" "Test Title!"

#output
#{"responseHeader":{"status":0,"QTime":8}}

 """