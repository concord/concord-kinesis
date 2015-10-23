package com.concord.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class RecordProcessor implements IRecordProcessor {
  private BlockingQueue<Record> recordQueue;
  private String shardId;

  public RecordProcessor(BlockingQueue<Record> rq) {
    recordQueue = rq;
  }

  @Override
  public void initialize(String shardId) {
    Preconditions.checkNotNull(shardId);
    System.err.println("Initialized processor on shard id: " + shardId);
    this.shardId = shardId;
  }

  @Override
  public void processRecords(List<Record> records,
                             IRecordProcessorCheckpointer checkpointer) {
    for (Record record : records) {
      try {
        recordQueue.put(record);
        checkpointer.checkpoint(record);
      } catch (InterruptedException e) {
        // recordQueue put
        Throwables.propagate(e);
      } catch (InvalidStateException e) {
        // checkpointer
        Throwables.propagate(e);
      } catch (ShutdownException e) {
        // checkpointer
        System.err.println("Shutting down kinesis consumer");
        break;
      }
    }
  }

  @Override
  public void shutdown(IRecordProcessorCheckpointer checkpointer,
                       ShutdownReason reason) {
    System.out.println("Shutting down Kinesis consumer for shard: " + shardId);
    System.exit(1);
  }
}

