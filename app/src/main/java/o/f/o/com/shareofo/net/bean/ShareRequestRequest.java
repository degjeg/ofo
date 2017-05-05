// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ofo.proto at 9:1
package o.f.o.com.shareofo.net.bean;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class ShareRequestRequest extends Message<ShareRequestRequest, ShareRequestRequest.Builder> {
  public static final ProtoAdapter<ShareRequestRequest> ADAPTER = new ProtoAdapter_ShareRequestRequest();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_DEVICE_NAME = "";

  public static final String DEFAULT_IP = "";

  public static final ByteString DEFAULT_ALIAS = ByteString.EMPTY;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String device_name;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String ip;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#BYTES"
  )
  public final ByteString alias;

  public ShareRequestRequest(String device_name, String ip, ByteString alias) {
    this(device_name, ip, alias, ByteString.EMPTY);
  }

  public ShareRequestRequest(String device_name, String ip, ByteString alias, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.device_name = device_name;
    this.ip = ip;
    this.alias = alias;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.device_name = device_name;
    builder.ip = ip;
    builder.alias = alias;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ShareRequestRequest)) return false;
    ShareRequestRequest o = (ShareRequestRequest) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(device_name, o.device_name)
        && Internal.equals(ip, o.ip)
        && Internal.equals(alias, o.alias);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (device_name != null ? device_name.hashCode() : 0);
      result = result * 37 + (ip != null ? ip.hashCode() : 0);
      result = result * 37 + (alias != null ? alias.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (device_name != null) builder.append(", device_name=").append(device_name);
    if (ip != null) builder.append(", ip=").append(ip);
    if (alias != null) builder.append(", alias=").append(alias);
    return builder.replace(0, 2, "ShareRequestRequest{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<ShareRequestRequest, Builder> {
    public String device_name;

    public String ip;

    public ByteString alias;

    public Builder() {
    }

    public Builder device_name(String device_name) {
      this.device_name = device_name;
      return this;
    }

    public Builder ip(String ip) {
      this.ip = ip;
      return this;
    }

    public Builder alias(ByteString alias) {
      this.alias = alias;
      return this;
    }

    @Override
    public ShareRequestRequest build() {
      return new ShareRequestRequest(device_name, ip, alias, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_ShareRequestRequest extends ProtoAdapter<ShareRequestRequest> {
    ProtoAdapter_ShareRequestRequest() {
      super(FieldEncoding.LENGTH_DELIMITED, ShareRequestRequest.class);
    }

    @Override
    public int encodedSize(ShareRequestRequest value) {
      return (value.device_name != null ? ProtoAdapter.STRING.encodedSizeWithTag(1, value.device_name) : 0)
          + (value.ip != null ? ProtoAdapter.STRING.encodedSizeWithTag(2, value.ip) : 0)
          + (value.alias != null ? ProtoAdapter.BYTES.encodedSizeWithTag(3, value.alias) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, ShareRequestRequest value) throws IOException {
      if (value.device_name != null) ProtoAdapter.STRING.encodeWithTag(writer, 1, value.device_name);
      if (value.ip != null) ProtoAdapter.STRING.encodeWithTag(writer, 2, value.ip);
      if (value.alias != null) ProtoAdapter.BYTES.encodeWithTag(writer, 3, value.alias);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public ShareRequestRequest decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.device_name(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.ip(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.alias(ProtoAdapter.BYTES.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public ShareRequestRequest redact(ShareRequestRequest value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}