// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ofo.proto at 27:1
package o.f.o.com.shareofo.net.bean;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

/**
 * push data 的请求
 * pull data 的返回
 */
public final class DataPack extends Message<DataPack, DataPack.Builder> {
  public static final ProtoAdapter<DataPack> ADAPTER = new ProtoAdapter_DataPack();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_REMAIN = 0;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer remain;

  @WireField(
      tag = 2,
      adapter = "o.f.o.com.shareofo.net.bean.OfoDate#ADAPTER",
      label = WireField.Label.REPEATED
  )
  public final List<OfoDate> data;

  public DataPack(Integer remain, List<OfoDate> data) {
    this(remain, data, ByteString.EMPTY);
  }

  public DataPack(Integer remain, List<OfoDate> data, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.remain = remain;
    this.data = Internal.immutableCopyOf("data", data);
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.remain = remain;
    builder.data = Internal.copyOf("data", data);
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof DataPack)) return false;
    DataPack o = (DataPack) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(remain, o.remain)
        && data.equals(o.data);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (remain != null ? remain.hashCode() : 0);
      result = result * 37 + data.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (remain != null) builder.append(", remain=").append(remain);
    if (!data.isEmpty()) builder.append(", data=").append(data);
    return builder.replace(0, 2, "DataPack{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<DataPack, Builder> {
    public Integer remain;

    public List<OfoDate> data;

    public Builder() {
      data = Internal.newMutableList();
    }

    public Builder remain(Integer remain) {
      this.remain = remain;
      return this;
    }

    public Builder data(List<OfoDate> data) {
      Internal.checkElementsNotNull(data);
      this.data = data;
      return this;
    }

    @Override
    public DataPack build() {
      return new DataPack(remain, data, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_DataPack extends ProtoAdapter<DataPack> {
    ProtoAdapter_DataPack() {
      super(FieldEncoding.LENGTH_DELIMITED, DataPack.class);
    }

    @Override
    public int encodedSize(DataPack value) {
      return (value.remain != null ? ProtoAdapter.INT32.encodedSizeWithTag(1, value.remain) : 0)
          + OfoDate.ADAPTER.asRepeated().encodedSizeWithTag(2, value.data)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, DataPack value) throws IOException {
      if (value.remain != null) ProtoAdapter.INT32.encodeWithTag(writer, 1, value.remain);
      OfoDate.ADAPTER.asRepeated().encodeWithTag(writer, 2, value.data);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public DataPack decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.remain(ProtoAdapter.INT32.decode(reader)); break;
          case 2: builder.data.add(OfoDate.ADAPTER.decode(reader)); break;
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
    public DataPack redact(DataPack value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.data, OfoDate.ADAPTER);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
