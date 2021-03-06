package click.tagit.uncategorized;

import static click.tagit.detail.DetailActivity.mIsGreviance;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import click.tagit.R;
import click.tagit.custom.view.DividerItemDecoration;
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
 * Activities containing this fragment MUST implement the {@link OnListUncategorizedFragmentInteractionListener}
 * interface.
 */
public class UncategorizedFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    RecyclerView mRecyclerView;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListUncategorizedFragmentInteractionListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UncategorizedFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static UncategorizedFragment newInstance(int columnCount) {
        UncategorizedFragment fragment = new UncategorizedFragment();
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
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mSwipeRefreshLayout) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListUncategorizedFragmentInteractionListener) {
            mListener = (OnListUncategorizedFragmentInteractionListener) context;
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
        View view = inflater.inflate(R.layout.fragment_uncategorized_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated() called with: view = [" + view + "], savedInstanceState = ["
                + savedInstanceState + "]");

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });
        refreshItems();
    }

    private void refreshItems() {
        Timber.d("refreshItems() called");
        mCompositeDisposable.add(ClickTagitRESTClientSingleton.INSTANCE
                .getRESTClient()
                .getFileInfo("uncategorized")
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
                                    RecyclerView.ItemDecoration itemDecoration = new
                                            DividerItemDecoration(getActivity(),
                                            DividerItemDecoration
                                                    .VERTICAL_LIST);
                                    mRecyclerView
                                            .addItemDecoration(itemDecoration);
                                    mRecyclerView.setLayoutManager(
                                            new LinearLayoutManager(getActivity()));
                                    mRecyclerView
                                            .setAdapter(new MyUncategorizedRecyclerViewAdapter(
                                                    fileInfoResponse.getData(), mListener));
                                }
                                mSwipeRefreshLayout.setRefreshing(false);
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Timber.e(throwable, "onError() called: error");

                                mSwipeRefreshLayout.setRefreshing(false);
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
                                mSwipeRefreshLayout.setRefreshing(false);
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
    public interface OnListUncategorizedFragmentInteractionListener {

        // TODO: Update argument type and name
        void onListUncategorizedFragmentInteraction(Data data);
    }
}
