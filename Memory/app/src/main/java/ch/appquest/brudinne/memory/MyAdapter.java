package ch.appquest.brudinne.memory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter class to set up the RecyclerView
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    /**
     * internal class which holds the references to all views <br>
     * and cache them within the layout for fast access
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        // all views which are used for a Card
        public ImageView    imageView;
        public ImageButton  photoButton;
        public TextView     textView;

        /**
         * the constructor is needed for the view lookups to find all subviews
         *
         * @param itemView this should be a Card
         */
        public ViewHolder(View itemView) {
            super(itemView);
            // get relevant subviews
            imageView   = itemView.findViewById(R.id.image);
            textView    = itemView.findViewById(R.id.text);
            photoButton = itemView.findViewById(R.id.newCardPhoto);
        }
    }

    // stores a list of Cards and the MainActivity for easy access
    private ArrayList<Card> thePictures;
    private MainActivity context;

    /**
     * @param pictures
     * @param context
     */
    public MyAdapter(ArrayList<Card> pictures, Context context) {
        this.context    = (MainActivity) context;
        thePictures     = pictures;
    }

    /**
     * is called by any added Card to our list
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // get the view of a Card
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View contactView        = inflater.inflate(R.layout.card_view, parent, false);
        return new ViewHolder(contactView);
    }

    /**
     * called at start and if MyAdapter gets notified to get the activity updated
     *
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on its position
        final Card card         = thePictures.get(position);
        TextView textView       = viewHolder.textView;
        ImageView imageView     = viewHolder.imageView;
        ImageButton photoButton = viewHolder.photoButton;
        // set visibilities and listener depending on the kind of instance
        if (card instanceof PictureCard) {
            // set picture and text
            textView.setText(((PictureCard) card).getDescription());
            imageView.setImageBitmap(((PictureCard) card).getPicture());
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            // "remove" button from view
            photoButton.setVisibility(View.GONE);
        } else {
            // "remove" picture and text from view
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            // enable button
            photoButton.setVisibility(View.VISIBLE);
            photoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    context.takeQrCodePicture(card);
                }
            });
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return thePictures.size();
    }

}