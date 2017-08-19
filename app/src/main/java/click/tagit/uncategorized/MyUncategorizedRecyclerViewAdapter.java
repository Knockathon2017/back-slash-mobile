package click.tagit.uncategorized;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import click.tagit.R;
import click.tagit.custom.glide.GlideApp;
import click.tagit.data.remote.grievance.Data;
import click.tagit.databinding.FragmentUncategorizedBinding;
import click.tagit.uncategorized.UncategorizedFragment.OnListUncategorizedFragmentInteractionListener;
import com.android.databinding.library.baseAdapters.BR;
import java.util.List;
import timber.log.Timber;

public class MyUncategorizedRecyclerViewAdapter extends
        RecyclerView.Adapter<MyUncategorizedRecyclerViewAdapter.ViewHolder> {

    private final List<Data> mDataList;
    private final OnListUncategorizedFragmentInteractionListener mListener;

    public MyUncategorizedRecyclerViewAdapter(List<Data> dataList,
            OnListUncategorizedFragmentInteractionListener listener) {
        mDataList = dataList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d("onCreateViewHolder() called with: parent = [" + parent + "], viewType = ["
                + viewType + "]");
        LayoutInflater layoutInflater =
                LayoutInflater.from(parent.getContext());
        FragmentUncategorizedBinding itemBinding =
                FragmentUncategorizedBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(final MyUncategorizedRecyclerViewAdapter.ViewHolder holder,
            int position) {
        Timber.d("onBindViewHolder() called with: holder = "
                + "[" + holder + "], position = [" + position
                + "]");
        final Data data = mDataList.get(position);
        if (null != data) {
            holder.bind(data);
            GlideApp.with(holder.binding.getRoot().getContext())
                    .load(data.getThumbnail())
                    .fallback(R.drawable.ic_add_a_photo_black_24px)
                    .error(R.drawable.ic_add_a_photo_black_24px)
                    .transition(withCrossFade())
                    .into((ImageView) holder.binding.getRoot()
                            .findViewById(R.id.image_view_thumbnail));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding binding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj) {
            binding.setVariable(BR.dataUnCategorized, obj);
            binding.setVariable(BR.dataListListenerUnCategorized, mListener);
            binding.executePendingBindings();
        }
    }
}
