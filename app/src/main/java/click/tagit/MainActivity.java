package click.tagit;

import static click.tagit.detail.DetailActivity.mIsGreviance;

import android.Manifest.permission;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import click.tagit.categorized.CategorizedFragment.OnListCategorizeFragmentInteractionListener;
import click.tagit.categorized.dummy.DummyContent.DummyItem;
import click.tagit.data.remote.grievance.Data;
import click.tagit.detail.DetailActivity;
import click.tagit.detail.DetailActivity_;
import click.tagit.grievance.GrievanceFragment.OnListGrievanceFragmentInteractionListener;
import click.tagit.grievance.dummy.DummyContent;
import click.tagit.uncategorized.UncategorizedFragment.OnListUncategorizedFragmentInteractionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        OnListCategorizeFragmentInteractionListener, OnListGrievanceFragmentInteractionListener,
        OnListUncategorizedFragmentInteractionListener, EasyPermissions.PermissionCallbacks {

    private static final int RC_CAMERA_PERM = 126;
    private Uri mTempImageURI;
    private String mTempImagePath;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if(!mIsGreviance){
                        DetailActivity_.intent(MainActivity.this).mIsText(true).start();
                        return true;
                    }
                    return false;
                case R.id.navigation_dashboard:
                    mTempImageURI = null;
                    mTempImagePath = null;
                    checkCameraPermission();
                    return true;
                case R.id.navigation_notifications:
                    if(!mIsGreviance){
                        return true;
                    }
                    return false;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(DummyItem item) {

    }

    @Override
    public void onListFragmentInteraction(Data data) {
        Timber.d("onListFragmentInteraction() called with: data = [" + data + "]");

    }

    @Override
    public void onListFragmentInteraction(
            click.tagit.uncategorized.dummy.DummyContent.DummyItem item) {

    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    public void checkCameraPermission() {
        Timber.d("checkCameraPermission() called");

        String[] perms = {permission.CAMERA};
        if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {
            Timber.d("checkCameraPermission() called: has permission");
            // Have permissions, do the thing!
            captureCamera();
        } else {
            Timber.d("checkCameraPermission() called: rationale permission");
            // Ask for both permissions
            EasyPermissions.requestPermissions(this,
                    getString(R.string.rationale_camera_permission),
                    RC_CAMERA_PERM, perms);
        }
    }

    private void captureCamera() {
        Timber.d("captureCamera() called");

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Timber.d(ex, "captureCamera() called");
            // TODO: show error or toast
            return;
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = null;
            photoURI = FileProvider.getUriForFile(MainActivity.this, getPackageName()
                    + getString(R.string.provider), photoFile);

            mTempImageURI = photoURI;
            mTempImagePath = photoFile.getAbsolutePath();
            Timber.d("captureCamera() called: photoURI = %s", photoURI.toString());
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); // set the image file name

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        } else {
            // TODO: show error or toast
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = new File(getFilesDir(),
                getString(R.string.dir_camera));

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Timber.e("createImageFile() called: error in creating dirs");
                return null;
            } else {
                Timber.d("createImageFile() called: storageDir created");
            }
        } else {
            Timber.d("createImageFile() called: storageDir already exists");
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File capturedImage = new File(storageDir, timeStamp + ".jpeg");
        if (!capturedImage.createNewFile()) {
            Timber.e("createImageFile() called: error in creating capturedImage file");
        } else {
            Timber.d("createImageFile() called: capturedImage file created");
        }
        Timber.d("createImageFile() called: capturedImage file path is %s",
                capturedImage.getPath());
        return capturedImage;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult() called with: requestCode = [" + requestCode
                + "], resultCode = ["
                + resultCode + "], data = [" + data + "]");

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.d("onActivityResult: capture photo success");

                DetailActivity_.intent(MainActivity.this).mTempImagePath(mTempImagePath)
                        .mTempImageURI(mTempImageURI).start();
            } else { // Result was a failure
                Timber.e("onActivityResult: capture photo failure");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.d("onRequestPermissionsResult() called with: requestCode = [" + requestCode
                + "], permissions = [" + permissions + "], grantResults = [" + grantResults + "]");

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Timber.d(
                "onPermissionsGranted() called with: requestCode = [" + requestCode + "], perms = ["
                        + perms + "]");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Timber.d("onPermissionsDenied() called with: requestCode = [" + requestCode + "], perms = ["
                + perms + "]");
    }
}
