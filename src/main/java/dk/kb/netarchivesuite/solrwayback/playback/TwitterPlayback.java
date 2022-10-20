package dk.kb.netarchivesuite.solrwayback.playback;

import dk.kb.netarchivesuite.solrwayback.parsers.ParseResult;
import dk.kb.netarchivesuite.solrwayback.parsers.Twitter2Html;
import dk.kb.netarchivesuite.solrwayback.parsers.WaybackToolbarInjecter;
import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntry;
import dk.kb.netarchivesuite.solrwayback.service.dto.IndexDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This is for JSON harvesting from Twitter only. For normal HTTP harvest, the HTMLPlayback is used.
 */
public class TwitterPlayback extends PlaybackHandler{

    private static final Logger log = LoggerFactory.getLogger(TwitterPlayback.class);

    public TwitterPlayback(ArcEntry arc, IndexDoc doc, boolean showToolbar){
        super(arc,doc,showToolbar);
    }

    @Override
    public ArcEntry playback() throws Exception{

        log.debug(" Generate twitter webpage from FilePath:" + doc.getSource_file_path() + " offset:" + doc.getOffset());
        // Fake html into arc.
        String encoding = "UTF-8"; // Why does encoding say ISO ? This seems to fix the bug
        String json = new String(arc.getBinary(), encoding);
        Twitter2Html parser = new Twitter2Html(json, arc.getCrawlDate());
        String html = parser.getHtmlFromJson();
        arc.setBinary(html.getBytes());
        arc.setContentType("text/html");
        ParseResult htmlReplaced = new ParseResult(); // Do not parse.
        htmlReplaced.setReplaced(html);
        String textReplaced = htmlReplaced.getReplaced(); // TODO count links found, replaced

        // Inject toolbar
        if (showToolbar) { // If true or null.
            textReplaced = WaybackToolbarInjecter.injectWaybacktoolBar(doc, htmlReplaced, false);
        }
        arc.setContentEncoding(encoding);
        arc.setBinary(textReplaced.getBytes(encoding)); // Can give error. Uses UTF-8 (from index) instead of ISO-8859-1

        return arc;
    }

}
