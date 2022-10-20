from flask import Flask, render_template, request
from urllib.request import urlopen
import simplejson

app = Flask(__name__)

BASE_PATH='http://solr:8983/solr/discovery/select?wt=json&df=name&rows=250&q='

@app.route('/', methods=["GET","POST"])
def index():
    query = None
    numresults = None
    aux = ''
    results = 0

    # get the search term if entered, and attempt
    # to gather results to be displayed
    if request.method == "POST":
        query = request.form["searchTerm"]

        # return all results if no data was provided
        if query is None or query == "":
            query = "*:*"
        else:
            #querynumber = int.from_bytes(query.encode('utf-8'), 'little')
            #aux = 'source_file:'+query+'*'+' OR '+ 'crawl_year:'+querynumber + ' OR ' + 'wayback_date:'+querynumber+'*'+' OR '+'resourcename:'+query+'*'+' OR '+'content_type:'+query+'*'+' OR '+'content:'+query+'*'
            aux = 'source_file:'+query+'*'+'%20OR%20'+'resourcename:'+query+'*'+'%20OR%20'+'content_type:'+query+'*'+'%20OR%20'+'content:'+query+'*'
            query= aux
            print(query)

        # query for information and return results
        connection = urlopen("{}{}".format(BASE_PATH, query))
        response = simplejson.load(connection)
        numresults = response['response']['numFound']
#valido si la respuesta es mayor a cero


        results = response['response']['docs']

    return render_template('index.html', query=query, numresults=numresults, results=results)

if __name__ == '__main__':
    app.run(host='0.0.0.0')