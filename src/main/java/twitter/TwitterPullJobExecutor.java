package twitter;

import kafka.ProducerInit;
import model.TwitterPullTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.BlockingQueue;

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

        for (int i = 0; i < twitterPullTask.getRequestedNumber(); i++) {
            try {
                String rawTweet = (String) queue.take();
                LOGGER.info(rawTweet);
                kafkaProducer.sendMessage(String.valueOf(twitterPullTask.getId()), rawTweet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        kafkaProducer.closeProducer();
//        printSomeTweets(10);
    }

    private void printSomeTweets(int tweetsCount) {
        for (int i = 0; i < tweetsCount; i++) {
            try {
                LOGGER.info(String.valueOf(queue.take()));
            } catch (InterruptedException e) {
                LOGGER.error("Twitter pull failure - " + e.getMessage());
            }
        }
    }
}
