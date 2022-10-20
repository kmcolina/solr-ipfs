package dk.kb.netarchivesuite.solrwayback.normalize;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import org.junit.Test;

import dk.kb.netarchivesuite.solrwayback.UnitTestUtils;
import dk.kb.netarchivesuite.solrwayback.facade.Facade;
import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntry;


public class PuneEncodeAndNormaliseTest extends UnitTestUtils {
       
    @Test
    public void testPunyEncodingAndNormalize() throws Exception {
        
        String url="http://www.test.dk/ABC.cfm?value=27";        
        String urlPunyNorm= Facade.punyCodeAndNormaliseUrl(url);
        assertEquals("http://test.dk/abc.cfm?value=27", urlPunyNorm);
        
        
        url="http://test.dk/abc.cfm?value=27&value2=Z.Y";
        urlPunyNorm= Facade.punyCodeAndNormaliseUrl(url);
        assertEquals("http://test.dk/abc.cfm?value=27&value2=z.y", urlPunyNorm);
        
        
        url="http://pølser.dk/pølseguf.html?pølse=Medister";
        urlPunyNorm= Facade.punyCodeAndNormaliseUrl(url);
        assertEquals("http://xn--plser-vua.dk/pølseguf.html?pølse=medister", urlPunyNorm);
        
    
    }
   }
