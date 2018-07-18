package com.example.msi.dnskin.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

/**
 * @author：琚涛
 * @date：2018/07/16
 * @description：
 */
public class RxPermission {

    static final String TAG = RxPermission.class.getSimpleName();
    static final Object TRIGGER = new Object();

    @VisibleForTesting
    Lazy<RxPermissionFragment> rxPermissionsFragment;

    public RxPermission(@NonNull final FragmentActivity activity) {
        rxPermissionsFragment = getLazySingleton(activity.getSupportFragmentManager());
    }

    public RxPermission(@NonNull final Fragment fragment) {
        rxPermissionsFragment = getLazySingleton(fragment.getChildFragmentManager());
    }

    @NonNull
    private Lazy<RxPermissionFragment> getLazySingleton(@NonNull final FragmentManager fragmentManager) {
        return new Lazy<RxPermissionFragment>() {
            private RxPermissionFragment rxPermissionsFragment;

            @Override
            public RxPermissionFragment get() {
                if (rxPermissionsFragment == null) {
                    rxPermissionsFragment = getRxPermissionsFragment(fragmentManager);
                }
                return rxPermissionsFragment;
            }
        };
    }

    private RxPermissionFragment getRxPermissionsFragment(@NonNull final FragmentManager fragmentManager) {
        RxPermissionFragment rxPermissionsFragment = findRxPermissionsFragment(fragmentManager);
        boolean isNewInstance = rxPermissionsFragment == null;
        if (isNewInstance) {
            rxPermissionsFragment = new RxPermissionFragment();
            fragmentManager.beginTransaction().add(rxPermissionsFragment, TAG).commitNow();
        }
        return rxPermissionsFragment;
    }

    private RxPermissionFragment findRxPermissionsFragment(@NonNull final FragmentManager fragmentManager) {
        return (RxPermissionFragment) fragmentManager.findFragmentByTag(TAG);
    }

    public void setLogging(boolean logging) {
        rxPermissionsFragment.get().setLogging(logging);
    }

    @SuppressWarnings("WeakerAccess")
    public <T> ObservableTransformer<T, Boolean> ensure(final String... permissions) {
        return new ObservableTransformer<T, Boolean>() {
            @Override
            public ObservableSource<Boolean> apply(Observable<T> upstream) {
                return request(upstream, permissions).buffer(permissions.length)
                                                     .flatMap(
                                                             new Function<List<Permission>,
                                                                     ObservableSource<Boolean>>() {
                                                                 @Override
                                                                 public ObservableSource<Boolean> apply(
                                                                         List<Permission> permissions) throws
                                                                         Exception {
                                                                     if (permissions.isEmpty()) {
                                                                         return Observable.empty();
                                                                     }
                                                                     for (Permission permission : permissions) {
                                                                         if (!permission.granted) {
                                                                             return Observable.just(false);
                                                                         }
                                                                     }
                                                                     return Observable.just(true);
                                                                 }
                                                             });
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEachCombined(final String... permissions) {
        return new ObservableTransformer<T, Permission>() {
            @Override
            public ObservableSource<Permission> apply(Observable<T> upstream) {
                return request(upstream, permissions).buffer(permissions.length)
                                                     .flatMap(
                                                             new Function<List<Permission>,
                                                                     ObservableSource<Permission>>() {
                                                                 @Override
                                                                 public ObservableSource<Permission> apply(
                                                                         List<Permission> permissions) throws
                                                                         Exception {
                                                                     if (permissions.isEmpty()) {
                                                                         return Observable.empty();
                                                                     }
                                                                     return Observable.just(
                                                                             new Permission(permissions));
                                                                 }
                                                             });
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    public <T> ObservableTransformer<T, Permission> ensureEach(final String... permissions) {
        return new ObservableTransformer<T, Permission>() {
            @Override
            public ObservableSource<Permission> apply(Observable<T> upstream) {
                return request(upstream, permissions);
            }
        };
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Observable<Boolean> request(final String... permissions) {
        return Observable.just(TRIGGER).compose(ensure(permissions));
    }

    private Observable<Permission> request(final Observable<?> trigger, final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException(
                    "RxPermissions.request/requestEach requires at least one input permission");
        }
        return oneOf(trigger, pending(permissions)).flatMap(new Function<Object, ObservableSource<Permission>>() {
            @Override
            public ObservableSource<Permission> apply(Object o) throws Exception {
                return requestImplementation(permissions);
            }
        });
    }

    private Observable<?> pending(final String... permissions) {
        for (String permission : permissions) {
            if (!rxPermissionsFragment.get().containsByPermission(permission)) {
                return Observable.empty();
            }
        }
        return Observable.just(TRIGGER);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        if (trigger == null) {
            return Observable.just(TRIGGER);
        }
        return Observable.merge(trigger, pending);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Observable<Permission> requestImplementation(final String... permissions) {
        List<Observable<Permission>> list = new ArrayList<>(permissions.length);
        List<String> unrequestedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            rxPermissionsFragment.get().log("Requesting permission " + permission);
            if (isGranted(permission)) {
                list.add(Observable.just(new Permission(permission, true, false)));
                continue;
            }
            if (isRevoked(permission)) {
                list.add(Observable.just(new Permission(permission, false, false)));
                continue;
            }
            PublishSubject<Permission> subject = rxPermissionsFragment.get().getSubjectByPermission(permission);
            if (subject == null) {
                unrequestedPermissions.add(permission);
                subject = PublishSubject.create();
                rxPermissionsFragment.get().setSubjectForPermission(permission, subject);
            }
            list.add(subject);
        }
        if (!unrequestedPermissions.isEmpty()) {
            String[] unrequestedPermissionsArray = unrequestedPermissions.toArray(
                    new String[unrequestedPermissions.size()]);
            requestPermissionsFromFragment(unrequestedPermissionsArray);
        }
        return Observable.concat(Observable.fromIterable(list));
    }

    @TargetApi(Build.VERSION_CODES.M)
    void requestPermissionsFromFragment(String[] permissions) {
        rxPermissionsFragment.get().log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions));
        rxPermissionsFragment.get().requestPermissions(permissions);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("WeakerAccess")
    public boolean isGranted(String permission) {
        return !isMarshmallow() || rxPermissionsFragment.get().isGranted(permission);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isRevoked(String permission) {
        return isMarshmallow() && rxPermissionsFragment.get().isRevoked(permission);
    }

    boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Observable<Permission> requestEach(final String... permissions) {
        return Observable.just(TRIGGER).compose(ensureEach(permissions));
    }

    @SuppressWarnings("WeakerAccess")
    public Observable<Boolean> shouldShowRequestPermissionRationale(final Activity activity,
                                                                    final String... permissions) {
        if (!isMarshmallow()) {
            return Observable.just(false);
        }
        return Observable.just(shouldShowRequestPermissionRationaleImplementation(activity, permissions));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean shouldShowRequestPermissionRationaleImplementation(final Activity activity,
                                                                       final String... permissions) {
        for (String permission : permissions) {
            if (!isGranted(permission) && !activity.shouldShowRequestPermissionRationale(permission)) {
                return false;
            }
        }
        return true;
    }

    void onRequestPermissionsResult(String permissions[], int[] grantResults) {
        rxPermissionsFragment.get()
                             .onRequestPermissionsResult(permissions, grantResults, new boolean[permissions.length]);
    }

    @FunctionalInterface
    public interface Lazy<V> {

        V get();
    }
}
