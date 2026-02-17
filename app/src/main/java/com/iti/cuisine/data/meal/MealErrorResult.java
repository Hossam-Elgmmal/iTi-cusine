package com.iti.cuisine.data.meal;

import android.util.Log;

import com.iti.cuisine.Constants;
import com.iti.cuisine.R;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

public enum MealErrorResult {
    NO_INTERNET(R.string.no_internet_connection),
    TIMEOUT(R.string.connection_timeout_please_check_your_internet),
    SERVER_UNAVAILABLE(R.string.service_temporarily_unavailable),
    NO_DATA_AVAILABLE(R.string.no_meals_available_right_now),
    UNKNOWN_ERROR(R.string.something_went_wrong_please_try_again);

    private final int messageId;

    MealErrorResult(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public static MealErrorResult fromThrowable(Throwable error) {
        if (error instanceof UnknownHostException) {
            return NO_INTERNET;
        } else if (error instanceof SocketTimeoutException || error instanceof TimeoutException) {
            return TIMEOUT;
        }
        else if (error instanceof HttpException) {
            HttpException httpException = (HttpException) error;
            int code = httpException.code();

            if (code >= 500) {
                return SERVER_UNAVAILABLE;
            } else if (code == 404) {
                return NO_DATA_AVAILABLE;
            } else {
                return SERVER_UNAVAILABLE;
            }
        } else if (error instanceof IOException) {
            return NO_INTERNET;
        } else if (error instanceof NoSuchElementException) {
            return NO_DATA_AVAILABLE;
        }

        Log.e(Constants.TAG, "Meals Error: ", error);

        return UNKNOWN_ERROR;
    }
}