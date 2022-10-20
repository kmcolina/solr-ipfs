package dk.kb.netarchivesuite.solrwayback.parsers;

import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ArcWarcFileParserAbstract {
  private static final Logger log = LoggerFactory.getLogger(ArcWarcFileParserAbstract.class);


  public static int getStatusCode(String line){//HTTP/1.1 302 Object moved      
    String[] tokens = line.split(" ");
    String status = tokens[1];
    return Integer.parseInt(status);     
    
  }

  protected static void loadBinary(InputStream is, ArcEntry arcEntry) throws IOException {
      long binarySize = arcEntry.getBinaryArraySize();

      if (binarySize > Integer.MAX_VALUE) {
          String message = "Binary size too large for java byte[]. Size:" + binarySize +
                           ", source='" + arcEntry.getArcSource().getSource() + "'";
        log.error(message);
        throw new IllegalArgumentException(message);
      }

      byte[] bytes = new byte[(int) binarySize];

      // we are not using IOUtils.readFully as we'd rather return non-complete data than nothing
      int read = IOUtils.read(is, bytes);
      if (read == -1) {
          log.warn("Attempted to load binary for {}#{} but got EOF immediately",
                   arcEntry.getArcSource().getSource(), arcEntry.getOffset());
      } else if (read < bytes.length) {
          log.warn("Incomplete binary for {}#{}: {}/{} bytes read",
                   arcEntry.getArcSource().getSource(), arcEntry.getOffset(), read, bytes.length);
      }
      arcEntry.setBinary(bytes);
  }

}
