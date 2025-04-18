package com.hello.sandbox.fake.frameworks;

import android.os.IBinder;
import android.os.IInterface;
import com.hello.sandbox.SandBoxCore;
import com.hello.sandbox.utils.Reflector;
import java.lang.reflect.ParameterizedType;

/** Created by BlackBox on 2022/3/23. */
public abstract class BlackManager<Service extends IInterface> {
  public static final String TAG = "BlackManager";

  private Service mService;

  protected abstract String getServiceName();

  public Service getService() {
    if (mService != null
        && mService.asBinder().pingBinder()
        && mService.asBinder().isBinderAlive()) {
      return mService;
    }
    try {
      mService =
          Reflector.on(getTClass().getName() + "$Stub")
              .method("asInterface", IBinder.class)
              .call(SandBoxCore.get().getService(getServiceName()));
      mService
          .asBinder()
          .linkToDeath(
              new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                  mService.asBinder().unlinkToDeath(this, 0);
                  mService = null;
                }
              },
              0);
      return getService();
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
  }

  private Class<Service> getTClass() {
    return (Class<Service>)
        ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }
}
