package sn.zhang.deskAround.tapshare.provider.bili;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {

    public static IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("sn.zhang.deskAround.ACTION_CONTENT_PREPARATION_REQUIRED");
        return intentFilter;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ((!lpparam.packageName.equals("tv.danmaku.bili"))) {
            return;
        }
        final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("sn.zhang.deskAround.ACTION_CONTENT_PREPARATION_REQUIRED")) {
                    try {
                        Object VideoDetailsActivity = context.getClassLoader().loadClass("com.bilibili.base.BiliContext").getDeclaredMethod("topActivitiy", null).invoke(null);
                        Stream<Field> fields = Arrays.stream(VideoDetailsActivity.getClass().getDeclaredFields()).filter(field -> field.getType().getName().startsWith("com.bilibili.video.videodetail.VideoDetailsActivity")).filter(field -> Arrays.stream(field.getType().getDeclaredMethods()).anyMatch(method -> method.getName().equals("getAvid")));
                        Optional<Field> fieldMightEmpty = fields.findFirst();
                        if (fieldMightEmpty.isPresent()) {
                            Field field = fieldMightEmpty.get();
                            String avid = (String) field.getType().getDeclaredMethod("getAvid", null).invoke(XposedHelpers.getObjectField(VideoDetailsActivity, field.getName()));
                            Log.d("BILIDEBUG", "ready to broadcast : " + avid);
                            ((Context) VideoDetailsActivity).sendBroadcast(new Intent("sn.zhang.deskAround.ACTION_CONTENT_PREPARATION_FINISHED").putExtra("sn.zhang.deskAround.EXTRA_DATA", "https://b23.tv/av" + avid));
                        } else {
                            Log.d("BILIDEBUG", "nothing to broadcast");
                            throw new NoSuchMethodException();
                        }
                    } catch (IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException | ClassNotFoundException e) {
                        AndroidAppHelper.currentApplication().sendBroadcast(new Intent("sn.zhang.deskAround.ACTION_CAST_FAILED").putExtra("sn.zhang.deskAround.EXTRA_DATA", "无法获取视频信息"));
                    }
                }
            }
        };

        XposedHelpers.findAndHookMethod("com.bilibili.video.videodetail.VideoDetailsActivity", lpparam.classLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ((Context) param.thisObject).registerReceiver(mBroadcastReceiver, getIntentFilter());
            }
        });
        XposedHelpers.findAndHookMethod("com.bilibili.video.videodetail.VideoDetailsActivity", lpparam.classLoader, "onStop", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ((Context) param.thisObject).unregisterReceiver(mBroadcastReceiver);
            }
        });
    }
}
