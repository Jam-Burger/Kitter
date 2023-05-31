package com.jamburger.kitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.activities.MainActivity;
import com.jamburger.kitter.components.Post;

import java.util.List;

public class MyKittAdapter extends RecyclerView.Adapter<MyKittAdapter.ViewHolder> {
    Context mContext;
    List<DocumentReference> mPosts;

    public MyKittAdapter(Context mContext, List<DocumentReference> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_my_kitt, parent, false);
        return new MyKittAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentReference postReference = mPosts.get(position);
        postReference.get().addOnSuccessListener(postSnapshot -> {
            Post post = postSnapshot.toObject(Post.class);
            if (!post.getKitt().isEmpty()) {
                holder.kitt.setText(post.getKitt());
                holder.time.setText(MainActivity.dateIdToString(post.getPostid()));
                holder.container.setVisibility(View.VISIBLE);
            } else {
                mPosts.remove(postReference);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView kitt, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            kitt = itemView.findViewById(R.id.txt_kitt);
            time = itemView.findViewById(R.id.txt_time);
            container = itemView.findViewById(R.id.container_my_kitt);
            container.setVisibility(View.GONE);
        }
    }
}
