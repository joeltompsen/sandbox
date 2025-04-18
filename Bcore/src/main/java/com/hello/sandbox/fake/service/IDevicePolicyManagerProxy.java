package com.hello.sandbox.fake.service;

import android.content.ComponentName;
import android.content.Context;
import black.android.app.admin.BRIDevicePolicyManagerStub;
import black.android.os.BRServiceManager;
import com.hello.sandbox.fake.hook.BinderInvocationStub;
import com.hello.sandbox.fake.hook.MethodHook;
import com.hello.sandbox.fake.hook.ProxyMethod;
import com.hello.sandbox.utils.MethodParameterUtils;
import java.lang.reflect.Method;

/** Created by Milk on 2021/5/17. * ∧＿∧ (`･ω･∥ 丶　つ０ しーＪ 此处无Bug */
public class IDevicePolicyManagerProxy extends BinderInvocationStub {
  public IDevicePolicyManagerProxy() {
    super(BRServiceManager.get().getService(Context.DEVICE_POLICY_SERVICE));
  }

  @Override
  protected Object getWho() {
    return BRIDevicePolicyManagerStub.get()
        .asInterface(BRServiceManager.get().getService(Context.DEVICE_POLICY_SERVICE));
  }

  @Override
  protected void inject(Object baseInvocation, Object proxyInvocation) {
    replaceSystemService(Context.DEVICE_POLICY_SERVICE);
  }

  @Override
  public boolean isBadEnv() {
    return false;
  }

  @ProxyMethod("getStorageEncryptionStatus")
  public static class GetStorageEncryptionStatus extends MethodHook {

    @Override
    protected Object hook(Object who, Method method, Object[] args) throws Throwable {
      MethodParameterUtils.replaceFirstAppPkg(args);
      return method.invoke(who, args);
    }
  }

  @ProxyMethod("getDeviceOwnerComponent")
  public static class GetDeviceOwnerComponent extends MethodHook {

    @Override
    protected Object hook(Object who, Method method, Object[] args) throws Throwable {
      return new ComponentName("", "");
    }
  }

  @ProxyMethod("getDeviceOwnerName")
  public static class getDeviceOwnerName extends MethodHook {

    @Override
    protected Object hook(Object who, Method method, Object[] args) throws Throwable {
      return "sandbox";
    }
  }

  @ProxyMethod("getProfileOwnerName")
  public static class getProfileOwnerName extends MethodHook {

    @Override
    protected Object hook(Object who, Method method, Object[] args) throws Throwable {
      return "sandbox";
    }
  }

  @ProxyMethod("isDeviceProvisioned")
  public static class isDeviceProvisioned extends MethodHook {
    @Override
    protected Object hook(Object who, Method method, Object[] args) throws Throwable {
      return true;
    }
  }
}
