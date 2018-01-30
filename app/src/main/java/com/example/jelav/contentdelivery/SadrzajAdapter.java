package com.example.jelav.contentdelivery;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**W
 * Created by jelav on 29/12/2017.
 */

public class SadrzajAdapter extends RecyclerView.Adapter<SadrzajAdapter.SadrzajViewHolder> {

    private static final String TAG = SadrzajAdapter.class.getSimpleName();

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(Sadrzaj sadrzaj);
    }

    private SadrzajResponse mSadrzajResponse;

    public SadrzajAdapter(ListItemClickListener listener){
        mOnClickListener = listener;
    }

    @Override
    public SadrzajViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.sadrzaj_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        view.setFocusable(true);

        SadrzajViewHolder viewHolder = new SadrzajViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SadrzajViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if(mSadrzajResponse == null)
            return 0;

        if(mSadrzajResponse.data != null){
            return mSadrzajResponse.data.size();
        }else {
            return 0;
        }
    }

    public void setSadrzajData(SadrzajResponse sadrzajResponse) {
        mSadrzajResponse = sadrzajResponse;
        notifyDataSetChanged();
    }

    class SadrzajViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // COMPLETED (13) Within NumberViewHolder, create a TextView variable called listItemNumberView
        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView listItemSadrzajView;

        public SadrzajViewHolder(View itemView) {
            // COMPLETED (15) Within the constructor, call super(itemView) and then find listItemNumberView by ID
            super(itemView);

            listItemSadrzajView = (TextView) itemView.findViewById(R.id.tv_sadrzaj_item);
            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            // COMPLETED (17) Within bind, set the text of listItemNumberView to the listIndex
            // COMPLETED (18) Be careful to get the String representation of listIndex, as using setText with an int does something different

            Sadrzaj sadrzaj = mSadrzajResponse.data.get(listIndex);
            listItemSadrzajView.setText(sadrzaj.naziv);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            Sadrzaj sadrzaj = mSadrzajResponse.data.get(clickedPosition);
            mOnClickListener.onListItemClick(sadrzaj);
        }
    }
}
