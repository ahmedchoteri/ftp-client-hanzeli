package com.hanzeli.karlftp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class Example extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aktivita sa ako objekt nainicializuje a vytvorí
        // si všekty objekty, ktoré bude
        // potrebovať pre svoju existenciu
        // (napr. definuje si GUI objekty)
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Aktivita sa pripravuje na stav spustená a vytvorí
        // si GUI, ktoré sa má zobraziť
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Aktivita sa stáva viditeľnou (zobrazí sa GUI) a
        // prechádza do stavu spustená
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Aktivita prechádza do stavu pozastavená a iná
        // aktivita sa dostáva do popredia
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Aktivita prechádza do stavu zastavená a je kompletne
        // prekrytá inou aplikáciou
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Aktivita sa pripravuje na to,že bude zničená a
        // vykonáva všetky potrebné finalizačné kroky
    }
}
