package twitter;

import com.twitter.hbc.core.Client;
import model.tweet.Tweet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class TwitterClientInitTest {

    private final String SEARCH_TERM1 = "basketball";
    private final String SEARCH_TERM2 = "NBA";
    private TwitterClientInit twitterClientInit;
    Client twitterClient;
    BlockingQueue<String> queue;

    @BeforeEach
    public void setUp() {
        queue = new LinkedBlockingQueue<>(100);
        twitterClientInit = new TwitterClientInit(queue, Arrays.asList(SEARCH_TERM1, SEARCH_TERM2));
        twitterClient = twitterClientInit.clientInit();
    }

    @Test
    public void twitterSimpleTest() {
        for (int msgRead = 0; msgRead < 10; msgRead++) {
            String msg = null;
            try {
                msg = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            Tweet tweet = JsonParse.parseStringToTweet(msg);
            System.out.println("@@@ raw tweet: " + msg);
        }
        twitterClient.stop();
        queue.clear();
    }
}