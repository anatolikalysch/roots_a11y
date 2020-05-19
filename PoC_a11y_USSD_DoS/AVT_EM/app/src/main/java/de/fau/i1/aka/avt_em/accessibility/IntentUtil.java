package de.fau.i1.aka.avt_em.accessibility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.util.Patterns;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IntentUtil
{

    public static void openHomeScreen(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openDrawOverOtherAppsSettings(Context context)
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openAccessibilitySettings(Context context)
    {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openSecuritySettings(Context context)
    {
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openInstaller(Context context, Uri apk)
    {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(apk);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openPlayStore(Context context, String packageName)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("market://details?id=" + packageName);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openApplicationSettings(Context context, String applicationPackageName)
    {
        Uri uri = Uri.parse("package:" + applicationPackageName);

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openInstallUnknownAppsSetting(Context context, String applicationPackageName)
    {
        Uri uri = Uri.parse("package:" + applicationPackageName);

        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openPhone(Context context, String number)
    {
        number = number != null ? Uri.encode(number) : "";
        Uri uri = Uri.parse("tel:" + number);

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openApplication(Context context, String applicationPackageName)
    {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(applicationPackageName);
        if (intent != null)
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void openActivity(Context context, String applicationPackageName, String activityPackageName)
    {
        Intent intent = getActivityIntent(context, applicationPackageName, activityPackageName);
        if (intent != null)
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void openActivity(Context context, Class<? extends Activity> activity)
    {
        Intent intent = new Intent(context, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static Intent getApplicationIntent(Context context, String packageName)
    {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null)
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public static Intent getActivityIntent(Context context, String applicationPackageName, String activityPackageName)
    {
        Intent intent = new Intent();
        intent.setClassName(applicationPackageName, activityPackageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty() ? intent : null;
    }

    public static void openWebSite(Context context, String url)
    {
        Pattern webUrl = Patterns.WEB_URL;
        Matcher matcher = webUrl.matcher(url);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Url is not valid.");
        }
        Uri uri = Uri.parse(url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
