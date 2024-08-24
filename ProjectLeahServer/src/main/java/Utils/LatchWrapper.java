package Utils;

import java.util.concurrent.CountDownLatch;

public class LatchWrapper {
    CountDownLatch latch;
    public void setLatch(int count) {
        latch = new CountDownLatch(count);
    }

    public void countDownLatch() {
        latch.countDown();
    }
    public CountDownLatch getLatch() {
        return latch;
    }
    public void awaitLatch() throws InterruptedException {
        latch.await();
    }
}
