package click.tagit.detail;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import click.tagit.R;
import click.tagit.custom.glide.GlideApp;
import click.tagit.data.remote.ClickTagitRESTClientSingleton;
import click.tagit.data.remote.uploadfile.UploadFileResponse;
import click.tagit.data.remote.uploadtext.UploadTextRequest;
import click.tagit.databinding.ActivityDetailBinding;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice;
import com.google.android.gms.location.LocationRequest;
import com.patloew.rxlocation.RxLocation;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.joda.time.DateTime;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.HttpException;
import timber.log.Timber;

@EActivity
public class DetailActivity extends AppCompatActivity implements OnDateSetListener,
        EasyPermissions.PermissionCallbacks {

    private ProgressDialog mProgressDialog;

    private static final int RC_LOCATION_FINE_AND_COARSE_PERM = 124;
    public static boolean mIsGreviance = false;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    ActivityDetailBinding mActivityDetailBinding;
    @Extra
    Uri mTempImageURI;
    @Extra
    String mTempImagePath;
    @Extra
    boolean mIsText;
    private DetailModel mDetailModel;
    private RxLocation rxLocation;
    private LocationRequest locationRequest;
    private MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        mActivityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Timber.d("onCreate() called: mTempImageURI=%s, mTempImagePath=%s, "
                        + "mActivityDetailBinding=%s, mIsText=%b, mIsGreviance=%b"
                , (mTempImageURI != null ? mTempImageURI.toString() : null),
                mTempImagePath, mActivityDetailBinding, mIsText, mIsGreviance);

        mDetailModel = new DetailModel();

        // TODO: optimise by not creating n number of GlideApp.with
        GlideApp.with(DetailActivity.this)
                .load(mTempImageURI)
                .fallback(R.drawable.ic_add_a_photo_black_24px)
                .transition(withCrossFade())
                .into(mActivityDetailBinding.imageView);
        mActivityDetailBinding.setDetailModel(mDetailModel);
        mActivityDetailBinding.setIsEnabled(mIsGreviance);
        rxLocation = new RxLocation(this);
        rxLocation.setDefaultTimeout(15, TimeUnit.SECONDS);
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Click(R.id.button_submit)
    void submitButtonWasClicked() {
        Timber.d("submitButtonWasClicked() called");

        checkLocationPermission();
    }

    @AfterPermissionGranted(RC_LOCATION_FINE_AND_COARSE_PERM)
    public void checkLocationPermission() {
        Timber.d("checkLocationPermission() called");

        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
            startLocationRefresh();
        } else {
            // Ask for both permissions
            EasyPermissions
                    .requestPermissions(this, getString(R.string.rationale_location_permission),
                            RC_LOCATION_FINE_AND_COARSE_PERM, perms);
        }
    }

    public void startLocationRefresh() {
        mCompositeDisposable.add(
                rxLocation.settings().checkAndHandleResolution(locationRequest)
                        .flatMapObservable(this::getAddressObservable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onAddressUpdate, throwable -> Log
                                .e("MainPresenter", "Error fetching location/address updates",
                                        throwable))
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult() called with: requestCode = [" + requestCode
                + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            checkLocationPermission();
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
                DetailActivity.this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Timber.d("onPermissionsGranted() called with: requestCode = ["
                + requestCode + "], perms = [" + perms + "]");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Timber.d("onPermissionsDenied() called with: requestCode = [" + requestCode + "], perms = ["
                + perms + "]");

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        // TODO: correct the below behaviour even when
        if (EasyPermissions.somePermissionPermanentlyDenied(DetailActivity.this, perms)) {
            new AppSettingsDialog.Builder(DetailActivity.this).build().show();
        } else {
            checkLocationPermission();
        }
    }

    @Click(R.id.button_predefined_category)
    void buttonPredifinedCategoryWasClicked() {
        Timber.d("buttonPredifinedCategoryWasClicked() called");

        new Builder(this)
                .title("Choose your category")
                .items(R.array.categories)
                .itemsCallbackSingleChoice(-1, new ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                            CharSequence text) {
                        Timber.d("onSelection() called with: dialog = [" + dialog + "], itemView "
                                + "= [" + itemView + "], which = [" + which + "], text = [" + text
                                + "]");
                        mDetailModel.predefinedCategory = (String) text;
                        mActivityDetailBinding.textViewPredefinedCategory.setText(text);
                        return true;
                    }
                })
                .positiveText("Choose")
                .show();
    }

    @Click(R.id.button_alarm)
    void buttonAlarmWasClicked() {
        Timber.d("button_alarm() called");

        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                DetailActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Timber.d("onDateSet() called with: view = [" + view + "], year = [" + year
                + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");

        mDetailModel.alarm = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
        mActivityDetailBinding.textViewAlarm.setText(mDetailModel.alarm);
    }

    private Observable<Address> getAddressObservable(boolean success) {
        if (success) {
            return rxLocation.location().updates(locationRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(this::onLocationUpdate)
                    .flatMap(this::getAddressFromLocation);

        } else {
            onLocationSettingsUnsuccessful();

            return rxLocation.location().lastLocation()
                    .doOnSuccess(this::onLocationUpdate)
                    .flatMapObservable(this::getAddressFromLocation);
        }
    }

    void onLocationSettingsUnsuccessful() {
        Timber.d("onLocationSettingsUnsuccessful() called");
        postData();
    }

    void postData() {
        Timber.d("postData() called");

        mMaterialDialog = new Builder(this)
                .title("In progress")
                .content("Please wait...")
                .progress(true, 0)
                .cancelable(false)
                .show();

        mDetailModel.time = DateTime.now().getMillis();
        if (!mIsGreviance) {
            mDetailModel.category = "grievances";
        } else {
            if (TextUtils.isEmpty(mDetailModel.category)) {
                mDetailModel.category = mDetailModel.predefinedCategory;
            }
        }
        Timber.d("submitButtonWasClicked() called: %s, %s, %s, %d, %s, %s", mDetailModel.tags,
                mDetailModel.description, mDetailModel.alarm, mDetailModel.time
                , mDetailModel.category, mDetailModel.predefinedCategory);

        if (mIsText) {
            uploadText();
        } else {
            uploadFile();
        }
    }

    private void uploadText() {
        Timber.d("uploadText() called");

        UploadTextRequest uploadTextRequest = new UploadTextRequest(mDetailModel.tags
                , mDetailModel.description
                , mDetailModel.alarm
                , mDetailModel.time
                , mDetailModel.category
                , mDetailModel.latitude
                , mDetailModel.longitude
                , mDetailModel.postalCode
                , mDetailModel.locality
                , mDetailModel.address);

        mCompositeDisposable.add(ClickTagitRESTClientSingleton.INSTANCE
                .getRESTClient()
                .postGText(uploadTextRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableObserver<UploadFileResponse>() {

                            @Override
                            public void onNext(@NonNull UploadFileResponse uploadFileResponse) {
                                Timber.d("onNext() called with: uploadFileResponse = ["
                                        + uploadFileResponse + "]");

                                mMaterialDialog.dismiss();
                                finish();
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.e(throwable, "onError() called: error");

                                mMaterialDialog.dismiss();
                                // TODO: need error handling
                                if (throwable instanceof HttpException) {
                                    // We had non-2XX http error
                                }
                                if (throwable instanceof IOException) {
                                    // A network or conversion error happened
                                }
                            }

                            @Override
                            public void onComplete() {
                                Timber.d("onComplete() called");
                                mMaterialDialog.dismiss();
                                finish();
                            }
                        }));
    }

    private void uploadFile() {
        // TODO: don't create if file array size is null
        MultipartBody.Part body = prepareFilePart("display", Uri.parse(mTempImageURI.toString())
                , mTempImagePath);

        mCompositeDisposable.add(ClickTagitRESTClientSingleton.INSTANCE
                .getRESTClient()
                .multipartUploadFile(createPartFromString(mDetailModel.tags)
                        , createPartFromString(mDetailModel.description)
                        , createPartFromString(mDetailModel.alarm)
                        , createPartFromString(String.valueOf(mDetailModel.time))
                        , createPartFromString(mDetailModel.category)
                        , createPartFromString(String.valueOf(mDetailModel.latitude))
                        , createPartFromString(String.valueOf(mDetailModel.longitude))
                        ,createPartFromString(mDetailModel.postalCode)
                        ,createPartFromString(mDetailModel.locality)
                        ,createPartFromString(mDetailModel.address)
                        , body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableObserver<UploadFileResponse>() {

                            @Override
                            public void onNext(@NonNull UploadFileResponse uploadFileResponse) {
                                Timber.d("onNext() called with: uploadFileResponse = ["
                                        + uploadFileResponse + "]");
                                mMaterialDialog.dismiss();
                                finish();
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.e(throwable, "onError() called: error");

                                mMaterialDialog.dismiss();
                                // TODO: need error handling
                                if (throwable instanceof HttpException) {
                                    // We had non-2XX http error
                                }
                                if (throwable instanceof IOException) {
                                    // A network or conversion error happened
                                }
                            }

                            @Override
                            public void onComplete() {
                                Timber.d("onComplete() called");
                                mMaterialDialog.dismiss();
                                finish();
                            }
                        }));
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri
            , String fileAbsolutePath) {
        Timber.d("prepareFilePart() called with: partName = [" + partName + "], fileUri = ["
                + fileUri
                + "], fileAbsolutePath = [" + fileAbsolutePath + "]");

        // use the FileUtils to get the actual file by uri
        File file = new File(fileAbsolutePath);

        Timber.d("prepareFilePart() called with: file.size = [" + file.length() + "]");
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        if (TextUtils.isEmpty(descriptionString)) {
            return null;
        }
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    private Observable<Address> getAddressFromLocation(Location location) {
        return rxLocation.geocoding().fromLocation(location).toObservable()
                .subscribeOn(Schedulers.io());
    }

    void onAddressUpdate(Address address) {
        Timber.d("onAddressUpdate() called with: address = [" + address + "]");

        mDetailModel.latitude = String.valueOf(address.getLatitude());
        mDetailModel.longitude = String.valueOf(address.getLongitude());
        mDetailModel.postalCode = address.getPostalCode();
        mDetailModel.locality = address.getLocality();
        mDetailModel.address = getAddressText(address);
        postData();
    }

    private String getAddressText(Address address) {
        Timber.d("getAddressText() called with: address = [" + address + "]");

        String addressText = "";
        final int maxAddressLineIndex = address.getMaxAddressLineIndex();

        for (int i = 0; i <= maxAddressLineIndex; i++) {
            addressText += address.getAddressLine(i);
            if (i != maxAddressLineIndex) {
                addressText += " ";
            }
        }

        Timber.d("getAddressText() called: addressText = [" + addressText + "]");
        return addressText;
    }

    void onLocationUpdate(Location location) {
        Timber.d("onLocationUpdate() called with: location = [" + location + "]");
    }
}
