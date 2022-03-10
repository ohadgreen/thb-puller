package twitter;

import model.TwitterPullTask;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

class TwitterPullJobExecutorTest {

    @Test
    void run() {
        String searchTerm = "peace";
        List<String> searchTermList  = Collections.singletonList(searchTerm);
        TwitterPullTask tweeterPullTask = new TwitterPullTask(UUID.randomUUID(), "Tester", searchTermList, 300);

        TwitterPullJobExecutor twitterPullJobExecutor = new TwitterPullJobExecutor(new LinkedBlockingQueue<>(500), tweeterPullTask);
        twitterPullJobExecutor.run();

    }
}