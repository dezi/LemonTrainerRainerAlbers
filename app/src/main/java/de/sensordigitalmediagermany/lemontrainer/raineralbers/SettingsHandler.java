package de.sensordigitalmediagermany.lemontrainer.raineralbers;

import android.support.annotation.Nullable;

import android.view.ViewGroup;
import android.util.Log;
import android.os.Build;

import java.util.Locale;

import org.json.JSONObject;

@SuppressWarnings({"WeakerAccess"})
public class SettingsHandler
{
    final static String LOGTAG = SettingsHandler.class.getSimpleName();

    public static int getSharedPrefInt(String key)
    {
        try
        {
            return ApplicationBase.prefs.getInt(key, 0);
        }
        catch (Exception ex)
        {
            return 0;
        }
    }

    public static boolean getSharedPrefBoolean(String key)
    {
        try
        {
            return ApplicationBase.prefs.getBoolean(key, false);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    @Nullable
    public static String getSharedPrefString(String key)
    {
        try
        {
            return ApplicationBase.prefs.getString(key, null);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static void setSharedPrefString(String key, String value)
    {
        ApplicationBase.prefs.edit().putString(key, value).apply();
    }

    public static void setSharedPrefInt(String key, int value)
    {
        ApplicationBase.prefs.edit().putInt(key, value).apply();
    }

    public static void setSharedPrefBoolean(String key, boolean value)
    {
        ApplicationBase.prefs.edit().putBoolean(key, value).apply();
    }

    public static void removeSharedPref(String key)
    {
        ApplicationBase.prefs.edit().remove(key).apply();
    }

    public static void loadAllStuff()
    {
        if (Globals.accountId > 0)
        {
            SettingsHandler.fetchSettings();
        }
    }

    public static void loadSettings()
    {
        Globals.UDID = getSharedPrefString("session.UDID");

        Globals.emailAddress = getSharedPrefString("user.emailAddress");
        Globals.passWord = getSharedPrefString("user.passWord");
        Globals.userName = getSharedPrefString("user.userName");
        Globals.birthDay = getSharedPrefString("user.birthDay");
        Globals.firstName = getSharedPrefString("user.firstName");
        Globals.lastName = getSharedPrefString("user.lastName");
        Globals.gender = getSharedPrefString("user.gender");

        Globals.country = getSharedPrefString("user.country");
        Globals.city = getSharedPrefString("user.city");
        Globals.zip = getSharedPrefString("user.zip");
        Globals.street = getSharedPrefString("user.street");

        Globals.accountId = getSharedPrefInt("user.accountId");

        Log.d(LOGTAG, "loadSettings: userName=" + Globals.userName);
    }

    public static void saveSettings()
    {
        setSharedPrefString("session.UDID", Globals.UDID);

        setSharedPrefString("user.emailAddress", Globals.emailAddress);
        setSharedPrefString("user.passWord", Globals.passWord);

        setSharedPrefString("user.userName", Globals.userName);
        setSharedPrefString("user.birthDay", Globals.birthDay);
        setSharedPrefString("user.firstName", Globals.firstName);
        setSharedPrefString("user.lastName", Globals.lastName);
        setSharedPrefString("user.gender", Globals.gender);

        setSharedPrefString("user.country", Globals.country);
        setSharedPrefString("user.city", Globals.city);
        setSharedPrefString("user.zip", Globals.zip);
        setSharedPrefString("user.street", Globals.street);

        setSharedPrefInt("user.accountId", Globals.accountId);
    }

    public static void killSettings()
    {
        removeSharedPref("session.UDID");

        removeSharedPref("user.emailAddress");
        removeSharedPref("user.passWord");

        removeSharedPref("user.userName");
        removeSharedPref("user.birthDay");
        removeSharedPref("user.firstName");
        removeSharedPref("user.lastName");
        removeSharedPref("user.gender");

        removeSharedPref("user.country");
        removeSharedPref("user.city");
        removeSharedPref("user.zip");
        removeSharedPref("user.street");

        removeSharedPref("user.accountId");

        loadSettings();
    }

    public static void fetchSettings()
    {
        fetchSettings(Globals.accountId, null);
    }

    public static void fetchSettings(final Runnable callback)
    {
        fetchSettings(Globals.accountId, callback);
    }

    public static void fetchSettings(int accountId, final Runnable callback)
    {
        JSONObject params = new JSONObject();
        Json.put(params, "accountId", accountId);

        RestApi.getPostThreaded("getMyData", params, new RestApi.RestApiResultListener()
        {
            @Override
            public void OnRestApiResult(String what, JSONObject params, JSONObject result)
            {
                if ((result != null) && Json.equals(result, "Status", "OK"))
                {
                    int accountId = Json.getInt(params, "accountId");
                    JSONObject data = Json.getObject(result, "Data");

                    if ((accountId > 0) && (data != null))
                    {
                        Globals.accountId = accountId;

                        Globals.userName = Json.getString(data, "username");
                        Globals.birthDay = Json.getString(data, "birthday");
                        Globals.city = Json.getString(data, "city");
                        Globals.country = Json.getString(data, "country");
                        Globals.firstName = Json.getString(data, "firstname");
                        Globals.lastName = Json.getString(data, "lastname");
                        Globals.street = Json.getString(data, "street");
                        Globals.zip = Json.getString(data, "zip");
                        Globals.gender = Json.getString(data, "gender");

                        saveSettings();

                        if (callback != null) ApplicationBase.handler.post(callback);
                    }
                }
            }
        });
    }

    public static void realLoginAction(final ViewGroup rootView, final Runnable loginSuccess, final Runnable loginFailure)
    {
        JSONObject params = new JSONObject();

        Json.put(params, "UDID", Globals.UDID);
        Json.put(params, "email", Globals.emailAddress);
        Json.put(params, "password", Globals.passWord);
        Json.put(params, "trainerName", Defines.TRAINER_NAME);

        Json.put(params, "deviceKind", 2);
        Json.put(params, "version", Simple.getAppVersion(rootView.getContext()));
        Json.put(params, "platform", Build.MANUFACTURER + " " + Build.MODEL);
        Json.put(params, "language", Locale.getDefault().getDisplayLanguage());

        RestApi.getPostThreaded("checkForLogin", params, new RestApi.RestApiResultListener()
        {
            @Override
            public void OnRestApiResult(String what, JSONObject params, JSONObject result)
            {
                if ((result != null) && Json.equals(result, "Status", "OK"))
                {
                    int accountId = Json.getInt(result, "accountId");
                    JSONObject data = Json.getObject(result, "Data");

                    if ((accountId > 0) && (data != null))
                    {
                        Globals.accountId = accountId;

                        Globals.userName = Json.getString(data, "username");
                        Globals.birthDay = Json.getString(data, "birthday");
                        Globals.city = Json.getString(data, "city");
                        Globals.country = Json.getString(data, "country");
                        Globals.firstName = Json.getString(data, "firstname");
                        Globals.lastName = Json.getString(data, "lastname");
                        Globals.street = Json.getString(data, "street");
                        Globals.zip = Json.getString(data, "zip");
                        Globals.gender = Json.getString(data, "gender");

                        SettingsHandler.saveSettings();

                        loginSuccess.run();

                        return;
                    }
                }

                loginFailure.run();
            }
        });
    }
}
