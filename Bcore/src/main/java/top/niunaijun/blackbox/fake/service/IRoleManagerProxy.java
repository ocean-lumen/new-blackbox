package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import black.android.app.role.BRIRoleManagerStub;
import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;

/**
 * RoleManager checks that the package name passed in belongs to the calling uid
 * (AppOpsManager.checkPackage). Inside the sandbox the guest passes ITS package name
 * (e.g. org.mozilla.firefox) while the real caller is the host uid, so the system throws
 * "Package X does not belong to <uid>" and apps such as Firefox crash in
 * Browsers.isDefaultBrowser(). A virtual app never holds a real system role, so answer false.
 */
public class IRoleManagerProxy extends BinderInvocationStub {
    public static final String NAME = "role";

    public IRoleManagerProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        return BRIRoleManagerStub.get().asInterface(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(NAME);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("isRoleHeld")
    public static class IsRoleHeld extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return false;
        }
    }

    @ProxyMethod("isRoleHeldAsUser")
    public static class IsRoleHeldAsUser extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return false;
        }
    }
}
