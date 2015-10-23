package com.concord.kinesis.utils;

import com.amazonaws.auth.AWSCredentials;

public class Credentials implements AWSCredentials {
  private String key;
  private String secret;

  public Credentials(String key, String secret) {
    this.key = key;
    this.secret = secret;
  }

  @Override
  public String getAWSAccessKeyId() { return key; }

  @Override
  public String getAWSSecretKey() { return secret; }
}


