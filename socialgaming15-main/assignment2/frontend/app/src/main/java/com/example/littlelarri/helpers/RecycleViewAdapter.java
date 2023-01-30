package com.example.littlelarri.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.littlelarri.R;
import com.example.littlelarri.Player;

import java.util.ArrayList;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    Context context;
    ArrayList<Player> players;
    boolean isRankingList; // false: list of friends, true: ranking list
    boolean showExtraUID; // false: firebaseUID is not shown under nickname, true: firebaseUID is shown under nickname

    public RecycleViewAdapter(Context context, ArrayList<Player> players, boolean isRankingList, boolean showExtraUID, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.players = players;
        this.isRankingList = isRankingList;
        this.showExtraUID = showExtraUID;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public static RecycleViewAdapter emptyAdapter(Context context) {
        return new RecycleViewAdapter(context, new ArrayList<Player>(), false, false, null);
    }

    @NonNull
    @Override
    public RecycleViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if (showExtraUID)
            view = inflater.inflate(R.layout.recycler_view_row2, parent, false);
        else
            view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new RecycleViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.MyViewHolder holder, int position) {

        if (isRankingList) // if listType is the ranking list, we want to show the ranking numbers on the left
            holder.listRank.setText(position + 1 + "");
        else // if listType is the friend list, we don't want to show the ranking numbers
            holder.listRank.setText("");

        holder.listNickname.setText(players.get(position).getNickname());

        holder.listLevel.setText((int) players.get(position).getLevel() + "");

        if (showExtraUID)
            holder.listUID.setText("#" + players.get(position).getUID());

        if (players.get(position).isCurrentUser())
            holder.listCardView.setBackgroundColor(context.getResources().getColor(R.color.grey, null));
        else
            holder.listCardView.setBackgroundColor(context.getResources().getColor(R.color.white, null));
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView listRank;
        TextView listNickname;
        TextView listLevel;
        TextView listUID;
        CardView listCardView;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            listRank = itemView.findViewById(R.id.listRank);
            listNickname = itemView.findViewById(R.id.listNickname);
            listLevel = itemView.findViewById(R.id.listLevel);
            listUID = itemView.findViewById(R.id.listUID);
            listCardView = itemView.findViewById(R.id.cardView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
