package com.hello.sandbox.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.hello.sandbox.SandBoxCore;
import com.hello.sandbox.app.BActivityThread;
import com.hello.sandbox.core.env.BEnvironment;
import com.hello.sandbox.utils.FileUtils;
import com.hello.sandbox.utils.TrieTree;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Created by Milk on 4/9/21. * ∧＿∧ (`･ω･∥ 丶　つ０ しーＪ 此处无Bug */
@SuppressLint("SdCardPath")
public class IOCore {
  public static final String TAG = "IOCore";

  private static final IOCore sIOCore = new IOCore();
  private static final TrieTree mTrieTree = new TrieTree();
  private static final TrieTree sBlackTree = new TrieTree();
  private final Map<String, String> mRedirectMap = new LinkedHashMap<>();

  private static final Map<String, Map<String, String>> sCachePackageRedirect = new HashMap<>();

  public static IOCore get() {
    return sIOCore;
  }

  // /data/data/com.google/  ----->  /data/data/com.virtual/data/com.google/
  public void addRedirect(String origPath, String redirectPath) {
    if (TextUtils.isEmpty(origPath)
        || TextUtils.isEmpty(redirectPath)
        || mRedirectMap.get(origPath) != null) return;
    // Add the key to TrieTree
    mTrieTree.add(origPath);
    mRedirectMap.put(origPath, redirectPath);
    File redirectFile = new File(redirectPath);
    if (!redirectFile.exists()) {
      FileUtils.mkdirs(redirectPath);
    }
    NativeCore.addIORule(origPath, redirectPath);
  }

  public String redirectPath(String path) {
    if (TextUtils.isEmpty(path)) return path;
    if (path.contains("/sandbox/")) {
      return path;
    }

    // Search the key from TrieTree
    String key = mTrieTree.search(path);
    if (!TextUtils.isEmpty(key)) {
      path = path.replace(key, Objects.requireNonNull(mRedirectMap.get(key)));
    }
    return path;
  }

  public File redirectPath(File path) {
    if (path == null) return null;
    String pathStr = path.getAbsolutePath();
    String redirectPath = redirectPath(pathStr);
    if (pathStr.equals(redirectPath)){
      return path;
    }
    return new File(redirectPath);
  }

  public String redirectPath(String path, Map<String, String> rule) {
    if (TextUtils.isEmpty(path)) return path;

    // Search the key from TrieTree
    String key = mTrieTree.search(path);
    if (!TextUtils.isEmpty(key)) path = path.replace(key, Objects.requireNonNull(rule.get(key)));

    return path;
  }

  public File redirectPath(File path, Map<String, String> rule) {
    if (path == null) return null;
    String pathStr = path.getAbsolutePath();
    return new File(redirectPath(pathStr, rule));
  }

  // 由于正常情况Application已完成重定向，以下重定向是怕代码写死。
  public void enableRedirect(Context context) {
    Map<String, String> rule = new LinkedHashMap<>();
    String packageName = context.getPackageName();

    try {
      ApplicationInfo packageInfo =
          SandBoxCore.getBPackageManager()
              .getApplicationInfo(
                  packageName, PackageManager.GET_META_DATA, BActivityThread.getUserId());
      int systemUserId = SandBoxCore.getHostUserId();
      rule.put(String.format("/data/data/%s/lib", packageName), packageInfo.nativeLibraryDir);
      rule.put(
          String.format("/data/user/%d/%s/lib", systemUserId, packageName),
          packageInfo.nativeLibraryDir);

      rule.put(String.format("/data/data/%s", packageName), packageInfo.dataDir);
      rule.put(String.format("/data/user/%d/%s", systemUserId, packageName), packageInfo.dataDir);

      if (SandBoxCore.getContext().getExternalCacheDir() != null
          && context.getExternalCacheDir() != null) {
        File external = BEnvironment.getExternalUserDir(BActivityThread.getUserId());
        // sdcard
        File sdcardAndroidFile = new File("/sdcard/Android");
        String androidDir = String.format("/storage/emulated/%d/Android", systemUserId);
        if (!sdcardAndroidFile.exists()) {
          sdcardAndroidFile = new File(androidDir);
        }
        if (sdcardAndroidFile.exists()) {
          File[] childDirs = sdcardAndroidFile.listFiles(pathname -> pathname.isDirectory());
          if (childDirs != null) {
            for (File childDir : childDirs) {
              Log.d(TAG, childDir.getAbsolutePath());
              rule.put(
                  "/sdcard/Android/" + childDir.getName(),
                  external.getAbsolutePath() + "/Android/" + childDir.getName());
              rule.put(
                  androidDir + "/" + childDir.getName(),
                  external.getAbsolutePath() + "/Android/" + childDir.getName());
            }
          } else {
            rule.put("/sdcard/Android", external.getAbsolutePath() + "/Android");
            rule.put(androidDir, external.getAbsolutePath() + "/Android");
          }
        } else {
          rule.put("/sdcard/Android", external.getAbsolutePath());
          rule.put(androidDir, external.getAbsolutePath());
        }
      }
      if (SandBoxCore.get().isHideRoot()) {
        hideRoot(rule);
      }
      proc(rule);
    } catch (Exception e) {
      e.printStackTrace();
    }
    for (String key : rule.keySet()) {
      get().addRedirect(key, rule.get(key));
    }
    NativeCore.enableIO();
  }

  private void hideRoot(Map<String, String> rule) {
    rule.put("/system/app/Superuser.apk", "/system/app/Superuser.apk-fake");
    rule.put("/sbin/su", "/sbin/su-fake");
    rule.put("/system/bin/su", "/system/bin/su-fake");
    rule.put("/system/xbin/su", "/system/xbin/su-fake");
    rule.put("/data/local/xbin/su", "/data/local/xbin/su-fake");
    rule.put("/data/local/bin/su", "/data/local/bin/su-fake");
    rule.put("/system/sd/xbin/su", "/system/sd/xbin/su-fake");
    rule.put("/system/bin/failsafe/su", "/system/bin/failsafe/su-fake");
    rule.put("/data/local/su", "/data/local/su-fake");
    rule.put("/su/bin/su", "/su/bin/su-fake");
  }

  private void proc(Map<String, String> rule) {
    int appPid = BActivityThread.getAppPid();
    int pid = Process.myPid();
    String selfProc = "/proc/self/";
    String proc = "/proc/" + pid + "/";

    String cmdline = new File(BEnvironment.getProcDir(appPid), "cmdline").getAbsolutePath();
    rule.put(proc + "cmdline", cmdline);
    rule.put(selfProc + "cmdline", cmdline);
  }
}
