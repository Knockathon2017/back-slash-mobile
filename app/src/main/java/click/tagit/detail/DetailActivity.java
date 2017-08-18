package click.tagit.detail;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.joda.time.DateTime;
import retrofit2.HttpException;
import timber.log.Timber;

@EActivity
public class DetailActivity extends AppCompatActivity implements OnDateSetListener {

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
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Click(R.id.button_submit)
    void submitButtonWasClicked() {
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
                , mDetailModel.category);

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
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.e(throwable, "onError() called: error");

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
                        , body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableObserver<UploadFileResponse>() {

                            @Override
                            public void onNext(@NonNull UploadFileResponse uploadFileResponse) {
                                Timber.d("onNext() called with: uploadFileResponse = ["
                                        + uploadFileResponse + "]");
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.e(throwable, "onError() called: error");

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
}
