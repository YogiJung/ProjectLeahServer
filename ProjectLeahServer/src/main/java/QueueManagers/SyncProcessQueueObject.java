package QueueManagers;

import Utils.BackPressure;
import Utils.ResponseFormat;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static Utils.FormatUtils.responseToByteBuf;

public class SyncProcessQueueObject<T> {
    String name;
    LinkedBlockingQueue<T> blockingQueue = new LinkedBlockingQueue<T>();
    ConcurrentLinkedQueue<T> concurrentQueue = new ConcurrentLinkedQueue<T>();
    public SyncProcessQueueObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void enQueue(T data) throws InterruptedException {

        // backPressureFlag 0 (receive), 1(stop), 2(Queue Clear)
        if (concurrentQueue.size() < 300) {
            concurrentQueue.offer(data);
        } else {
            BackPressure.backPressureFlag = 1;
            System.out.println("Concurrent Queue is full!");
        }

        if (BackPressure.backPressureFlag == 2 && name.equals("InitialQueue")) {
            concurrentQueue.clear();
        }
    }
    public void enQueue(T data, ChannelHandlerContext ctx) throws InterruptedException {

        // backPressureFlag 0 (receive), 1(stop)
        if (concurrentQueue.size() < 300) {
//            BackPressure.backPressureFlag = 0;
            concurrentQueue.offer(data);
        } else {
            System.out.println("Concurrent Queue is full");
            ctx.writeAndFlush(responseToByteBuf(new ResponseFormat("flowControl", 1)));
        }
    }
    public void putQueue(T data) throws InterruptedException {
        blockingQueue.put(data);
    }

    public T deQueue() {
        T data = concurrentQueue.poll();
        return data;
    }
//    public T deQueue(long timeout, TimeUnit timeUnit) throws InterruptedException {
//        return blockingQueue.poll(timeout, timeUnit);
//    }

    public T takeQueue() throws InterruptedException {
        return blockingQueue.take();
    }
    public void clearQueue() {
        concurrentQueue.clear();
    }

}
