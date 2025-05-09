package mx.unam.contactosapp;

import android.app.Application;

import mx.unam.contactosapp.viewmodel.HomeViewModel;

public class ContactosApp extends Application {
    private HomeViewModel homeViewModel;

    @Override
    public void onCreate() {
        super.onCreate();
        homeViewModel = new HomeViewModel();
    }

    public HomeViewModel getHomeViewModel() {
        return homeViewModel;
    }
}
