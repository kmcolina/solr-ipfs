package dk.kb.netarchivesuite.solrwayback.export;

import java.io.PrintWriter;

import dk.kb.netarchivesuite.solrwayback.properties.PropertiesLoader;
import dk.kb.netarchivesuite.solrwayback.solr.SolrStreamingExportClient;

public class TestGenerateCSV {

    private static final String SOLR = "http://localhost:8983/solr/netarchivebuilder";

    public static void main(String[] args) throws Exception{
    
    PropertiesLoader.initProperties();
    
    String query = "thomas egense";
    String filter = null;

    String fields = "id, domain,  hash , links_images  ";
     SolrStreamingExportClient solr =  SolrStreamingExportClient.createCvsExporter(SOLR, query,fields, filter);

     StreamingSolrExportBufferedInputStream streamExport = new StreamingSolrExportBufferedInputStream(solr,100);
    
     PrintWriter writer = new PrintWriter("export.txt", "UTF-8");
            
     int read = streamExport.read(); 
    while (read != -1){
    //  System.out.print(Character.toString((char) read));
      writer.write(Character.toString((char) read));
      read=streamExport.read();
    }
    writer.close();
    streamExport.close();
    
  }

}
