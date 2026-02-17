package com.iti.cuisine.profile;

import android.app.Application;
import android.util.Log;

import androidx.core.util.Pair;

import com.iti.cuisine.Constants;
import com.iti.cuisine.R;
import com.iti.cuisine.data.auth.AuthRepo;
import com.iti.cuisine.data.auth.AuthRepoImpl;
import com.iti.cuisine.data.meal.MealErrorResult;
import com.iti.cuisine.data.meal.MealRepo;
import com.iti.cuisine.data.meal.MealRepoImpl;
import com.iti.cuisine.data.user.UserData;
import com.iti.cuisine.data.user.UserRepo;
import com.iti.cuisine.data.user.UserRepoImpl;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ProfilePresenterImpl implements ProfilePresenter {

    private final BehaviorSubject<UserData> currentUser = BehaviorSubject.create();
    private final BehaviorSubject<String> planCount = BehaviorSubject.create();
    private final BehaviorSubject<String> favoriteCount = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showLoading = BehaviorSubject.create();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private ProfileView view;

    private final UserRepo userRepo;
    private final MealRepo mealRepo;
    private final AuthRepo authRepo;

    private Disposable userDataDisposable;
    private Disposable planCountDisposable;
    private Disposable favoriteCountDisposable;
    private Disposable uploadDisposable;
    private Disposable loadingDisposable;

    public static ProfilePresenter createNewInstance() {
        MealRepo mealRepo = MealRepoImpl.getInstance();
        UserRepo userRepo = new UserRepoImpl();
        AuthRepo authRepo = new AuthRepoImpl();
        return new ProfilePresenterImpl(userRepo, mealRepo, authRepo);
    }

    public ProfilePresenterImpl(UserRepo userRepo, MealRepo mealRepo, AuthRepo authRepo) {
        this.userRepo = userRepo;
        this.mealRepo = mealRepo;
        this.authRepo = authRepo;
        refreshData();
    }

    private void refreshData() {
        Disposable planDisposable = mealRepo.getPlanMealCount()
                .subscribe(p -> planCount.onNext(p.toString())
                        , e -> Log.e(Constants.TAG, "refreshData: ", e));

        Disposable favDisposable = mealRepo.getFavoriteCount()
                .subscribe(f -> favoriteCount.onNext(f.toString())
                        , e -> Log.e(Constants.TAG, "refreshData: ", e));

        disposables.addAll(planDisposable, favDisposable);
    }

    @Override
    public void setView(ProfileView view) {
        this.view = view;
        listenToCounts();
        listenToLoading();
        listenToUserData();
    }

    private void listenToUserData() {
        userDataDisposable = userRepo.getUserObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            currentUser.onNext(user);
                            if (view != null) {
                                view.setUseData(user);
                            }
                        },e -> Log.e(Constants.TAG, "listenToCounts: ", e));
        disposables.add(userDataDisposable);
    }

    private void listenToCounts() {
        planCountDisposable = planCount
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> {
                            if (view != null) {
                                view.setPlanCount(count);
                            }
                        },e -> Log.e(Constants.TAG, "listenToCounts: ", e));
        favoriteCountDisposable = favoriteCount
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> {
                            if (view != null) {
                                view.setFavoriteCount(count);
                            }
                        },e -> Log.e(Constants.TAG, "listenToCounts: ", e));

        disposables.addAll(planCountDisposable, favoriteCountDisposable);
    }

    @Override
    public void uploadData() {
        UserData user = currentUser.getValue();
        if (user == null || user.isGuest()) {
            if (view != null) {
                view.showMessage(R.string.you_re_in_guest_mode_sign_in_to_save_your_favorites_and_meal_plans);
            }
            return;
        }
        uploadDisposable =  Single.zip(
                        mealRepo.getSingleAllFavoriteMeal(),
                        mealRepo.getSingleAllPlanMeal(),
                        Pair::new
                )
                .flatMapCompletable(pair ->
                        userRepo.uploadData(pair.first, pair.second)
                )
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> showLoading.onNext(true))
                .timeout(5000, TimeUnit.MILLISECONDS)
                .doFinally(() -> showLoading.onNext(false))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (view != null) {
                                view.showMessage(R.string.data_uploaded_successfully);
                            }
                        },
                        e -> {
                            if (view != null) {
                                view.showMessage(MealErrorResult.fromThrowable(e).getMessageId());
                            }
                        }
                );
        disposables.add(uploadDisposable);
    }

    @Override
    public void signOut(Application application) {
        mealRepo.deleteAllMeals().subscribe();
        authRepo.signOut(application);
        if (view != null) {
            view.navigateToLogin();
        }
    }


    private void listenToLoading() {
        loadingDisposable = showLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(show -> {
            if (view != null) {
                if (show) {
                    view.showLoading();
                } else {
                    view.hideLoading();
                }
            }
        });
        disposables.add(loadingDisposable);
    }

    @Override
    public void removeView() {
        stopListeningToDiscoverData();
        view = null;
    }

    private void stopListeningToDiscoverData() {
        disposables.remove(planCountDisposable);
        disposables.remove(favoriteCountDisposable);
        disposables.remove(loadingDisposable);
        disposables.remove(userDataDisposable);
    }

    @Override
    public void destroy() {

    }
}
