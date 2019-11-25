package com.purbon.kafka.clients;

public class Transaction {

  private String txId;
  private long amount;
  private long timestamp;

  public Transaction() {
    this.txId = "";
    this.amount = 0;
    this.timestamp = System.currentTimeMillis();
  }
  public String getTxId() {
    return txId;
  }

  public void setTxId(String txId) {
    this.txId = txId;
  }

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
