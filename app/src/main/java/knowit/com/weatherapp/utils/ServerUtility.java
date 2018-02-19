package knowit.com.weatherapp.utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import knowit.com.weatherapp.AsyncTaskCompleteListener;
import knowit.com.weatherapp.exceptions.AppErrorException;
import knowit.com.weatherapp.exceptions.NetworkErrorException;
import knowit.com.weatherapp.exceptions.ServerFaultException;
import knowit.com.weatherapp.xml.BaseXml;
import knowit.com.weatherapp.xml.WeatherData;

import static android.content.ContentValues.TAG;

public class ServerUtility {

    private static final int CONNECTION_TIMEOUT = 5000;

    public static final String ENCODING = "UTF8";
    public static final String METHOD_GET = "GET";


    private static final String SLASH = "/";
    private static final String HTTPS = "https";
    private static final String EMPTY_STRING = "";
    private static final String API_VERSION = "1.9";
    private static final String ICON_API_VERSION = "1.1";
    private static final String NIGHT_PARAMETER = ";is_night=1";
    private static final String SYMBOL_PARAMETER = "?symbol=";
    private static final String CONTENT_IMAGE_PNG_PARAMETER = ";content_type=image/png";
    private static final String BASE_URL = "https://api.met.no/weatherapi/locationforecast/";
    private static final String BASE_ICON_URL = "https://api.met.no/weatherapi/weathericon/";
    private static final String CONTENT_TYPE = "application/json; charset=utf8";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String MALFORMED_URL_MESSAGE = "Wrong base URL or URI";
    private static final String WRONG_HTTP_METHOD_MESSAGE = "Wrong HTTP-method";
    private static final String HTTP_FORBIDDEN_MESSAGE = "403 forbidden";
    private static final String UNKNOWN_ERROR_MESSAGE = "Unknow application error";
    private static final String INVALID_URL_MESSAGE = "Invalid URL";

    private static final String UNKNOWN_XML_ERROR = "Uknown error";


    public static WeatherData getWeather(String query)
            throws MalformedURLException, ServerFaultException, AppErrorException, NetworkErrorException {
        URL url;
        try {
            url = new URL(BASE_URL + API_VERSION + SLASH + query);
        } catch (MalformedURLException e) {
            throw new MalformedURLException(MALFORMED_URL_MESSAGE);
        }

        return (WeatherData) getXmlFromServer(ServerUtility.METHOD_GET, url, EMPTY_STRING, WeatherData.class);
    }

    private static InputStream callServer(String method, URL url, String body)
            throws IOException, AppErrorException, ServerFaultException {

        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);

        if (body != null && !body.isEmpty()) {
            if (method.equals(METHOD_GET)) {
                throw new AppErrorException(WRONG_HTTP_METHOD_MESSAGE);
            }

            connection.setDoOutput(true);
            OutputStream requestBodyStream = connection.getOutputStream();
            try {
                requestBodyStream = connection.getOutputStream();
                requestBodyStream.write(body.getBytes(ENCODING));
            } finally {
                if (requestBodyStream != null) try { requestBodyStream.close(); } catch (IOException ignore) {}
            }
        }

        connection.connect();
        if(BASE_URL.substring(0, 5).equals(HTTPS)) {
            Utility.setConnectionTest(new TestSecuredConnection(connection));
            if (Utility.getConnectionTest().getException() != null) {
                if (Utility.getConnectionTest().getException().getClass() == SSLPeerUnverifiedException.class) {
                    throw new AppErrorException("Uverifisert serverport. Vennligst ta kontakt med kundesenteret");
                } else if (Utility.getConnectionTest().getException().getClass() == IllegalStateException.class) {
                    throw new AppErrorException("Det har oppstått en feil");
                }
            } else if (!Utility.getConnectionTest().isValid()) {
//			System.out.println("Server certificate is NOT valid!");
                connection.disconnect();
                throw new AppErrorException("SSL-sertifikatet er ugyldig. Vennligst ta kontakt med kundesenteret");
            }
        }
        switch (connection.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new AppErrorException(HTTP_FORBIDDEN_MESSAGE);
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new ServerFaultException();
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                throw new ServerFaultException();
            default:
                Log.e(TAG, "Connection refused. HTTP response code: " + connection.getResponseCode());
                throw new AppErrorException(UNKNOWN_ERROR_MESSAGE);
        }

        return connection.getInputStream();
    }


    public static <T> Object getXmlFromServer(String method,
                                              URL url, String requestBody, Class<? extends BaseXml> type)
            throws AppErrorException, ServerFaultException, NetworkErrorException {

        InputStream response = null;
        try {
            response = callServer(method, url, requestBody);
        } catch (MalformedURLException e) {
            throw new AppErrorException(INVALID_URL_MESSAGE);
        } catch (IOException e) {
            throw new NetworkErrorException();
        }

        if (type == null) {
            return null;
        }

        BaseXml xmlObject = null;
        try {
            xmlObject = BaseXml.inputStreamToXml(response, type);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception while reading XML", e);
            throw new AppErrorException(UNKNOWN_XML_ERROR);
        }
        return xmlObject;
    }


    public static void getImageBitmap(AsyncTaskCompleteListener callback, int number) {
        String imageUrl = BASE_ICON_URL + ICON_API_VERSION + SLASH + SYMBOL_PARAMETER + number;

        Calendar now = Calendar.getInstance();
        Calendar six = new GregorianCalendar();
        six.set(Calendar.MINUTE, 0);
        six.set(Calendar.SECOND, 0);
        six.set(Calendar.MILLISECOND, 0);
        six.set(Calendar.HOUR_OF_DAY, 18);

        if (now.after(six)) {
            imageUrl += NIGHT_PARAMETER;
        }

        imageUrl += CONTENT_IMAGE_PNG_PARAMETER;
        System.out.println(imageUrl);

        new ImageLoadTask(callback, imageUrl).execute(imageUrl);
    }


    private static Bitmap getImageFromUrlSource(String urlString) {
        URL url;
        Bitmap bm = null;
        InputStream is = null;
        URLConnection connection;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = null;

        try {
            url = new URL(urlString);
            connection = url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            connection.connect();

            is = connection.getInputStream();
            bis = new BufferedInputStream(is);
            bos = new ByteArrayOutputStream();
            bm = BitmapFactory.decodeStream(bis);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "SocketTimeoutException while getting bitmap", e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting bitmap", e);
        } finally {
            Utility.closeSilently(bis);
            Utility.closeSilently(bos);
            Utility.closeSilently(is);
        }
        return bm;
    }

    private static class ImageLoadTask extends AsyncTask<String, String, Bitmap> {

        private String url;
        private AsyncTaskCompleteListener callback;

        ImageLoadTask(AsyncTaskCompleteListener callback, String url) {
            this.url = url;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return ServerUtility.getImageFromUrlSource(url);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                callback.onSuccess(image);
            } else {
                callback.onFailure();
            }
        }
    }

}