package remix.myplayer.application;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.update.BmobUpdateAgent;
import remix.myplayer.BuildConfig;
import remix.myplayer.appshortcuts.DynamicShortcutManager;
import remix.myplayer.db.DBManager;
import remix.myplayer.db.DBOpenHelper;
import remix.myplayer.service.MusicService;
import remix.myplayer.theme.ThemeStore;
import remix.myplayer.util.ColorUtil;
import remix.myplayer.util.CommonUtil;
import remix.myplayer.util.CrashHandler;
import remix.myplayer.util.ErrUtil;
import remix.myplayer.util.MediaStoreUtil;
import remix.myplayer.util.PermissionUtil;
import remix.myplayer.util.PlayListUtil;
import remix.myplayer.util.cache.DiskCache;

/**
 * Created by taeja on 16-3-16.
 */

/**
 * 错误收集与上报
 */
public class APlayerApplication extends android.app.Application {
    private static Context mContext;
    private RefWatcher mRefWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        APlayerApplication application = (APlayerApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        initUtil();
        initTheme();

        //友盟异常捕获
        MobclickAgent.setCatchUncaughtExceptions(true);
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        //字体
        CommonUtil.setFontSize(this);
        //友盟分享
        UMShareAPI.get(this);
        Config.DEBUG = BuildConfig.DEBUG;
        //bomb
        Bmob.initialize(this, "0c070110fffa9e88a1362643fb9d4d64");
        BmobUpdateAgent.setUpdateOnlyWifi(false);
        BmobUpdateAgent.update(this);
        //禁止默认的页面统计方式
        MobclickAgent.openActivityDurationTrack(false);
        //异常捕获
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        //检测内存泄漏
        mRefWatcher = LeakCanary.install(this);
        //AppShortcut
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            new DynamicShortcutManager(this).setUpShortcut();
        startService(new Intent(this, MusicService.class));
    }

    private void initUtil() {
        //初始化工具类
        DBManager.initialInstance(new DBOpenHelper(mContext));
        PermissionUtil.setContext(mContext);
        MediaStoreUtil.setContext(mContext);
        CommonUtil.setContext(mContext);
        ErrUtil.setContext(mContext);
        DiskCache.init(mContext);
        ColorUtil.setContext(mContext);
        PlayListUtil.setContext(mContext);
        final int cacheSize = (int)(Runtime.getRuntime().maxMemory() / 8);
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setBitmapMemoryCacheParamsSupplier(() -> new MemoryCacheParams(cacheSize, Integer.MAX_VALUE,cacheSize,Integer.MAX_VALUE, 2 * ByteConstants.MB))
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .setMainDiskCacheConfig(DiskCacheConfig.newBuilder(mContext).setMaxCacheSize(0).build())
                .build();
        Fresco.initialize(this,config);
    }

    /**
     * 初始化主题
     */
    private void initTheme() {
        ThemeStore.THEME_MODE = ThemeStore.loadThemeMode();
        ThemeStore.THEME_COLOR = ThemeStore.loadThemeColor();

        ThemeStore.MATERIAL_COLOR_PRIMARY = ThemeStore.getMaterialPrimaryColorRes();
        ThemeStore.MATERIAL_COLOR_PRIMARY_DARK = ThemeStore.getMaterialPrimaryDarkColorRes();
    }

    public static Context getContext(){
        return mContext;
    }

    static {
        PlatformConfig.setWeixin("wx10775467a6664fbb","8a64ff1614ffe8d8dd4f8cc794f3c4f1");
    }
}
