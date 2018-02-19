package knowit.com.weatherapp.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.text.Html;
import android.util.LruCache;

import java.io.Closeable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author Alexei Ivanov
 */
public class Utility {

    public static final int MINIMUM_LOCATION_UPDATE_DELAY = 60000 * 5;
    public static final int MINIMUM_LOCATION_UPDATE_DISTANCE = 500;

    private static TestSecuredConnection connectionTest;

    private static LruCache<String, Bitmap> memoryCache;
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int cacheSize = maxMemory / 8; // Using 1/8th of the available memory for this memory cache as recommended.

    public static Date convertDateString(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getTimeFromDateString(String date) {
        Date time = convertDateString(date);
        if (time == null) {
            return 0;
        } else {
            return time.getTime();
        }
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy', kl. 'HH:mm:ss");
        return sdf.format(date);
    }

    public static Date getDateFromString(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy', kl. 'HH:mm:ss");
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        initBitmapCache();
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        // do not init here, if memoryCache is null here, memoryCache.get(key) will yield nothing
        return memoryCache == null ? null : memoryCache.get(key);
    }

    public static void clearBitmapCache() {
        // use with caution - should only be used before each new search
        if (memoryCache != null) memoryCache.evictAll();
    }

    private static void initBitmapCache() {
        if (memoryCache == null) {
            memoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
    }

    public static String convertHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {

            }
        }
    }

    public static LruCache<String, Bitmap> getBitmapMemCache() {
        return memoryCache;
    }

    public static void setBitmapMemCache(LruCache<String,Bitmap> bitmapMemCache) {
        memoryCache = bitmapMemCache;
    }

    public static void resetBitmapCache() {
        memoryCache = null;
        initBitmapCache();
    }

    public static String getHashCode(String query, int page) {
        if (query == null || query.isEmpty()) return "";
        return String.valueOf((query+page).hashCode());
    }

    public static int getDuration(String date) {
        long now = System.currentTimeMillis();
        Date d = getDateFromString(date);
        long t = d == null ? 0 : d.getTime();

        return (int) (now-t)/60000;
    }

    /**
     * Sets the given {@link TestSecuredConnection} object as {@link Utility#connectionTest} field.
     * @param test a {@link TestSecuredConnection} object to save
     */
    public static void setConnectionTest(TestSecuredConnection test){
        connectionTest = test;
    }

    /**
     * Returns the {@link TestSecuredConnection} object stored in {@link Utility#connectionTest} field.
     * @return a {@link TestSecuredConnection} object
     */
    public static TestSecuredConnection getConnectionTest(){
        return connectionTest;
    }

}