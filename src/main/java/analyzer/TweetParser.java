package analyzer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.tweet.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TweetParser {

    private static Logger LOGGER = LoggerFactory.getLogger(TweetParser.class);

    public static Tweet parseTweet(String rawTweet) {
        Tweet tweet = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
           tweet = objectMapper.readValue(rawTweet, Tweet.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("can't parse tweet " + rawTweet);
        }
        return tweet;
    }

    public Tweet parseTweetWrapper(String rawTweet) {
        return parseTweet(rawTweet);
    }
}
