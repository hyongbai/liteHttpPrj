package me.yourbay.litehttp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LiteHttp {
    private static boolean DEBUG = true;
    /* int params */
    private final static int S_TIME_OUT = 30 * 1000;
    private final static int S_MAX_REPEAT_COUNT = 3;
    private final static int S_BUFF_SIZE = 16 * 1024;
    /* string params */
    private final static String S_USER_AGENT = "Mozilla/5.0 (Linux; U; Android; en-ca;) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    /* default headers */
    private final static Map<String, String> S_DEFAULT_HEADERS = new HashMap<>();

    static {
        S_DEFAULT_HEADERS.put("User-Agent", S_USER_AGENT);
    }

    private static Handler HANDLER = new Handler(Looper.getMainLooper());

    private LiteHttp() {

    }

    /* basic methods */

    public static HttpURLConnection beginConnect(String method, String urlStr, Map<String, String> headers, boolean hasBody, int retryCount) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(S_TIME_OUT);
        conn.setReadTimeout(S_TIME_OUT);
        conn.setDoInput(true);
        conn.setDoOutput(hasBody);
            /* method */
        conn.setRequestMethod(method);
            /* headers */
        if (headers != null) {
            setHeader(conn, headers);
        }
            /* set default headers */
        setHeader(conn, S_DEFAULT_HEADERS);
            /* connect */
        if (repeatConnect(conn, retryCount) == retryCount) {
            conn.disconnect();
        }
        return conn;
    }

    public static boolean setHeader(URLConnection conn, Map<String, String> header) {
        if (conn == null || header == null || header.isEmpty()) {
            return false;
        }
        try {
            Set<Map.Entry<String, String>> set = header.entrySet();
            for (Map.Entry<String, String> me : set) {
                conn.setRequestProperty(me.getKey(), me.getValue());
            }
            return true;
        } catch (Exception e) {
            log("/* SET-HEADER EXCEPTION : " + (e != null ? e.getMessage() : "NULL") + " */");
            e.printStackTrace();
        }
        return false;

    }

    public static int repeatConnect(URLConnection conn, int retryCount) throws Exception {
        if (conn == null) {
            return 0;
        }
        Exception last = null;
            /* connect */
        for (int i = 0; i < retryCount; i++) {
            try {
                conn.connect();
                return i;
            } catch (Exception e) {
                last = e;
                log("/* REPEATLY-CONNECT : " + (i + 1) + " " + (e != null ? e.getMessage() : "NULL") + " */");
                e.printStackTrace();
                snooze();
            }
        }
        log("ERROR: connect EXCEED max=" + retryCount);
        throw last != null ? last : new IllegalAccessException("exceed max retry count " + retryCount);
    }

    /* for data */

    public static void getConnResult(URLConnection conn, OutputStream os, ProgressListener listener, Response response) throws Exception {
        final boolean hasOs = os != null;
        InputStream is = conn.getInputStream();
        if (!hasOs) {
            os = new ByteArrayOutputStream();
        }
        write(os, is, listener, conn.getContentLength());
        if (!hasOs) {
            response.result = os.toString();
        }
        if (conn instanceof HttpURLConnection) {
            response.statusCode = ((HttpURLConnection) conn).getResponseCode();
        }
        close(is);
    }

    public static boolean upload(URLConnection conn, InputStream is, ProgressListener listener) {
        if (is == null) {
            return false;
        }
        OutputStream os = null;
        try {
            if (conn != null) {
                os = conn.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(os);
                return write(bos, is, listener, -1);
            }
        } catch (Exception e) {
            log("/* UPLOAD EXCEPTION : " + (e != null ? e.getMessage() : "NULL") + " */");
            e.printStackTrace();
        } finally {
            close(os);
        }
        return false;
    }

    public static boolean write(final OutputStream os, final InputStream is, final ProgressListener listener, long length) {
        if (os == null || is == null) {
            return false;
        }
        try {
            //
            final long total = is.available();
            final byte[] buffer = new byte[S_BUFF_SIZE];
            //
            int len;
            long readLen = 0;
            while ((len = is.read(buffer)) != -1) {
                readLen += len;
                os.write(buffer, 0, len);
                if (listener != null) {
                    final long current = readLen;
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProgress(os, is, total, current, (total > current) ? (int) (current / total) * 100 : -1);
                        }
                    });
                }
            }
            //
            os.flush();
            return true;
        } catch (Exception e) {
            log("/* WRITE EXCEPTION : " + (e != null ? e.getMessage() : "NULL") + " */");
        }
        return false;
    }

    /* utilities */
    public static boolean close(Closeable... closeable) {
        if (closeable == null || closeable.length == 0) {
            return false;
        }
        for (Closeable cls : closeable) {
            if (cls == null) {
                continue;
            }
            try {
                cls.close();
            } catch (Exception e) {
            }
        }
        return true;
    }


    public static void log(String log) {
        if (!DEBUG) {
            return;
        }
        Log.d(LiteHttp.class.getSimpleName(), log);
    }

    public static void snooze() {
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* connector */
    public enum Method {
        OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE
    }

    public static class Response {
        public String result;
        public int statusCode;
        public Exception exception;

        private Response() {
        }
    }

    public static class Request {
        int retry = S_MAX_REPEAT_COUNT;
        //
        String url;
        Method method = Method.GET;
        Map<String, String> header;
        StreamListener streamListener;
        ProgressListener progressListener;

        public Request setUrl(String url) {
            this.url = url;
            return this;
        }

        public Request setRetry(int retry) {
            this.retry = retry;
            return this;
        }

        /**
         * {@link Method#GET} is default
         */
        public Request setMethod(Method m) {
            this.method = m;
            return this;
        }

        public Request setHeader(Map<String, String> h) {
            this.header = h;
            return this;
        }

        public Request setStreamListener(StreamListener streamListener) {
            this.streamListener = streamListener;
            return this;
        }

        public Request setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
            return this;
        }

        public Request() {
        }

        public Response connect() {
            Response response = new Response();
            final InputStream is = streamListener != null ? streamListener.getInStream() : null;
            final OutputStream os = streamListener != null ? streamListener.getOutStream() : null;
            try {
                HttpURLConnection conn = beginConnect(method.name().toUpperCase(), url, header, is != null, retry);
                if (conn == null) {
                    return null;
                }
                if (is != null) {
                    upload(conn, is, progressListener);
                }
                try {
                    getConnResult(conn, os, progressListener, response);
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                response.exception = e;
            } finally {
                close(is, os);
            }
            return response;
        }
    }

    public interface StreamListener {
        /*for body/upload*/
        InputStream getInStream();

        /*for download*/
        OutputStream getOutStream();
    }

    public interface ProgressListener {
        void onProgress(OutputStream os, InputStream is, long total, long current, int progress);
    }
}

