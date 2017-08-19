package click.tagit.categorized;

import static click.tagit.detail.DetailActivity.mIsGreviance;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import click.tagit.R;
import click.tagit.data.remote.ClickTagitRESTClientSingleton;
import click.tagit.data.remote.grievance.Data;
import click.tagit.data.remote.grievance.FileInfoResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import retrofit2.HttpException;
import timber.log.Timber;

/**
 * A fragment representing a list of Items.
 * <p />
 * Activities containing this fragment MUST implement the {@link OnListCategorizeFragmentInteractionListener}
 * interface.
 */
public class CategorizedFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    RecyclerView mRecyclerView;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListCategorizeFragmentInteractionListener mListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CategorizedFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CategorizedFragment newInstance(int columnCount) {
        CategorizedFragment fragment = new CategorizedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Timber.d("setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        mIsGreviance = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListCategorizeFragmentInteractionListener) {
            mListener = (OnListCategorizeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categorized_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            mRecyclerView = (RecyclerView) view;
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated() called with: view = [" + view + "], savedInstanceState = ["
                + savedInstanceState + "]");

        mCompositeDisposable.add(ClickTagitRESTClientSingleton.INSTANCE
                .getRESTClient()
                .getFileInfo("categorized")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableObserver<FileInfoResponse>() {

                            @Override
                            public void onNext(@NonNull FileInfoResponse fileInfoResponse) {
                                Timber.d("onNext() called with: fileInfoResponse = ["
                                        + fileInfoResponse + "]");

                                if (fileInfoResponse != null & fileInfoResponse.getStatus() == 200
                                        & fileInfoResponse.getData() != null) {
                                    mRecyclerView.setLayoutManager(
                                            new LinearLayoutManager(getActivity()));
                                    mRecyclerView
                                            .setAdapter(new MyCategorizedRecyclerViewAdapter(
                                                    fileInfoResponse.getData(), mListener));
                                }
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

    @Override
    public void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListCategorizeFragmentInteractionListener {

        // TODO: Update argument type and name
        void onListCategorizeFragmentInteraction(Data data);
    }
}
