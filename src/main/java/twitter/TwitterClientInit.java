package twitter;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class TwitterClientInit {
    private Logger LOGGER = LoggerFactory.getLogger(TwitterClientInit.class);

    private Client twitterClient;
    private final BlockingQueue queue;
    private final List searchTermList;

    public TwitterClientInit(BlockingQueue queue, List searchTermList) {
        this.queue = queue;
        this.searchTermList = searchTermList;
    }

    public Client clientInit() {
        Authentication oAuth1 = new OAuth1(System.getenv("TWITTER_CONSUMER_KEY"), System.getenv("TWITTER_CONSUMER_SECRET"), System.getenv("TWITTER_TOKEN"), System.getenv("TWITTER_SECRET"));

        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        endpoint.trackTerms(this.searchTermList);

        twitterClient = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(oAuth1)
                .processor(new StringDelimitedProcessor(this.queue))
                .build();

        // Establish a connection
        twitterClient.connect();
        LOGGER.info("Established Connection to Twitter API");

        return twitterClient;
    }

    public long printTweets(int tweetsCount){
        Instant twitterPullStart = Instant.now();
        for (int i = 0; i < tweetsCount; i++) {
            try {
                LOGGER.info(String.valueOf(queue.take()));
            } catch (InterruptedException e) {
                LOGGER.error("Twitter pull failure - " + e.getMessage());
                return 0l;
            }
        }

        Instant twitterPullEnd = Instant.now();
        long twitterPullElapsedTime = Duration.between(twitterPullStart, twitterPullEnd).toSeconds();
        return twitterPullElapsedTime;
    }

    public void stopClient() {
        twitterClient.stop();
    }
}
