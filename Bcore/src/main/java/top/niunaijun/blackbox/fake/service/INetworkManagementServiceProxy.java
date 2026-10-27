package top.niunaijun.blackbox.fake.service;

import static top.niunaijun.blackbox.app.BActivityThread.getUid;

import java.lang.reflect.Method;

import black.android.os.BRINetworkManagementServiceStub;
import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;
import top.niunaijun.blackbox.utils.Slog;


public class INetworkManagementServiceProxy extends BinderInvocationStub {
    public static final String NAME = "network_management";

    public INetworkManagementServiceProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        return BRINetworkManagementServiceStub.get().asInterface(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(NAME);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        // These calls require NETWORK_STACK / MAINLINE_NETWORK_STACK, which the host app never
        // has. StrictMode.setVmPolicy() (called by Compose, WebView, GMS, Play Store...) invokes
        // setUidCleartextNetworkPolicy, so forwarding them crashes every guest process.
        // They only tune per-uid policy, so a no-op is safe for a sandboxed app.
        addMethodHook(new NoOpMethod("setUidCleartextNetworkPolicy"));
        addMethodHook(new NoOpMethod("setUidMeteredNetworkBlacklist"));
        addMethodHook(new NoOpMethod("setUidMeteredNetworkWhitelist"));
    }

    private static class NoOpMethod extends MethodHook {
        private final String name;

        NoOpMethod(String name) {
            this.name = name;
        }

        @Override
        protected String getMethodName() {
            return name;
        }

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    @ProxyMethod("getNetworkStatsUidDetail")
    public static class getNetworkStatsUidDetail extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstUid(args);
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
