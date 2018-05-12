package io.github.vladimirmi.localradio.data.net;

public class Api {

    public final static String BASE_URL = "http://api.dar.fm/";
    public final static String STREAM_BASE_URL = "http://stream.dar.fm/";

    public static final String QUERY_LATITUDE = "latitude";
    public static final String QUERY_LONGITUDE = "longitude";
    public static final String QUERY_IP = "ip";
    public static final String QUERY_COUNTRY = "country";
    public static final String QUERY_CITY = "city";

    public final static int CONNECT_TIMEOUT = 5000;
    public final static int READ_TIMEOUT = 5000;
    public final static int WRITE_TIMEOUT = 5000;

    public final static int RETRY_COUNT = 3;
    public final static int RETRY_DELAY = 2000;

    private Api() {
    }
}
