package com.google.protobench;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class UnsafeUtil {
  static final sun.misc.Unsafe UNSAFE = getUnsafe();
  static final boolean HAS_UNSAFE_ARRAY_OPERATIONS = supportsUnsafeArrayOperations();
  static final long ARRAY_BASE_OFFSET = byteArrayBaseOffset();


  /**
   * Gets the {@code sun.misc.Unsafe} instance, or {@code null} if not available on this
   * platform.
   */
  private static sun.misc.Unsafe getUnsafe() {
    sun.misc.Unsafe unsafe = null;
    try {
      unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
        @Override
        public sun.misc.Unsafe run() throws Exception {
          Class<sun.misc.Unsafe> k = sun.misc.Unsafe.class;

          for (Field f : k.getDeclaredFields()) {
            f.setAccessible(true);
            Object x = f.get(null);
            if (k.isInstance(x)) {
              return k.cast(x);
            }
          }
          // The sun.misc.Unsafe field does not exist.
          return null;
        }
      });
    } catch (Throwable e) {
      // Catching Throwable here due to the fact that Google AppEngine raises NoClassDefFoundError
      // for Unsafe.
    }
    return unsafe;
  }

  /**
   * Indicates whether or not unsafe array operations are supported on this platform.
   */
  // TODO(nathanmittler): Add support for Android's MemoryBlock.
  private static boolean supportsUnsafeArrayOperations() {
    boolean supported = false;
    if (UNSAFE != null) {
      try {
        UNSAFE.getClass().getMethod("arrayBaseOffset", Class.class);
        UNSAFE.getClass().getMethod("putByte", Object.class, long.class, byte.class);
        supported = true;
      } catch (Throwable e) {
        // Do nothing.
      }
    }
    return supported;
  }

  /**
   * Get the base offset for byte arrays, or {@code -1} if {@code sun.misc.Unsafe} is not
   * available.
   */
  private static <T> int byteArrayBaseOffset() {
    return HAS_UNSAFE_ARRAY_OPERATIONS ? UNSAFE.arrayBaseOffset(byte[].class) : -1;
  }
}
