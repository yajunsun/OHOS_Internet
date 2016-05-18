package zgan.ohos.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;

@SuppressLint("DefaultLocale")
public final class LocationUtil {

	/*public static String getDrivenToken(Context context) {
        String drivenToken = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
		if (drivenToken == null) {
			drivenToken = android.provider.Settings.System.getString(
					context.getContentResolver(),
					android.provider.Settings.System.ANDROID_ID);
			if (drivenToken == null) {
				return "AAAAAAAAAAAAA";
			}
		}
		return drivenToken;
	}*/

    public static String getDrivenToken(Context context, String phone) {
        String drivenToken = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (drivenToken == null) {
            drivenToken = android.provider.Settings.System.getString(
                    context.getContentResolver(),
                    android.provider.Settings.System.ANDROID_ID);
            if (drivenToken == null) {
                return "ZG" + phone;
            }
        }
        return drivenToken;
    }


//    @SuppressLint("DefaultLocale")
//    public static String checkNetWork(Context context) {
//        ConnectivityManager connMgr = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        String result = null;
//        if (networkInfo == null) {
//            result = GlobalUtil.NETWORK_NONE;
//        } else {
//            int nType = networkInfo.getType();
//            if (nType == ConnectivityManager.TYPE_MOBILE) {
//                if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
//                    result = "CMNET";
//                } else {
//                    result = "CMWAP";
//                }
//            } else if (nType == ConnectivityManager.TYPE_WIFI) {
//                result = "WIFI";
//            }
//        }
//        return result;
//    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isAvailable();
    }


//    public static Location getCurrentLocation(Context context) {
//        LocationManager locationManager = (LocationManager) context
//                .getSystemService(Context.LOCATION_SERVICE);
//
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Location location = locationManager
//                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            return location;
//        } else {
////            if (GlobalUtil.NETWORK_NONE.equals(checkNetWork(context))) {
////                return null;
////            }
//            locationManager.requestLocationUpdates(
//                    LocationManager.NETWORK_PROVIDER, 1000l, 0f,
//                    new LocationListener() {
//                        @Override
//                        public void onStatusChanged(String provider,
//                                                    int status, Bundle extras) {
//                        }
//
//                        @Override
//                        public void onProviderEnabled(String provider) {
//                        }
//
//                        @Override
//                        public void onProviderDisabled(String provider) {
//                        }
//
//                        @Override
//                        public void onLocationChanged(Location location) {
//                        }
//                    });
//            Location location = locationManager
//                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//
//            return location;
//        }
//    }

}
