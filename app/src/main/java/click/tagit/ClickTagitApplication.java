package click.tagit;

import android.app.Application;
import click.tagit.data.remote.ClickTagitRESTClientSingleton;
import com.facebook.stetho.Stetho;
import timber.log.Timber;

/**
 *   * User: Anurag Singh
 *   * Date: 18/8/17
 *   * Time: 19:02 PM
 */
public class ClickTagitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        plantTimber();
        // Initialize network library
        ClickTagitRESTClientSingleton.INSTANCE.init();

        Stetho.initializeWithDefaults(this);
    }

    /**
     * Initialize Timber logging
     */
    private void plantTimber() {
        Timber.plant(new Timber.DebugTree() {
            //Add the line number to the tag.
            //Will only work if minifyEnabled is set to false in app.gradle
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
        });
        Timber.d("plantTimber() called");
    }
}
