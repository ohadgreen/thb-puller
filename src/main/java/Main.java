import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.ConsumerInit;
import model.TwitterPullTask;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter.TwitterPullJobExecutor;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String CONSUMER_GROUP = "twitter.1";

    public static void main(String[] args) throws JsonProcessingException {
        Main main = new Main();
        main.runTwitterJobsConsumer();
    }

    private void runTwitterJobsConsumer() throws JsonProcessingException {
        int threadsCount = Integer.parseInt(System.getenv("THREAD_POOL_SIZE"));
        ExecutorService threadExecutor = Executors.newFixedThreadPool(threadsCount);

        ConsumerInit twitterJobsConsumer = new ConsumerInit(CONSUMER_GROUP);
        KafkaConsumer<String, String> twitterJobsKafkaConsumer = twitterJobsConsumer.initConsumer();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("twitter consumer starting...");

        while (true) {
            ConsumerRecords<String, String> consumerRecords = twitterJobsKafkaConsumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> rec : consumerRecords) {
                TwitterPullTask twitterPullTask = objectMapper.readValue(rec.value(), TwitterPullTask.class);
                LOGGER.info("Key {} - Partition {} - Offset {} ", rec.key(), rec.partition(), rec.offset());
                LOGGER.info("processing {} ", twitterPullTask);

                TwitterPullJobExecutor twitterPullJobExecutor = new TwitterPullJobExecutor(new LinkedBlockingQueue<>(10000), twitterPullTask);
                threadExecutor.execute(twitterPullJobExecutor);

                twitterJobsKafkaConsumer.commitSync();
            }
        }
    }
}
