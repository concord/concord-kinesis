package com.concord.kinesis;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.*;

public class RecordProcessor implements IRecordProcessor {
  private BlockingQueue<Record> recordQueue;
  private String shardId;

  public RecordProcessor(BlockingQueue<Record> rq) {
    recordQueue = rq;
  }

  @Override
  public void initialize(String shardId) {
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
        System.err.println("Process interrupted while emitting record");
      } catch (InvalidStateException e) {
        // checkpointer
        System.err.println("Kinesis consumer reached invalid state");
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
  }
}

