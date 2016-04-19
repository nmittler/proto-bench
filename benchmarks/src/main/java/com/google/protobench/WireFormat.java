package com.google.protobench;

import com.google.protobuf.Internal;

import java.io.IOException;

/**
 * This class is used internally by the Protocol Buffer library and generated
 * message implementations.  It is public only because those generated messages
 * do not reside in the {@code protobuf} package.  Others should not use this
 * class directly.
 *
 * This class contains constants and helper functions useful for dealing with
 * the Protocol Buffer wire format.
 *
 * @author kenton@google.com Kenton Varda
 */
public final class WireFormat {
  // Do not allow instantiation.
  private WireFormat() {}

  public static final int FIXED_32_SIZE = 4;
  public static final int FIXED_64_SIZE = 8;
  public static final int MAX_VARINT_SIZE = 10;

  public static final int WIRETYPE_VARINT           = 0;
  public static final int WIRETYPE_FIXED64          = 1;
  public static final int WIRETYPE_LENGTH_DELIMITED = 2;
  public static final int WIRETYPE_START_GROUP      = 3;
  public static final int WIRETYPE_END_GROUP        = 4;
  public static final int WIRETYPE_FIXED32          = 5;

  static final int TAG_TYPE_BITS = 3;
  static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;

  /** Given a tag value, determines the wire type (the lower 3 bits). */
  public static int getTagWireType(final int tag) {
    return tag & TAG_TYPE_MASK;
  }

  /** Given a tag value, determines the field number (the upper 29 bits). */
  public static int getTagFieldNumber(final int tag) {
    return tag >>> TAG_TYPE_BITS;
  }

  /** Makes a tag value given a field number and wire type. */
  static int makeTag(final int fieldNumber, final int wireType) {
    return (fieldNumber << TAG_TYPE_BITS) | wireType;
  }

  public enum JavaType {
    INT(0),
    LONG(0L),
    FLOAT(0F),
    DOUBLE(0D),
    BOOLEAN(false),
    STRING(""),
    BYTES(Internal.EMPTY_BYTE_ARRAY),
    ENUM(null),
    MESSAGE(null);

    JavaType(final Object defaultDefault) {
      this.defaultDefault = defaultDefault;
    }

    /**
     * The default default value for fields of this type, if it's a primitive
     * type.
     */
    Object getDefaultDefault() {
      return defaultDefault;
    }

    private final Object defaultDefault;
  }

  public enum FieldType {
    DOUBLE  (JavaType.DOUBLE     , WIRETYPE_FIXED64         ),
    FLOAT   (JavaType.FLOAT      , WIRETYPE_FIXED32         ),
    INT64   (JavaType.LONG       , WIRETYPE_VARINT          ),
    UINT64  (JavaType.LONG       , WIRETYPE_VARINT          ),
    INT32   (JavaType.INT        , WIRETYPE_VARINT          ),
    FIXED64 (JavaType.LONG       , WIRETYPE_FIXED64         ),
    FIXED32 (JavaType.INT        , WIRETYPE_FIXED32         ),
    BOOL    (JavaType.BOOLEAN    , WIRETYPE_VARINT          ),
    STRING  (JavaType.STRING     , WIRETYPE_LENGTH_DELIMITED) {
      @Override
      public boolean isPackable() {
        return false; }
    },
    GROUP   (JavaType.MESSAGE    , WIRETYPE_START_GROUP     ) {
      @Override
      public boolean isPackable() {
        return false; }
    },
    MESSAGE (JavaType.MESSAGE    , WIRETYPE_LENGTH_DELIMITED) {
      @Override
      public boolean isPackable() {
        return false; }
    },
    BYTES   (JavaType.BYTES, WIRETYPE_LENGTH_DELIMITED) {
      @Override
      public boolean isPackable() {
        return false; }
    },
    UINT32  (JavaType.INT        , WIRETYPE_VARINT          ),
    ENUM    (JavaType.ENUM       , WIRETYPE_VARINT          ),
    SFIXED32(JavaType.INT        , WIRETYPE_FIXED32         ),
    SFIXED64(JavaType.LONG       , WIRETYPE_FIXED64         ),
    SINT32  (JavaType.INT        , WIRETYPE_VARINT          ),
    SINT64  (JavaType.LONG       , WIRETYPE_VARINT          );

    FieldType(final JavaType javaType, final int wireType) {
      this.javaType = javaType;
      this.wireType = wireType;
    }

    private final JavaType javaType;
    private final int wireType;

    public JavaType getJavaType() { return javaType; }
    public int getWireType() { return wireType; }

    public boolean isPackable() { return true; }
  }

  // Field numbers for fields in MessageSet wire format.
  static final int MESSAGE_SET_ITEM    = 1;
  static final int MESSAGE_SET_TYPE_ID = 2;
  static final int MESSAGE_SET_MESSAGE = 3;

  // Tag numbers.
  static final int MESSAGE_SET_ITEM_TAG =
          makeTag(MESSAGE_SET_ITEM, WIRETYPE_START_GROUP);
  static final int MESSAGE_SET_ITEM_END_TAG =
          makeTag(MESSAGE_SET_ITEM, WIRETYPE_END_GROUP);
  static final int MESSAGE_SET_TYPE_ID_TAG =
          makeTag(MESSAGE_SET_TYPE_ID, WIRETYPE_VARINT);
  static final int MESSAGE_SET_MESSAGE_TAG =
          makeTag(MESSAGE_SET_MESSAGE, WIRETYPE_LENGTH_DELIMITED);
}
