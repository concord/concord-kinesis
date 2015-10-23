package com.concord.kinesis;

import com.concord.*;
import com.concord.swift.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.concord.kinesis.utils.Options;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.UncaughtExceptionHandlers;

public class Consumer extends Computation implements Runnable {
  private final ArrayBlockingQueue<Record> recordQueue;
  private final ArrayList<byte[]> ostreams = new ArrayList<byte[]>();
  private final String name;

  public Consumer(ArrayBlockingQueue<Record> rq, List<String> os, String name) {
    Preconditions.checkNotNull(rq);
    Preconditions.checkNotNull(os);
    Preconditions.checkNotNull(name);

    recordQueue = rq;
    for (String o : os) {
      ostreams.add(o.getBytes());
    }
    this.name = name;
  }

  @Override
  public void init(ComputationContext ctx) {
    ctx.setTimer("loop", System.currentTimeMillis());
  }

  @Override
  public void processTimer(ComputationContext ctx, String key, long time) {
    com.amazonaws.services.kinesis.model.Record r;
    int recordsRead = 0;
    while ((r = recordQueue.poll()) != null) {
      recordsRead++;

      for (byte[] stream : ostreams) {
        ctx.produceRecord(stream,
            r.getPartitionKey().getBytes(),
            r.getData().array());
      }
    }

    time = System.currentTimeMillis();
    if (recordsRead == 0) {
      time += 50;
    }
    ctx.setTimer(key, time);
  }

  @Override
  public void processRecord(ComputationContext ctx,
                            com.concord.swift.Record record) {
    throw new RuntimeException();
  }

  @Override
  public Metadata metadata() {
    HashSet<String> os = new HashSet<String>();
    for (byte[] o : ostreams) {
      os.add(new String(o));
    }

    return new Metadata(name,
        new HashSet<StreamTuple>(),
        new HashSet<String>(os));
  }

  @Override
  public void run() {
    ServeComputation.serve(this);
  }

  public static void main(String[] args) {
    Thread.currentThread().setUncaughtExceptionHandler(
        UncaughtExceptionHandlers.systemExit());
    Thread.setDefaultUncaughtExceptionHandler(
        UncaughtExceptionHandlers.systemExit());

    Options opts = Options.parse(args);

    ArrayBlockingQueue<Record> recordQueue =
      new ArrayBlockingQueue<Record>(opts.getQueueSize());

    Consumer consumer = new Consumer(
        recordQueue,
        opts.getOstreams(),
        opts.getName());

    Thread consumerThread = new Thread(consumer);

    RecordProcessorFactory factory = new RecordProcessorFactory(recordQueue);
    Worker worker = new Worker(factory, opts.getKinesisConfiguration());

    Thread workerThread = new Thread(worker);

    try {
      consumerThread.start();
      workerThread.start();
      workerThread.join();
      consumerThread.join();
    } catch (InterruptedException e) {
      System.exit(1);
    }
  }
}

