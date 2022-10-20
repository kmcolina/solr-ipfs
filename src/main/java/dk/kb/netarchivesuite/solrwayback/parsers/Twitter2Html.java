package dk.kb.netarchivesuite.solrwayback.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dk.kb.netarchivesuite.solrwayback.facade.Facade;
import dk.kb.netarchivesuite.solrwayback.parsers.json.Tweet;
import dk.kb.netarchivesuite.solrwayback.parsers.json.TweetEntity;
import dk.kb.netarchivesuite.solrwayback.parsers.json.TweetHashtag;
import dk.kb.netarchivesuite.solrwayback.parsers.json.TweetMedia;
import dk.kb.netarchivesuite.solrwayback.parsers.json.TweetMention;
import dk.kb.netarchivesuite.solrwayback.parsers.json.TweetURL;
import dk.kb.netarchivesuite.solrwayback.parsers.json.TweetUser;
import dk.kb.netarchivesuite.solrwayback.parsers.json.TweetVideoVariant;
import dk.kb.netarchivesuite.solrwayback.properties.PropertiesLoader;
import dk.kb.netarchivesuite.solrwayback.service.dto.ArcEntryDescriptor;
import dk.kb.netarchivesuite.solrwayback.service.dto.ImageUrl;
import dk.kb.netarchivesuite.solrwayback.service.dto.IndexDoc;
import dk.kb.netarchivesuite.solrwayback.service.dto.SearchResult;
import dk.kb.netarchivesuite.solrwayback.solr.NetarchiveSolrClient;
import dk.kb.netarchivesuite.solrwayback.util.SolrQueryUtils;
import dk.kb.netarchivesuite.solrwayback.util.TwitterParsingUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// TODO could use a hand in refactoring into more classes with separate responsibilities.
public class Twitter2Html {
    private static final Logger log = LoggerFactory.getLogger(Twitter2Html.class);

    private final String crawlDate;
    private final Tweet tweet;
    private final TweetUser mainUser;
    private final Tweet mainContentTweet;

    /**
     * Constructor.
     * Parses the given Twitter JSON into a Tweet object and initializes commonly used variables from the tweet.
     * @param twitterJSON The JSON from Twitter.
     * @param crawlDate Date of the crawl - used for Solr searches.
     * @throws JsonProcessingException If Jackson fails to process the given json into a Tweet object.
     */
    public Twitter2Html(String twitterJSON, String crawlDate) throws JsonProcessingException {
        this.crawlDate = crawlDate;
        ObjectMapper mapper = new ObjectMapper();

        // Ignore properties that are not found when parsing json - TODO consider worth using @JSONIgnore instead
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        tweet = mapper.readValue(twitterJSON, Tweet.class);
        mainUser = tweet.getUser();
        mainContentTweet = TwitterParsingUtils.getMainContentTweet(tweet);
    }

    /**
     * The main method for this class.
     * Builds all the html for a tweet by parsing the JSON with Jackson and inserting the parsed tweet data
     * in html tags that resemble the original tweet structure.

     * @return The html making up the tweet.
     * @throws IOException If Jackson fails in parsing the JSON or css-file is not found.
     */
    public String getHtmlFromJson() throws IOException {
        String cssFromFile = IOUtils.toString(
                Twitter2Html.class.getClassLoader().getResourceAsStream("twitter_playback_style.css"),
                StandardCharsets.UTF_8);
        String reactionsCss = getIconsCSS();
        String css = cssFromFile + reactionsCss;

        TweetUser mainContentAuthor = TwitterParsingUtils.getMainContentAuthor(tweet);

        List<String> contentAuthorProfileImageList = Collections.singletonList(mainContentAuthor.getProfileImageUrl());
        List<ImageUrl> contentAuthorProfileImageUrl = getImageUrlsFromSolr(contentAuthorProfileImageList);

        List<String> tweetImages = TwitterParsingUtils.getTweetImageURLStrings(mainContentTweet);
        List<ImageUrl> tweetImageUrls = getImageUrlsFromSolr(tweetImages);

        List<TweetMedia> tweetVideos = TwitterParsingUtils.getTweetVideos(mainContentTweet);

        String mainTextHtml = getFormattedTweetText(mainContentTweet);
        String tweetMedia = getVideosHtml(tweetVideos) + imageUrlsToHtmlTags(tweetImageUrls);

        String html =
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                  "<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'>" +
                  "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
                  "<title>" + getHeadTitle() + "</title>" +
                  "<style>" + css + "</style>" +
                "</head>" +
                "<body>" +
                  "<div id='wrapper'>" +
                    "<div class='tweet'>" +
                      (tweet.isRetweet() ? getRetweetHeader() : "") +
                      getAuthorHtml(contentAuthorProfileImageUrl, mainContentAuthor) +
                      "<div class='item date'>" +
                        "<div>" + mainContentTweet.getCreationDate() + "</div>" +
                      "</div>"+
                      (mainContentTweet.getInReplyToTweetId() == null ? "" : getReplyLine(
                              TwitterParsingUtils.getReplyMentions(mainContentTweet),
                              mainContentTweet.getInReplyToTweetId())) +
                      // Few edge cases contain no main text - e.g. if tweet is a reply containing only a quote
                      (mainTextHtml.isEmpty() ? "" : "<div class='item text'>" + mainTextHtml + "</div>") +
                      "<div class='media'>" + tweetMedia + "</div>" +
                      (mainContentTweet.hasQuote() ? getQuoteHtml() : "") +
                      "<div class='item bottom-container'>" +
                        "<div class='reactions'>" +
                          "<span class='icon replies'></span>" + // TODO: should probably be div (optimally img)
                          "<span class='number'>" + mainContentTweet.getReplyCount() + "</span>" +
                          "<span class='icon retweets'></span>" +
                          "<span class='number'>" + mainContentTweet.getRetweetCount() + "</span>" +
                          "<span class='icon quotes'></span>" +
                          "<span class='number'>" + mainContentTweet.getQuoteCount() + "</span>" +
                          "<span class='icon likes'></span>" +
                          "<span class='number'>" + mainContentTweet.getFavoriteCount() + "</span>" +
                        "</div>" +
                        "<span class='found-replies-text'>" + foundRepliesToTweet(mainContentTweet.getId()) + "</span>" +
                      "</div>" +
                    "</div>" +
                  "</div>" +
                "</body>" +
                "</html>";
        return html;
    }

    /**
     * Creates the CSS for the tweet reaction icons.
     * Because of the dynamic url path to the image location this is handled here instead of in the static css-file.
     * @return String containing css for the tweet reaction icons
     */
    private static String getIconsCSS() {
        String reactionIconsImageUrl = PropertiesLoader.WAYBACK_BASEURL + "images/twitter_sprite.png";
        return ".reactions span.replies {" +
                    "background: transparent url(" + reactionIconsImageUrl + ") no-repeat -145px -50px;}" + // Missing correct icon?
                ".reactions span.retweets {" +
                    "background: transparent url(" + reactionIconsImageUrl + ") no-repeat -180px -50px;}" +
                ".reactions span.likes {" +
                    "background: transparent url(" + reactionIconsImageUrl + ") no-repeat -145px -130px;}" +
                ".reactions span.quotes {" +
                    "background: transparent url(" + reactionIconsImageUrl + ") no-repeat -105px -50px;}" + // Missing correct icon
                "span.user-verified {" +
                    "background: transparent url(" + reactionIconsImageUrl + ") no-repeat -67px -130px;}"; // TODO: using temp icon atm
    }

    /**
     * Generates the html for the retweet header above a tweet stating the retweeter and their info etc.
     * @return html making up the retweet header
     */
    private String getRetweetHeader() {
        List<ImageUrl> profileImageUrl = new ArrayList<>();
        try {
            List<String> retweetProfileImage = Collections.singletonList(mainUser.getProfileImageUrl());
            profileImageUrl = getImageUrlsFromSolr(retweetProfileImage);
        } catch (Exception e) {
            log.warn("Failed getting profile image of retweeter with name '{}'", mainUser.getName());
        }
        String html =
                "<div class='retweet-author'>" +
                  "<div class='retweet-text-wrap'>" +
                    "<div class='user-wrapper'>" +
                      "<a href='" + SolrQueryUtils.createTwitterSearchURL("tw_user_id:" + mainUser.getId()) + "'>" +
                        "<h3>" + mainUser.getName() + " Retweeted</h3>" +
                      "</a>" +
                      makeUserCard(profileImageUrl, mainUser.getName(),
                              mainUser.getScreenName(), mainUser.getDescription(),
                              mainUser.getFriendsCount(), mainUser.getFollowersCount(),
                              mainUser.isVerified()) +
                    "</div>" +
                    "<div class='date'>&middot " + tweet.getCreationDate() + "</div>" +
                  "</div>" +
                  makeButtonLinkingToTweet(tweet.getRetweetedTweet().getId(), "View original tweet") +
                "</div>";
        return html;
    }

    /**
     * Creates the html for the author of some tweet text, i.e. main tweet author or quote tweet author.
     * @param profileImageUrl ImageUrl for the user's profile image.
     * @param user The tweet author/user.
     * @return Html string containing all the author-related part of the tweet.
     */
    private String getAuthorHtml(List<ImageUrl> profileImageUrl, TweetUser user) {
        return "<div class='author'>" +
                  "<div class='user-wrapper'>" +
                    "<a href='" + SolrQueryUtils.createTwitterSearchURL(
                    "tw_user_id:" + user.getId()) + "'>" +
                      "<div class='avatar'>" + imageUrlsToHtmlTags(profileImageUrl) + "</div>" +
                      "<div class='user-handles'>" +
                        "<h2>" + user.getName() + "</h2>" +
                        (user.isVerified() ? "<span class='user-verified'></span>" : "") +
                        "<h4>@" + user.getScreenName() + "</h4>" +
                      "</div>" +
                    "</a>" +
                    makeUserCard(profileImageUrl, user.getName(),
                            user.getScreenName(), user.getDescription(),
                            user.getFriendsCount(), user.getFollowersCount(),
                            user.isVerified()) +
                  "</div>" +
                "</div>";
    }

    /**
     * TODO should make an equivalent method to this for just getting a single image
     * Searches Solr for the provided images and returns the found ImageUrls, if any, in a list.
     * @param imageUrlStrings The image urls to search for in Solr.
     * @return List of the found ImageUrls
     */
    private List<ImageUrl> getImageUrlsFromSolr(List<String> imageUrlStrings) {
        ArrayList<ImageUrl> imageUrls = new ArrayList<>();
        try {
            String imagesSolrQuery = SolrQueryUtils.createQueryStringForUrls(imageUrlStrings);
            ArrayList<ArcEntryDescriptor> imageEntries = NetarchiveSolrClient.getInstance()
                    .findImagesForTimestamp(imagesSolrQuery, crawlDate);
            imageUrls = Facade.arcEntrys2Images(imageEntries);
        } catch (Exception e) {
            log.warn("Failed communication with Solr while trying to query for images '{}' in tweet '{}'",
                    imageUrlStrings, tweet.getId());
        }
        return imageUrls;
    }

    /**
     * Creates the html string for the given video objects.
     * @param tweetVideos List of video media.
     * @return String of concatenated video-tags.
     */
    private String getVideosHtml(List<TweetMedia> tweetVideos) {
        StringBuilder sb = new StringBuilder();

        for (TweetMedia video : tweetVideos) {
            try {
                buildVideoHtmlString(sb, video);
            } catch (Exception e) {
                log.warn("Failed communication with Solr while trying to query for video with URL '{}' in tweet '{}'",
                        video.getMediaUrl(), tweet.getId());
            }
        }
        return sb.toString();
    }

    /**
     * Fetches playback urls for the given media from Solr if possible and appends a video-tag to
     * the given StringBuilder with the fetched info (or empty strings if nothing found in index).
     * @param sb StringBuilder to append video-tag to.
     * @param video Video object parsed from tweet.
     * @throws Exception If communication with Solr fails while trying to query for video.
     */
    private void buildVideoHtmlString(StringBuilder sb, TweetMedia video) throws Exception {
        TweetVideoVariant bestVideo = TwitterParsingUtils.getBestBitrateVideoVariant(video);
        String bestVideoUrl = bestVideo.getUrl();
        String videoImageUrl = video.getMediaUrl();
        String videoSolrQuery = SolrQueryUtils.createQueryStringForUrl(bestVideoUrl);

        String videoDownloadUrl = "";
        ArcEntryDescriptor videoSolrEntry = NetarchiveSolrClient.getInstance().findVideo(videoSolrQuery);
        if (videoSolrEntry != null) {
            videoDownloadUrl = getDownloadUrl(videoSolrEntry.getSource_file_path(), videoSolrEntry.getOffset());
        }

        String videoPlaceholderImageUrl = "";
        List<ImageUrl> imageUrlsFromSolr = getImageUrlsFromSolr(Collections.singletonList(videoImageUrl));
        if (!imageUrlsFromSolr.isEmpty()) {
            videoPlaceholderImageUrl = imageUrlsFromSolr.get(0).getDownloadUrl();
        }

        String videoHtml = videoUrlsToHtmlTag(videoDownloadUrl, videoPlaceholderImageUrl);
        sb.append(videoHtml);
    }

    /**
     * Creates a video tag with the given video urls.
     * If the download URL is an empty string the video tag will visually just show as the placeholder image
     * given by the other URL, which most likely will be indexed.
     * @param downloadUrl Url pointing to indexed video source.
     * @param placeholderImageUrl Url pointing to placeholder image for video.
     * @return String of html video tag for playback of indexed video.
     */
    private String videoUrlsToHtmlTag(String downloadUrl, String placeholderImageUrl) {

        if (downloadUrl== null || downloadUrl.isEmpty()) {            
            return "<video controls poster='" + placeholderImageUrl + "'/>\n";
        }
        
        return "<video controls src='" + downloadUrl + "' poster='" + placeholderImageUrl + "'/>\n";
    }

    /**
     * Generates the content for the html title tag. Depends on if retweet or standard tweet.
     * @return String to put inside <title/> tag.
     */
    private String getHeadTitle() {
        String titlePrefix = tweet.isRetweet() ? "Retweet by: " : "Tweet by: ";
        return titlePrefix + mainUser.getName() + " (userID: " + mainUser.getId() + ")";
    }

    /**
     * TODO maybe make links for URLs, hashtags and mentions in user description (need to be done by regex)
     * Method for creating the html for a user card - i.e. the little box with user info shown on hover over a user
     * @param profileImageUrl List containing the profile image url. Should only contain one element.
     * @param userName Username for user.
     * @param userHandle User handle/screen name.
     * @param description User description found in their profile.
     * @param followingCount User following count.
     * @param followersCount User followers count.
     * @param verified Boolean specifying if user is verified.
     * @return html for user card
     */
    private String makeUserCard(List<ImageUrl> profileImageUrl, String userName, String userHandle,
                                       String description, int followingCount, long followersCount, boolean verified) {
        return "<div class='user-card'>" +
                  "<div class='author'>" +
                    "<div class='avatar'>" + imageUrlsToHtmlTags(profileImageUrl) + "</div>" +
                    "<div class='user-handles'>" +
                      "<h2>" + userName + "</h2>" +
                      (verified ? "<span class='user-verified'></span>" : "") +
                      "<h4>@" + userHandle +"</h4>" +
                    "</div>" +
                  "</div>" +
                  "<span class='item user-desc'>" + description + "</span>" +
                  "<div class='follow-info'>" +
                    "<div class='following'>" +
                      "<span class='follow-num'>" + followingCount + "</span>" +
                      "<span> Following</span>" +
                    "</div>" +
                    "<div class='followers'>" +
                      "<span class='follow-num'>" + followersCount + "</span>" +
                      "<span> Followers</span>" +
                    "</div>" +
                  "</div>" +
                "</div>";
    }

    /**
     * Converts a list of image URLs to a string of html img tags.
     * @param images Images to make html for.
     * @return String of concatenated img tags.
     */
    private String imageUrlsToHtmlTags(List<ImageUrl> images){
        StringBuilder sb = new StringBuilder();
        for (ImageUrl image : images){
            sb.append("<img src='")
                    .append(image.getDownloadUrl())
                    .append("'/>\n");
        }
        return sb.toString();
    }

    /**
     * Creates the html for a button linking to a SolrWayback search for a tweet.
     * Empty string is returned if exception is thrown.
     * @param tweetID ID of the tweet to search for.
     * @param buttonText Explanatory text to put on button.
     * @return Html for a button linking to search for a tweet or empty string
     */
    private static String makeButtonLinkingToTweet(String tweetID, String buttonText) {
        String buttonHTML = "";
        try {
            SearchResult searchResult = Facade.search("tw_tweet_id:" + tweetID, null);
            List<IndexDoc> results = searchResult.getResults();
            if (results.isEmpty()) {
                return buttonHTML;
            }

            IndexDoc tweet = results.get(0);
            String filePath = tweet.getSource_file_path();
            long fileOffset = tweet.getOffset();

            buttonHTML =
                    "<div class='button-wrap'>" +
                      "<a href='" + getPlaybackUrl(filePath, fileOffset) + "'>" +
                        "<button type='button'>" + buttonText + "</button>" +
                      "</a>" +
                    "</div>";
        } catch (Exception e) {
            log.warn("Failed solr search for tweet '{}'", tweetID, e);
        }
        return buttonHTML;
    }

    /**
     * Builds the tweet playback url from the given source file path and its offset. TODO move to some utils class
     * @param sourcePath File path to warc file containing the content to show.
     * @param fileOffset Offset to look at in the file.
     * @return The full wayback url for playback.
     */
    private static String getPlaybackUrl(String sourcePath, long fileOffset) {
        return PropertiesLoader.WAYBACK_BASEURL +
                "services/viewForward?source_file_path=" + sourcePath + "&offset=" + fileOffset;
    }

    /**
     * Builds the resource download-url from a given source file path and its offset. TODO move to some utils class
     * @param sourcePath File path to warc file containing the content to show.
     * @param fileOffset Offset to look at in the file.
     * @return The full wayback url for playback.
     */
    private static String getDownloadUrl(String sourcePath, long fileOffset) {
        return PropertiesLoader.WAYBACK_BASEURL +
                "services/downloadRaw?source_file_path=" + sourcePath + "&offset=" + fileOffset;
    }

    /**
     * Creates the html specific for a reply tweet. This consists of reply mentions (people being replied to) with
     * links to SolrWayback searches for the individual users and a button that links to the tweet being replied to.
     * @param replyMentions List of people being replied to.
     * @param replyToTweetID ID for the tweet being replied to.
     * @return Html for the reply line in a reply tweet.
     */
    private static String getReplyLine(List<TweetMention> replyMentions, String replyToTweetID) {
        // TODO collapse on more than 3 mentions
        List<String> replyMentionsHTML = new ArrayList<>();
        for (TweetMention replyMention : replyMentions) {
            String replyScreenName = replyMention.getScreenName();
            String replyMentionHTML = makeMentionHtml(replyScreenName);
            replyMentionsHTML.add(replyMentionHTML);
        }

        Iterator<String> iterator = replyMentionsHTML.iterator();
        String firstMentionHTML = iterator.next();
        StringBuilder replyTagsHTML = new StringBuilder(firstMentionHTML);
        while (iterator.hasNext()) {
            String mention = iterator.next();
            if (!iterator.hasNext()) {
                replyTagsHTML.append(" and ").append(mention);
            } else {
                replyTagsHTML.append(" ").append(mention);
            }
        }

        String replyHTML =
                "<div class='reply-line'>" +
                  "<div class='reply-line-text'>" +
                    "Replying to " +
                    "<div class='reply-tags'>" +
                      replyTagsHTML +
                    "</div>" +
                  "</div>" +
                  makeButtonLinkingToTweet(replyToTweetID, "View tweet replied to") +
                "</div>";
        return replyHTML;
    }

    /**
     * Creates the html for a mention in a tweet.
     * This html will contain a link to search SolrWayback for tweets by the mentioned user or other mentions of them.
     * @param mentionScreenName The mention to make html for.
     * @return A string containing the html for the mention
     */
    private static String makeMentionHtml(String mentionScreenName) {
        String searchString = "(author:" + mentionScreenName + " OR tw_user_mentions:"
                    + mentionScreenName.toLowerCase() + ")";
        String searchUrl = SolrQueryUtils.createTwitterSearchURL(searchString);
        return "<span><a href='" + searchUrl + "'>@" + mentionScreenName + "</a></span>";
    }

    /**
     * Creates the html for a hashtag in a tweet.
     * This html will contain a link to search SolrWayback for other tweets containing the hashtag.
     * @param hashtagText The hashtag to make html for.
     * @return A string containing the html for the hashtag
     */
    private static String makeHashtagHtml(String hashtagText) {
        String searchString = "keywords%3A" + hashtagText;
        String searchUrl = SolrQueryUtils.createTwitterSearchURL(searchString);
        return "<span><a href='" + searchUrl + "'>#" + hashtagText + "</a></span>";
    }

    /**
     * Creates the html for a URL in a tweet.
     * This simply makes a link tag for the URL, so the link in the tweet is clickable.
     * @param tweetURL The URL to make html for.
     * @return A string containing the html for the tag
     */
    private String makeURLHtml(TweetURL tweetURL) {
        String expandedUrl = tweetURL.getExpandedUrl();
        if (expandedUrl.equals(tweet.getQuotePermalink())) { // Insert nothing if URL is the quote-URL
            log.debug("Ignored quote permalink '{}' while inserting url html", tweet.getQuotePermalink());
            return "";
        }
        String displayUrl = tweetURL.getDisplayUrl();
        return "<span><a href='" + expandedUrl + "'>" + displayUrl + "</a></span>";
    }

    /**
     * Creates the html for a quote as part of a tweet.
     * @return All the html for a quote as a string
     */
    private String getQuoteHtml() {
        Tweet quote = mainContentTweet.getQuotedTweet();
        TweetUser quotedUser = quote.getUser();
        List<String> rawQuoteProfileImageUrl = Collections.singletonList(quotedUser.getProfileImageUrl());
        List<String> rawQuoteImageUrls = TwitterParsingUtils.getTweetImageURLStrings(quote);
        List<TweetMedia> quoteVideos = TwitterParsingUtils.getTweetVideos(quote);

        List<ImageUrl> quoteProfileImage = new ArrayList<>();
        String quoteMediaHtml = "";
        try {
            quoteProfileImage = getImageUrlsFromSolr(rawQuoteProfileImageUrl);

            List<ImageUrl> quoteImages = getImageUrlsFromSolr(rawQuoteImageUrls);
            quoteMediaHtml = getVideosHtml(quoteVideos) + imageUrlsToHtmlTags(quoteImages);
        } catch (Exception e) {
            log.warn("Failed getting media for quote in tweet by '{}'", mainUser.getName(), e);
        }

        String quoteHtml =
                "<div class='quote'>" +
                  "<div class='author-container'>" +
                    getAuthorHtml(quoteProfileImage, quotedUser) +
                    makeButtonLinkingToTweet(quote.getId(), "View quote tweet") +
                  "</div>" +
                  "<div class='item date'>" +
                    "<div>" + quote.getCreationDate() + "</div>" +
                  "</div>" +
                  (quote.getInReplyToTweetId() == null ? "" : getReplyLine(
                          TwitterParsingUtils.getReplyMentions(quote), quote.getInReplyToTweetId())) +
                  "<div class='item text'>" +
                    getFormattedTweetText(quote) +
                  "</div>" +
                  "<div class='media'>" + quoteMediaHtml + "</div>" +
                "</div>";

        return quoteHtml;
    }

    /**
     * Formats the tweet text by inserting html links in place of hashtags, mentions and urls.
     * Also removes reply mentions from the text (these will always be in the start of the text outside 'display_text_range')
     * and removes trailing image urls (if tweet contains images the text will always contain an url linking to images).
     * @param tweet The tweet to format text for - can be 'main' tweet, retweeted tweet or quote
     * @return The formatted text string to show in playback.
     */
    public String getFormattedTweetText(Tweet tweet) {
        String contentText = TwitterParsingUtils.getContentText(tweet);
        int tweetTextStartIndex = TwitterParsingUtils.getDisplayTextRangeMin(tweet);
        StringBuilder textBuilder = new StringBuilder(contentText);
        replaceTextEntitiesAtIndices(textBuilder, tweet);

        String htmlText = newline2Br(textBuilder.toString());
        textBuilder = new StringBuilder(htmlText);
        textBuilder.replace(0, tweetTextStartIndex, ""); // Remove reply mentions if any
        contentText = textBuilder.toString();
        contentText = contentText.replaceFirst("https:\\/\\/t\\.co\\/[a-zA-Z0-9]{10}$", ""); // Remove trailing image-URL if any
        return contentText;
    }

    /**
     * Replaces the entities (hashtags/mentions/urls) in the tweet text by using the direct indices of the entities.
     * This is done backwards through the text, end-to-start, to avoid having to keep track of an index offset while
     * replacing the entities.
     * @param textBuilder StringBuilder containing the text to replace the tags in.
     * @param tweet The tweet to grab entities and text from.
     */
    private void replaceTextEntitiesAtIndices(StringBuilder textBuilder, Tweet tweet) {
        String contentText = TwitterParsingUtils.getContentText(tweet);
        List<? extends TweetEntity> sortedEntities = TwitterParsingUtils.getSortedEntitiesForInsertion(tweet);

        try {
            for (TweetEntity entity : sortedEntities) {
                String entityHtml = getEntityHtml(entity);
                Pair<Integer, Integer> indices = entity.getIndices();
                int startIndex = indices.getLeft();
                int endIndex = indices.getRight();
                // Use indices with offset to avoid emojis ruining everything
                int offsetStartIndex = contentText.offsetByCodePoints(0, startIndex);
                int offsetEndIndex = contentText.offsetByCodePoints(0, endIndex);
                log.debug("Inserting '{}' at indices {},{}", entityHtml, offsetStartIndex, offsetEndIndex);
                textBuilder.replace(offsetStartIndex, offsetEndIndex, entityHtml);
            }
        } catch (Exception e) { // Shouldn't happen
            log.warn("Failed replacing raw tags with solr search links", e);
        }
    }

    /**
     * Makes the html for the different types of TweetEntity to be inserted in text.
     * @param entity Entity to make html for.
     * @return String of html ready for insertion in text.
     */
    private String getEntityHtml(TweetEntity entity) {
        String entityHTML;
        if (entity instanceof TweetMention) {
            String mentionScreenName = ((TweetMention) entity).getScreenName();
            entityHTML = makeMentionHtml(mentionScreenName);
        } else if (entity instanceof TweetHashtag) {
            String hashtagText = ((TweetHashtag) entity).getText();
            entityHTML = makeHashtagHtml(hashtagText);
        } else { // instanceof TweetURL
            entityHTML = makeURLHtml((TweetURL) entity);
        }
        return entityHTML;
    }

    /**
     * Converts '\n' to the corresponding '<br>' tags in html
     * @param text Text to convert.
     * @return New string with newline characters replaced by <br>
     */
    private String newline2Br(String text) {
        if (text == null){
            return "";
        }
        return text.replace("\n","<br>");
    }

    /**
     * Searches Solr for replies to the tweet given by the tweet ID and returns a string stating if any replies
     * were found. If any replies were found, it will also contain a link to search for the replies in SolrWayback.
     * @param tweetID ID of a tweet
     * @return String stating if any replies were found.
     */
    private String foundRepliesToTweet(String tweetID) {
        String foundRepliesLine = "";
        try {
            SearchResult searchResult = Facade.search("tw_reply_to_tweet_id:" + tweetID, null);
            long resultCount = searchResult.getNumberOfResults();
            String searchLink = SolrQueryUtils.createTwitterSearchURL("tw_reply_to_tweet_id:" + tweetID);
            if (resultCount > 0) {
                log.info("Found replies to tweet id {}.. {}", tweetID, resultCount);
                foundRepliesLine = "Found <a href='" + searchLink + "'>" + resultCount + "</a> replies";
            } else {
                log.info("Didn't find any replies to tweet {}", tweetID);
                foundRepliesLine = "No replies found to tweet";
            }
        } catch (Exception e) {
            log.warn("Error while trying to find replies for tweet " + tweetID);
        }
        return foundRepliesLine;
    }
}
