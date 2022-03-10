package twitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.ProducerInit;
import model.TweetToProcess;
import model.TwitterPullTask;
import model.tweet.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class TwitterPullJobExecutor implements Runnable {

    private final Logger LOGGER = LoggerFactory.getLogger(TwitterPullJobExecutor.class);
    private final BlockingQueue queue;
    private final TwitterPullTask twitterPullTask;

    public TwitterPullJobExecutor(BlockingQueue queue, TwitterPullTask twitterPullTask) {
        this.queue = queue;
        this.twitterPullTask = twitterPullTask;
    }

    @Override
    public void run() {
        TwitterClientInit twitterClient = new TwitterClientInit(this.queue, this.twitterPullTask.getSearchTerms());
        twitterClient.clientInit();
        ProducerInit kafkaProducer = new ProducerInit();
        kafkaProducer.initProducer();

        int relevantTweetsCount = 0;
        Set<String> relevantLanguagesSet = new HashSet<>(Arrays.asList("en", "iw"));

        ObjectMapper objectMapper = new ObjectMapper();
        while (relevantTweetsCount <= twitterPullTask.getRequestedNumber()) {
            try {
                String rawTweet = (String) queue.take();
                Tweet tweet = objectMapper.readValue(rawTweet, Tweet.class);

                if (relevantLanguagesSet.contains(tweet.getLang())) {
                    LOGGER.info((tweet.getText().length() >= 30) ? tweet.getText().substring(30) : tweet.getText());
                    relevantTweetsCount ++;
                    TweetToProcess tweetToProcess = new TweetToProcess(twitterPullTask, objectMapper.writeValueAsString(tweet), (relevantTweetsCount == twitterPullTask.getRequestedNumber()));
                    kafkaProducer.sendMessage(String.valueOf(twitterPullTask.getId()), objectMapper.writeValueAsString(tweetToProcess));
                }
            } catch (InterruptedException | JsonProcessingException e) {
                LOGGER.error(e.getMessage());
            }
        }

//        for (int i = 0; i < twitterPullTask.getRequestedNumber(); i++) {
//            try {
//                String rawTweet = (String) queue.take();
//                LOGGER.info(rawTweet);
//                kafkaProducer.sendMessage(String.valueOf(twitterPullTask.getId()), rawTweet);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        kafkaProducer.closeProducer();
//        printSomeTweets(10);
    }
}
