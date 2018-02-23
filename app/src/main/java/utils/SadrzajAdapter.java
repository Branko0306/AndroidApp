package utils;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jelav.contentdelivery.R;

import java.io.IOException;
import java.net.URL;

import database.ApplicationDatabase;
import database.SadrzajLogEntity;
import database.SadrzajLogEntityDao;
import models.Sadrzaj;
import models.SadrzajResponse;
import network.NetworkUtils;

/**W
 * Created by jelav on 29/12/2017.
 */

public class SadrzajAdapter extends RecyclerView.Adapter<SadrzajAdapter.SadrzajViewHolder> {

    private static final String TAG = SadrzajAdapter.class.getSimpleName();

    private final Context context;

    public interface ListItemClickListener {
        void onListItemClick(Sadrzaj sadrzaj);
    }

    private SadrzajResponse mSadrzajResponse;

    public SadrzajAdapter(Context context){
        this.context=context;
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

    public void removeItem(int position) {
        mSadrzajResponse.data.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Sadrzaj item, int position) {
        mSadrzajResponse.data.add(position, item);
        notifyItemInserted(position);
    }

    public Sadrzaj getItem(int position){
        return mSadrzajResponse.data.get(position);
    }

    //region SadrzajViewHolder

    class SadrzajViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView listItemSadrzajView;
        TextView listItemSadrzajOpis;
        Button navigateButton;
        Button otvoriURLButton;
        public ImageView thumbnail;
        public ConstraintLayout viewForeground;

        public SadrzajViewHolder(View itemView) {
            super(itemView);

            listItemSadrzajView = (TextView) itemView.findViewById(R.id.tv_sadrzaj_item);
            listItemSadrzajOpis = (TextView) itemView.findViewById(R.id.tv_sadrzaj_opis);
            navigateButton = (Button)itemView.findViewById(R.id.btnNavigateMap);
            otvoriURLButton= (Button)itemView.findViewById(R.id.btnOtvoriURL);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            viewForeground = itemView.findViewById(R.id.view_foreground);

            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            Sadrzaj sadrzaj = mSadrzajResponse.data.get(listIndex);
            listItemSadrzajView.setText(sadrzaj.naziv);
            listItemSadrzajOpis.setText(sadrzaj.opis);

            navigateButton.setTag(sadrzaj);
            otvoriURLButton.setTag(sadrzaj);

            Glide.with(context).load(NetworkUtils.buildUriGetPicture(sadrzaj.pk))
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the original size to disk so that open will be fast
                    .skipMemoryCache(true)  // Cache everything
                    .fitCenter() // scale to fit entire image within ImageView
                    .into(thumbnail);
        }



        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            Sadrzaj sadrzaj = mSadrzajResponse.data.get(clickedPosition);

            if(listItemSadrzajOpis.getVisibility() == View.GONE){
                listItemSadrzajOpis.setVisibility(View.VISIBLE);
                otvoriURLButton.setVisibility(View.VISIBLE);
                navigateButton.setVisibility(View.VISIBLE);
            }else {
                listItemSadrzajOpis.setVisibility(View.GONE);
                otvoriURLButton.setVisibility(View.GONE);
                navigateButton.setVisibility(View.GONE);
            }
        }
    }

    //endregion
}
