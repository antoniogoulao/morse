package io.goulao.morse;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.goulao.morse.adapter.NavDrawerListAdapter;
import io.goulao.morse.entity.FlashLightMorseCode;
import io.goulao.morse.entity.MorseCodeCharacter;
import io.goulao.morse.entity.MorseCodeLibrary;
import io.goulao.morse.entity.NavDrawerItem;
import io.goulao.morse.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

/*      International Morse Code
 1. The length of a dot is one unit.
 2. A dash is three units.
 3. The space between parts of the same letter is one unit.
 4. The space between letters is three units.
 5. The space between words is seven units.
 */

    private static final String morseSpeedKey = "morsePulseSpeed";
    private static final String flashlightSwitchKey = "flashlight";
    private Camera camera;
    private Camera.Parameters params;
    private FlashMorseAsyncTask flashMorseAsyncTask;

    private boolean flashallowed;
    private int oneTimeUnit;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<NavDrawerItem> navDrawerItems;
    private String[] navMenuTitles;
    private NavDrawerListAdapter adapter;
    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(this);
        // First check if device is supporting flashlight or not
        boolean hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
            return;
        }

        getCamera();
        findViewById(R.id.send_button).setOnClickListener(new SendButtonOnClickListener());

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        TypedArray navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slider);

        navDrawerItems = new ArrayList<NavDrawerItem>();
        for (int i = 0; i < navMenuTitles.length; i++) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1)));
        }
        // Recycle the typed array
        navMenuIcons.recycle();

        mTitle = mDrawerTitle = getTitle();

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_menu_white_36px, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }


    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new SettingsFragment();
                break;
            case 1:
                //fragment = new AboutFragment();
                break;

            default:
                break;
        }

        if (fragment != null) {

            // If other fragments are being displayed pop them out
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            }

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.content_frame, fragment).addToBackStack(navMenuTitles[position]).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     *
     */
    private class FlashMorseAsyncTask extends AsyncTask<String, Void, Void> {

        private Handler handler = new Handler();

        @Override
        protected Void doInBackground(String[] params) {
            String input = params[0];
            synchronized (this) {
                try {
                    for (int i = 0; i < input.length(); i++) {
                        String character = String.valueOf(input.charAt(i));
                        if (character.equalsIgnoreCase(" ") || character.equalsIgnoreCase("\n")) {
                            // End of word
                            wait(7 * oneTimeUnit);
                            fillMorseString(" ");
                        } else if (FlashLightMorseCode.getCharactersCache().containsKey(character.toLowerCase())) {
                            // Cache in use
                            MorseCodeCharacter mChar = FlashLightMorseCode.getCharactersCache().get(character.toLowerCase());
                            int pos = 0;
                            for (Integer time : mChar.getTimes()) {
                                fillMorseString(String.valueOf(mChar.getCode().charAt(pos++)));
                                turnOnFlash();
                                wait(time);
                                turnOffFlash();
                                wait(oneTimeUnit);
                            }
                            wait(3 * oneTimeUnit);
                        } else {
                            String morseCode = MorseCodeLibrary.getMorseCodeCharacterList().get(character.toLowerCase());
                            // Avoid characters not supported by the library
                            if (morseCode != null && !morseCode.equals("")) {
                                List<Integer> list = new ArrayList<Integer>();
                                MorseCodeCharacter morseChar;
                                for (int j = 0; j < morseCode.length(); j++) {
                                    fillMorseString(String.valueOf(morseCode.charAt(j)));
                                    Integer time = FlashLightMorseCode.getFlashLightValues().get(String.valueOf(morseCode.charAt(j)));
                                    list.add(time);
                                    turnOnFlash();
                                    wait(time);
                                    turnOffFlash();
                                    wait(oneTimeUnit);
                                }
                                morseChar = new MorseCodeCharacter(morseCode, list);
                                FlashLightMorseCode.getCharactersCache().put(character, morseChar);
                                wait(3 * oneTimeUnit);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Log.d("FlashMorseAsyncTask", e.getMessage());
                }
            }
            return null;
        }

        private void fillMorseString(final String addition) {
            final TextView morseText = (TextView) findViewById(R.id.morse_text);
            handler.post(new Runnable() {
                public void run() {
                    morseText.setText(morseText.getText() + addition);
                }
            });
        }
    }

    private class SendButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (flashMorseAsyncTask == null || flashMorseAsyncTask.getStatus() != AsyncTask.Status.RUNNING || flashMorseAsyncTask.isCancelled()) {
                flashMorseAsyncTask = new FlashMorseAsyncTask();
                EditText textInput = ((EditText) findViewById(R.id.text_to_send));
                TextView morseText = (TextView) findViewById(R.id.morse_text);
                String text = textInput.getText().toString();
                if (!text.equals("")) {
                    textInput.setText("");
                    morseText.setText("");
                    ((TextView) findViewById(R.id.sent_text)).setText(text);
                    flashMorseAsyncTask.execute(text);
                }
            }
        }
    }

    // Get the camera
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("MainActivity", e.getMessage());
            }
        }
    }

    // Turning On flash
    private void turnOnFlash() {
        if (!flashallowed || camera == null || params == null) {
            return;
        }

        params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
    }


    // Turning Off flash
    private void turnOffFlash() {
        if (!flashallowed || camera == null || params == null) {
            return;
        }

        params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flashMorseAsyncTask != null && flashMorseAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            flashMorseAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        turnOffFlash();
        if (flashMorseAsyncTask != null && flashMorseAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            flashMorseAsyncTask.cancel(true);
        }

        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (flashMorseAsyncTask != null && flashMorseAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            flashMorseAsyncTask.cancel(true);
        }
        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCamera();
        loadPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
            setTitle(mDrawerTitle);
        }
    }

    private void loadPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        flashallowed = sp.getBoolean(flashlightSwitchKey, false);
        oneTimeUnit = Integer.parseInt(sp.getString(morseSpeedKey, "150"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("Preference changed", "Key " + key + " pressed");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(key.equals(flashlightSwitchKey)) {
            flashallowed = sp.getBoolean(flashlightSwitchKey, false);
        } else if (key.equals(morseSpeedKey)) {
            oneTimeUnit = Integer.parseInt(sp.getString(morseSpeedKey, "150"));
        }
    }
}
