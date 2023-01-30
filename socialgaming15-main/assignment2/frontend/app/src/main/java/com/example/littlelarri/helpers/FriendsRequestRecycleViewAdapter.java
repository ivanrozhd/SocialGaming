package com.example.littlelarri.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.littlelarri.Player;
import com.example.littlelarri.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class FriendsRequestRecycleViewAdapter extends RecyclerView.Adapter<FriendsRequestRecycleViewAdapter.MyViewHolder> {
    private final RecyclerViewInterface cardInterface;
    private final RecyclerViewInterface rejectButtonInterface;
    private final RecyclerViewInterface acceptButtonInterface;

    Context context;
    ArrayList<Player> players;
    FirebaseAuth firebaseAuth;
    PlayerVolleyHelper playerVolleyHelper;

    public FriendsRequestRecycleViewAdapter(Context context, ArrayList<Player> players, RecyclerViewInterface cardInterface,
                                            RecyclerViewInterface rejectButtonInterface, RecyclerViewInterface acceptButtonInterface) {
        this.context = context;
        this.players = players;
        this.cardInterface = cardInterface;
        this.rejectButtonInterface = rejectButtonInterface;
        this.acceptButtonInterface = acceptButtonInterface;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.playerVolleyHelper = new PlayerVolleyHelper(context);
    }

    public static FriendsRequestRecycleViewAdapter emptyAdapter(Context context) {
        return new FriendsRequestRecycleViewAdapter(context, new ArrayList<Player>(), null, null, null);
    }

    @NonNull
    @Override
    public FriendsRequestRecycleViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.friend_requests_recyclerview_row, parent, false);
        return new FriendsRequestRecycleViewAdapter.MyViewHolder(view, cardInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsRequestRecycleViewAdapter.MyViewHolder holder, int position) {
        holder.requestNickname.setText(players.get(position).getNickname());

        holder.acceptButton.setOnClickListener(v -> acceptButtonInterface.onItemClick(position));

        holder.rejectButton.setOnClickListener(v -> rejectButtonInterface.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView requestNickname;
        Button acceptButton;
        Button rejectButton;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            requestNickname = itemView.findViewById(R.id.requestNickname);
            acceptButton = itemView.findViewById(R.id.acceptFriendButton);
            rejectButton = itemView.findViewById(R.id.rejectFriendButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION)
                            recyclerViewInterface.onItemClick(pos);
                    }
                }
            });
        }
    }
}
