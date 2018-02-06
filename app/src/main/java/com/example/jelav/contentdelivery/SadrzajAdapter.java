package com.example.jelav.contentdelivery;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

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

        TextView listItemSadrzajView;
        TextView listItemSadrzajOpis;
        LinearLayout linearButtons;
        Button navigateButton;
        Button otvoriURLButton;

        public SadrzajViewHolder(View itemView) {
            super(itemView);

            listItemSadrzajView = (TextView) itemView.findViewById(R.id.tv_sadrzaj_item);
            listItemSadrzajOpis = (TextView) itemView.findViewById(R.id.tv_sadrzaj_opis);
            navigateButton = (Button)itemView.findViewById(R.id.btnNavigateMap);
            otvoriURLButton= (Button)itemView.findViewById(R.id.btnOtvoriURL);
            linearButtons = (LinearLayout)itemView.findViewById(R.id.buttons);

            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            Sadrzaj sadrzaj = mSadrzajResponse.data.get(listIndex);
            listItemSadrzajView.setText(sadrzaj.naziv);
            listItemSadrzajOpis.setText(sadrzaj.opis);
            navigateButton.setTag(sadrzaj);
            otvoriURLButton.setTag(sadrzaj);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            Sadrzaj sadrzaj = mSadrzajResponse.data.get(clickedPosition);

            if(listItemSadrzajOpis.getVisibility() == View.GONE){
                listItemSadrzajOpis.setVisibility(View.VISIBLE);
            }else {
                listItemSadrzajOpis.setVisibility(View.GONE);
            }

            if(linearButtons.getVisibility() == View.GONE){
                linearButtons.setVisibility(View.VISIBLE);
            }else {
                linearButtons.setVisibility(View.GONE);
            }

            mOnClickListener.onListItemClick(sadrzaj);
        }
    }
}
