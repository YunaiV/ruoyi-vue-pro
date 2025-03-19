package cn.iocoder.yudao.framework.common.util.general;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.time.Duration;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Limiter {

    private Integer counter = 0;
    private Integer limit = Integer.MAX_VALUE;
    private Integer deduct;
    private Timer timer;

    private Bucket bucket = null;

    // Constructor to set custom limit and calculate deduct value
    public Limiter(Integer limit) {
        this.limit = limit;
        this.deduct = calculateDeduct(limit);
        Bandwidth bandwidth = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder().addLimit(bandwidth).build();
    }

    // Method to calculate deduct value based on the limit
    private Integer calculateDeduct(Integer limit) {
        // Ensure that the counter decrements limit times per minute
        // 60,000 milliseconds in a minute divided by limit gives the interval
        return 60000 / limit;
    }



    // Method to wait until counter is below the limit
    public synchronized void tryAcquire() {
        bucket.tryConsume(1);
    }

    // Method to wrap a code block and return the result
    public <T> T executeWithLimiter(Supplier<T> codeBlock) {
        tryAcquire();
        return codeBlock.get();
    }

    // Method to increment the counter by 1
    public synchronized void incrementCounter() {
        counter++;
    }

    // Method that decreases the counter by 1 every calculated interval
    private synchronized void decrementCounter() {
        if (counter > 0) {
            counter--;
        }
    }

    // Method to start the timer for scheduled decrement
    private void startTimer() {
        timer = new Timer(true);
        // decrease counter with a period of {{deduct}} ms
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                decrementCounter();
            }
        }, deduct, deduct);
    }

    // Method to stop the timer when it's no longer needed
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
