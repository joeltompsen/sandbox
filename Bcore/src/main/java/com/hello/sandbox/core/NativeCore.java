package com.hello.sandbox.core;

import static com.hello.sandbox.core.env.BEnvironment.EMPTY_JAR;

import android.os.Process;
import androidx.annotation.Keep;
import com.hello.sandbox.SandBoxCore;
import com.hello.sandbox.app.BActivityThread;
import com.hello.sandbox.utils.compat.DexFileCompat;
import dalvik.system.DexFile;
import java.io.File;
import java.util.List;

/** Created by Milk on 4/9/21. * ∧＿∧ (`･ω･∥ 丶　つ０ しーＪ 此处无Bug */
public class NativeCore {
  public static final String TAG = "NativeCore";

  static {
    new File("");
    System.loadLibrary("sandbox");
  }

  public static native void init(int apiLevel);

  public static native void enableIO();

  public static native void addIORule(String targetPath, String relocatePath);

  public static native void hideXposed();

  public static native void init_seccomp();

  public static void dumpDex(ClassLoader classLoader, String packageName) {
    List<Long> cookies = DexFileCompat.getCookies(classLoader);
    for (Long cookie : cookies) {
      if (cookie == 0) continue;
      //            File file = new File(SandBoxCore.get().getDexDumpDir(), packageName);
      //            FileUtils.mkdirs(file);
      //            dumpDex(cookie, file.getAbsolutePath());
    }
  }

  @Keep
  public static int getCallingUid(int origCallingUid) {
    // 系统uid
    if (origCallingUid > 0 && origCallingUid < Process.FIRST_APPLICATION_UID) return origCallingUid;
    // 非用户应用
    if (origCallingUid > Process.LAST_APPLICATION_UID) return origCallingUid;

    if (origCallingUid == SandBoxCore.getHostUid()) {
      //            Log.d(TAG, "origCallingUid: " + origCallingUid + " => " +
      // BActivityThread.getCallingBUid());
      return BActivityThread.getCallingBUid();
    }
    return origCallingUid;
  }

  @Keep
  public static String redirectPath(String path) {
    return IOCore.get().redirectPath(path);
  }

  @Keep
  public static File redirectPath(File path) {
    return IOCore.get().redirectPath(path);
  }

  @Keep
  public static long[] loadEmptyDex() {
    try {
      DexFile dexFile = new DexFile(EMPTY_JAR);
      List<Long> cookies = DexFileCompat.getCookies(dexFile);
      long[] longs = new long[cookies.size()];
      for (int i = 0; i < cookies.size(); i++) {
        longs[i] = cookies.get(i);
      }
      return longs;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new long[] {};
  }
}
