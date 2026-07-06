package com.qjyes.yuliao;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;

/**
 * ApkInstaller: APP 内下载 + 自动安装 APK
 * install({url, filename}) → DownloadManager 下 → FileProvider → 系统安装器
 */
@CapacitorPlugin(name = "ApkInstaller")
public class ApkInstallerPlugin extends Plugin {

    private long downloadId = -1;
    private PluginCall pendingCall;

    @PluginMethod
    public void install(PluginCall call) {
        String url = call.getString("url");
        String filename = call.getString("filename", "yuliao-update.apk");

        if (url == null || url.isEmpty()) {
            call.reject("url is required");
            return;
        }

        Context ctx = getContext();

        // Android 8+ 必须有"未知来源"授权,没有就跳设置页
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
                && !ctx.getPackageManager().canRequestPackageInstalls()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + ctx.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            call.reject("NEED_UNKNOWN_SOURCE_PERMISSION");
            return;
        }

        pendingCall = call;

        try {
            // 目标文件:APP 私有下载目录(Android 10+ 无需存储权限)
            File apkFile = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);
            if (apkFile.exists()) {
                apkFile.delete();
            }

            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setTitle("宇聊更新");
            req.setDescription("正在下载 " + filename);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            req.setDestinationUri(Uri.fromFile(apkFile));
            req.setMimeType("application/vnd.android.package-archive");
            req.setAllowedOverMetered(true);
            req.setAllowedOverRoaming(true);

            DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadId = dm.enqueue(req);

            // 注册完成回调
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            ctx.registerReceiver(downloadCompleteReceiver, filter, Context.RECEIVER_EXPORTED);

            JSObject ret = new JSObject();
            ret.put("status", "downloading");
            ret.put("downloadId", downloadId);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("download failed: " + e.getMessage());
        }
    }

    private final BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id != downloadId) return;

            try {
                ctx.unregisterReceiver(this);
            } catch (Exception ignore) {}

            DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(id);
            Cursor c = dm.query(q);
            if (c != null && c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                c.close();

                if (status == DownloadManager.STATUS_SUCCESSFUL && localUri != null) {
                    File apkFile = new File(Uri.parse(localUri).getPath());
                    installApk(ctx, apkFile);
                    if (pendingCall != null) {
                        JSObject ret = new JSObject();
                        ret.put("status", "installing");
                        pendingCall.resolve(ret);
                        pendingCall = null;
                    }
                } else {
                    if (pendingCall != null) {
                        pendingCall.reject("download failed status=" + status);
                        pendingCall = null;
                    }
                }
            }
        }
    };

    private void installApk(Context ctx, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(
                ctx,
                ctx.getPackageName() + ".fileprovider",
                apkFile);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
