package usage.ywb.personal.circlelayout.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import usage.ywb.personal.circlelayout.R;

/**
 * @author Kingdee.ywb
 * @version [ V.2.6.2  2019/8/20 ]
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuHolder> {


    private LayoutInflater inflater;
    private String[] stringSet;


    public MenuAdapter(Context context, String[] stringSet) {
        inflater = LayoutInflater.from(context);
        this.stringSet = stringSet;
    }

    @NonNull
    @Override
    public MenuHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuHolder(inflater.inflate(R.layout.item_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MenuHolder holder, int position) {
        holder.nameTv.setText(stringSet[position]);
    }

    @Override
    public int getItemCount() {
        return stringSet == null ? 0 : stringSet.length;
    }

    class MenuHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTv;

        MenuHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.name_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener(v, getAdapterPosition());
        }
    }

    protected void onItemClickListener(View view, int position){

    }

}
