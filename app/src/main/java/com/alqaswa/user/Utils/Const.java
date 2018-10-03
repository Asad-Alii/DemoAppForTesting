package com.alqaswa.user.Utils;

/**
 * Created by getit on 8/5/2016.
 */
public class Const {

    public static String PREF_NAME = "SMARCAR_PRERENCE";
    public static final int GET = 0;
    public static final int POST = 1;
    public static final int TIMEOUT = 30000;
    public static final int MAX_RETRY = 4;
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    public static final int CHOOSE_PHOTO = 100;
    public static final int TAKE_PHOTO = 101;

    public static final String REQUEST_ACCEPT = "REQUEST_ACCEPT";
    public static final String REQUEST_CANCEL = "REQUEST_CANCEL";
    public static final int NO_REQUEST = -1;
    public static final String DRIVER_STATUS = "driverstatus";

    public static final long DELAY = 0;
    public static final long TIME_SCHEDULE = 5 * 1000;
    public static final long DELAY_OFFLINE = 15 * 60 * 1000;
    public static final long TIME_SCHEDULE_OFFLINE = 15 * 60 * 1000;

    public static final String PLACES_AUTOCOMPLETE_API_KEY = "AIzaSyCDUdgm4jv4BGkIUyrMtNYNCzxz1P1GY1k";
    public static final String GOOGLE_API_KEY = "AIzaSyCDUdgm4jv4BGkIUyrMtNYNCzxz1P1GY1k";

    //Fragments
    public static final String HOME_MAP_FRAGMENT = "home_map_fragment";
    public static final String TRAVEL_MAP_FRAGMENT = "travel_map_fragment";
    public static final String RATING_FRAGMENT = "rating_fragment";
    public static final String REGISTER_FRAGMENT = "register_fragment";
    public static final String FORGOT_PASSWORD_FRAGMENT = "forgot_fragment";
    public static final String SEARCH_FRAGMENT = "search_fragment";
    public static final String REQUEST_FRAGMENT = "request_fragment";
    public static final String HOURLY_FRAGMENT = "hourly_fragment";
    public static final String AIRPORT_FRAGMENT = "airport_fragment";
    public static final String BOLT_FRAGMENT = "bolt_fragment";

    //  Trip request status
    public static final int IS_CREATED = 0;
    public static final int IS_ACCEPTED = 1;
    public static final int IS_DRIVER_DEPARTED = 2;
    public static final int IS_DRIVER_ARRIVED = 3;
    public static final int IS_DRIVER_TRIP_STARTED = 4;
    public static final int IS_DRIVER_TRIP_ENDED = 5;
    public static final int IS_DRIVER_RATED = 6;

    public static final String DEVICE_TYPE = "android";
    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String SOCIAL_FACEBOOK = "facebook";
    public static final String SOCIAL_GOOGLE = "google";
    public static final String MANUAL = "manual";
    public static final String SOCIAL = "social";
    public static final String REQUEST_DETAIL = "requestDetails";

    public static final String paypal = "PAYPAL";
    public static final String paygate = "PAYGATE";
    public static final String stripe = "STRIPE";


    public static final String GOOGLE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?";

    public class Params {
        public static final String ID = "id";
        public static final String TOKEN = "token";
        public static final String SOCIAL_ID = "social_unique_id";
        public static final String URL = "url";
        public static final String PICTURE = "picture";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String REPASSWORD = "confirm_password";
        public static final String FIRSTNAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String PHONE = "mobile";
        public static final String OTP = "otp";
        public static final String SSN = "ssn";
        public static final String DEVICE_TOKEN = "device_token";
        public static final String ICON = "icon";
        public static final String DEVICE_TYPE = "device_type";
        public static final String LOGIN_BY = "login_by";
        public static final String CURRENCEY = "currency_code";
        public static final String LANGUAGE = "language";
        public static final String REQUEST_ID = "request_id";
        public static final String GENDER = "gender";
        public static final String COUNTRY = "country";
        public static final String TIMEZONE = "timezone";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String TAXI_TYPE = "service_id";
        public static final String SERVICE_TYPE = "service_type";
        public static final String ORIGINS = "origins";
        public static final String DESTINATION = "destinations";
        public static final String SENSOR = "sensor";
        public static final String MODE = "mode";
        public static final String DISTANCE = "distance";
        public static final String TIME = "time";
        public static final String S_LATITUDE = "s_latitude";
        public static final String S_LONGITUDE = "s_longitude";
        public static final String D_LATITUDE = "d_latitude";
        public static final String D_LONGITUDE = "d_longitude";
        public static final String S_ADDRESS = "s_address";
        public static final String D_ADDRESS = "d_address";
        public static final String PAYMENT_MODE = "payment_mode";
        public static final String IS_PAID = "is_paid";
        public static final String COMMENT = "comment";
        public static final String RATING = "rating";
        public static final String PAYMENT_METHOD_NONCE = "payment_method_nonce";
        public static final String LAST_FOUR = "last_four";
        public static final String CARD_ID = "card_id";
        public static final String NO_HOUR = "number_hours";
        public static final String REQ_STATUS_TYPE = "request_status_type";
        public static final String HOURLY_PACKAGE_ID = "hourly_package_id";
        public static final String AIRPORT_PACKAGE_ID = "airport_price_id";
        public static final String PROMOCODE = "promo_code";
        public static final String REFERRAL_CODE = "referral_code";
        public static final String FAV_ID ="fav_id" ;
        public static final String ADDRESS ="address" ;
        public static final String FAVOURITE_NAME ="favourite_name" ;
    }

    public class ServiceType {

        //public static final String HOST_URL = "http://139.59.35.215/";// basic server
        public static final String SOCKET_URL = "http://167.99.231.31:3000/";// basic socket server
        // public static final String HOST_URL = "http://nikola.world/";// live server
        //public static final String SOCKET_URL = "http://nikola.world:3000/";
        public static final String HOST_URL = "http://167.99.231.31/";// PLdeveloping server
        // public static final String HOST_URL = "http://50.63.15.123/";// PL client server
        public static final String BASE_URL = HOST_URL + "userApi/";
        public static final String LOGIN = BASE_URL + "login";
        public static final String REGISTER = BASE_URL + "register";
        public static final String UPDATE_PROFILE = BASE_URL + "updateProfile";
        public static final String FORGOT_PASSWORD = BASE_URL + "forgotpassword";
        public static final String TAXI_TYPE = BASE_URL + "serviceList";
        public static final String GET_PROVIDERS = BASE_URL + "guestProviderList";
        public static final String FARE_CALCULATION = BASE_URL + "fare_calculator";
        public static final String REQUEST_TAXI = BASE_URL + "sendRequest";
        public static final String CANCEL_CREATE_REQUEST = BASE_URL + "waitingRequestCancel";
        public static final String CHECKREQUEST_STATUS = BASE_URL + "requestStatusCheck";
        public static final String PAYNOW = BASE_URL + "payment";
        public static final String RATE_PROVIDER = BASE_URL + "rateProvider";
        public static final String CANCEL_RIDE = BASE_URL + "cancelRequest";
        public static final String GET_HISTORY = BASE_URL + "history";
        public static final String GET_PAYMENT_MODES = BASE_URL + "getPaymentModes?";
        public static final String PAYMENT_MODE_UPDATE = BASE_URL + "PaymentModeUpdate";
        public static final String GET_BRAIN_TREE_TOKEN_URL = BASE_URL + "getbraintreetoken";
        public static final String CREATE_ADD_CARD_URL = BASE_URL + "addcard";
        public static final String GET_ADDED_CARDS_URL = BASE_URL + "getcards?";
        public static final String REMOVE_CARD = BASE_URL + "deletecard";
        public static final String CREATE_SELECT_CARD_URL = BASE_URL + "selectcard";
        public static final String REQUEST_LATER = BASE_URL + "laterRequest";
        public static final String GET_LATER = BASE_URL + "upcomingRequest";
        public static final String CANCEL_LATER_RIDE = BASE_URL + "cancel_later_request?";
        public static final String HOURLY_PACKAGE_FARE = BASE_URL + "hourly_package_fare";
        public static final String USER_MESSAGE_NOTIFY = BASE_URL + "message_notification?";
        public static final String AIRPORT_LST = BASE_URL + "airport_details?";
        public static final String LOCATION_LST = BASE_URL + "location_details?";
        public static final String AIRPORT_PACKAGE_FARE = BASE_URL + "airport_package_fare";
        public static final String GET_OTP = BASE_URL + "sendOtp?";
        public static final String CANCEL_REASON = BASE_URL + "cancellationReasons";
        public static final String VALIDATE_PROMO = BASE_URL + "validate_promo";
        public static final String ADVERTISEMENTS = BASE_URL + "adsManagement";

        // wallet config
        public static final String WALLET_HOST_URL = "http://walletbay.net/apps";
        public static final String WALLET_BASE_URL = WALLET_HOST_URL + "/api/business/";
        public static final String WALLET_TYPES = WALLET_BASE_URL + "payment-gateways";
        public static final String WALLET_BALANCE = WALLET_HOST_URL + "/api/businesses/users/";

        public static final String GET_SAVED_PLACES =BASE_URL+"userFavourites" ;
        public static final String CANCEL_FAV =BASE_URL+"deleteuserFavourite" ;
        public static final String ADD_FAV =BASE_URL+"adduserFavourite" ;
    }

    // service codes
    public class ServiceCode {
        public static final int REGISTER = 1;
        public static final int LOGIN = 2;
        public static final int UPDATE_PROFILE = 3;
        public static final int FORGOT_PASSWORD = 4;
        public static final int GOOGLE_DIRECTION_API = 5;
        public static final int TAXI_TYPE = 6;
        public static final int GET_PROVIDERS = 7;
        public static final int GOOGLE_MATRIX = 8;
        public static final int FARE_CALCULATION = 9;
        public static final int REQUEST_TAXI = 10;
        public static final int CANCEL_CREATE_REQUEST = 11;
        public static final int CHECKREQUEST_STATUS = 12;
        public static final int PAYNOW = 13;
        public static final int RATE_PROVIDER = 14;
        public static final int GOOGLE_MATRIX_ETA = 15;
        public static final int HOMETAXI_TYPE = 16;
        public static final int CANCEL_RIDE = 17;
        public static final int GET_HISTORY = 18;
        public static final int GET_PAYMENT_MODES = 19;
        public static final int PAYMENT_MODE_UPDATE = 20;
        public static final int GET_BRAIN_TREE_TOKEN_URL = 21;
        public static final int CREATE_ADD_CARD_URL = 22;
        public static final int GET_ADDED_CARDS_URL = 23;
        public static final int CREATE_SELECT_CARD_URL = 24;
        public static final int REMOVE_CARD = 25;
        public static final int REQUEST_LATER = 26;
        public static final int GET_LATER = 27;
        public static final int CANCEL_LATER_RIDE = 28;
        public static final int HOURLY_PACKAGE_FARE = 29;
        public static final int USER_MESSAGE_NOTIFY = 30;
        public static final int AIRPORT_LST = 31;
        public static final int LOCATION_LST = 32;
        public static final int AIRPORT_PACKAGE_FARE = 33;
        public static final int GOOGLE_DIRECTION_forcar_API = 34;
        public static final int LOCATION_API_BASE_SOURCE = 35;
        public static final int LOCATION_API_BASE_DESTINATION = 36;
        public static final int ADDRESS_API_BASE = 37;
        public static final int GET_OTP = 38;
        public static final int GOOGLE_ADDRESS_API = 39;
        public static final int WALLET_TYPES = 40;
        public static final int WALLET_BALANCE = 41;
        public static final int WALLET_CREDIT = 42;
        public static final int WALLET_PAYGATE = 43;
        public static final int CANCEL_REASON = 44;
        public static final int VALIDATE_PROMO =45;
        public static final int GET_SAVED_PLACES = 46;
        public static final int CANCEL_FAV = 47;
        public static final int ADD_FAV =48 ;
        public static final int ADVERTISEMENTS = 49;
    }


    // Placesurls
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String TYPE_NEAR_BY = "/nearbysearch";
    public static final String OUT_JSON = "/json";

    // Location API
    public static final String LOCATION_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    // Address API
    public static final String ADDRESS_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";

    // direction API
    public static final String DIRECTION_API_BASE = "https://maps.googleapis.com/maps/api/directions/json?";
    public static final String ORIGIN = "origin";
    public static final String DESTINATION = "destination";
    public static final String EXTANCTION = "sensor=false&mode=driving&alternatives=true&key=" + Const.GOOGLE_API_KEY;
}
