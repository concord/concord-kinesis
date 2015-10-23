package com.concord.kinesis.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

public class CredentialsProvider implements AWSCredentialsProvider {
  Credentials credentials = null;

  public CredentialsProvider(String key, String secret) {
    credentials = new Credentials(key, secret);
  }

  @Override
  public AWSCredentials getCredentials() {
    return credentials;
  }

  @Override
  public void refresh() {}
}

