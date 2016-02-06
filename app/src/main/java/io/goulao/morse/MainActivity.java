package io.goulao.morse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.goulao.morse.entity.FlashLightMorseCode;
import io.goulao.morse.entity.MorseCodeCharacter;
import io.goulao.morse.entity.MorseCodeLibrary;

public class MainActivity extends AppCompatActivity {

/*      International Morse Code
 1. The length of a dot is one unit.
 2. A dash is three units.
 3. The space between parts of the same letter is one unit.
 4. The space between letters is three units.
 5. The space between words is seven units.
 */

    private Camera camera;
    private Camera.Parameters params;
    private FlashMorseAsyncTask flashMorseAsyncTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }

    private class FlashMorseAsyncTask extends AsyncTask<String, Void, Void> {

        private int oneTimeUnit = 150;
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
                            if(morseCode != null && !morseCode.equals("")) {
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
            final TextView morseText = (TextView)findViewById(R.id.morse_text);
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
            if(flashMorseAsyncTask == null || flashMorseAsyncTask.getStatus() != AsyncTask.Status.RUNNING || flashMorseAsyncTask.isCancelled()) {
                flashMorseAsyncTask = new FlashMorseAsyncTask();
                EditText textInput = ((EditText)findViewById(R.id.text_to_send));
                TextView morseText = (TextView)findViewById(R.id.morse_text);
                String text = textInput.getText().toString();
                if(!text.equals("")) {
                    textInput.setText("");
                    morseText.setText("");
                    ((TextView)findViewById(R.id.sent_text)).setText(text);
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
        if (camera == null || params == null) {
            return;
        }

        params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
    }


    // Turning Off flash
    private void turnOffFlash() {
        if (camera == null || params == null) {
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
        if(flashMorseAsyncTask != null && flashMorseAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            flashMorseAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        turnOffFlash();
        if(flashMorseAsyncTask != null && flashMorseAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            flashMorseAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(flashMorseAsyncTask != null && flashMorseAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
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
    }
}
