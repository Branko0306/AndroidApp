package utils;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jelav.contentdelivery.R;

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
    public void onBindViewHolder(final SadrzajViewHolder holder, int position) {
        holder.bind(position);
    }

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sadrzaj, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.actionNavigate:

                    return true;
                case R.id.actionOpen:

                    return true;
                default:
            }
            return false;
        }
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

    class SadrzajViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        TextView listItemSadrzajNaziv;
        TextView listItemSadrzajSkraceniOpis;
        TextView listItemSadrzajOpis;
        TextView firmaInfo;

        Button navigateButton;
        Button otvoriButton;

        public ImageView thumbnail;
        public ImageView firmaLogo;

        public LinearLayout viewForeground;

        public SadrzajViewHolder(View itemView) {
            super(itemView);

            listItemSadrzajNaziv = (TextView) itemView.findViewById(R.id.tv_sadrzaj_naziv);
            listItemSadrzajSkraceniOpis = (TextView) itemView.findViewById(R.id.tv_sadrzaj_skraceni_opis);
            listItemSadrzajOpis = (TextView) itemView.findViewById(R.id.tv_sadrzaj_opis);
            navigateButton = (Button)itemView.findViewById(R.id.actionButtonNavigateID);
            otvoriButton= (Button)itemView.findViewById(R.id.actionButtonOpenID);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            firmaLogo = itemView.findViewById(R.id.firmaLogo);
            firmaInfo = itemView.findViewById(R.id.firmaInfo);

            viewForeground = (LinearLayout) itemView.findViewById(R.id.view_foreground);

            itemView.setOnClickListener(this);
            listItemSadrzajNaziv.setOnClickListener(this);
            listItemSadrzajSkraceniOpis.setOnClickListener(this);
            thumbnail.setOnClickListener(this);
        }

        void bind(int listIndex) {
            Sadrzaj sadrzaj = mSadrzajResponse.data.get(listIndex);
            listItemSadrzajNaziv.setText(sadrzaj.Naziv);
            listItemSadrzajSkraceniOpis.setText(sadrzaj.SkraceniOpis);
            listItemSadrzajOpis.setText(sadrzaj.DugiOpis);

            String info = "";
            if(sadrzaj.SatiOd != sadrzaj.SatiDo)
                info = String.format("%s %s m  od %d:%d do %d:%d", sadrzaj.FirmaNaziv, sadrzaj.Udaljenost, sadrzaj.SatiOd, sadrzaj.MinuteOd, sadrzaj.SatiDo, sadrzaj.MinuteDo);
            else
                info = String.format("%s %s m", sadrzaj.FirmaNaziv, sadrzaj.Udaljenost);

            firmaInfo.setText(info);

            navigateButton.setTag(sadrzaj);
            otvoriButton.setTag(sadrzaj);

            listItemSadrzajOpis.setVisibility(View.GONE);

            Glide.with(context).load(NetworkUtils.buildUriGetPicture(sadrzaj.PK))
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the original size to disk so that open will be fast
                    .skipMemoryCache(true)  // Cache everything
                    .fitCenter() // scale to fit entire image within ImageView
                    .into(thumbnail);

            Glide.with(context).load(NetworkUtils.buildUriGetLogo(sadrzaj.FirmaPK))
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the original size to disk so that open will be fast
                    .skipMemoryCache(true)  // Cache everything
                    .fitCenter() // scale to fit entire image within ImageView
                    .into(firmaLogo);
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
        }
    }

    //endregion
}
