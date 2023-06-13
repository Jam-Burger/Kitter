package com.jamburger.kitter.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.jamburger.kitter.R;
import com.jamburger.kitter.components.Post;
import com.jamburger.kitter.utilities.DateFormatter;

import java.util.Comparator;
import java.util.TreeSet;

public class MyKittAdapter extends RecyclerView.Adapter<MyKittAdapter.ViewHolder> {
    Context mContext;
    TreeSet<DocumentReference> mPosts;

    public MyKittAdapter(Context mContext) {
        this.mContext = mContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPosts = new TreeSet<>(Comparator.comparing(DocumentReference::getId).reversed());
        }
    }

    public void addPost(DocumentReference post) {
        mPosts.add(post);
        notifyDataSetChanged();
    }

    public void clearPosts() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_my_kitt, parent, false);
        return new MyKittAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentReference postReference = mPosts.toArray(new DocumentReference[0])[position];

        Dialog dialog = new Dialog(mContext);
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        dialog.setContentView(R.layout.dialog_my_kitt_preview);
        TextView previewTextView = dialog.findViewById(R.id.kitt_mypost);

        postReference.get().addOnSuccessListener(postSnapshot -> {
            Post post = postSnapshot.toObject(Post.class);
            assert post != null;
            holder.kitt.setText(post.getKitt());
            holder.time.setText(DateFormatter.getTimeDifference(post.getPostid()));
            holder.container.setVisibility(View.VISIBLE);
            previewTextView.setText(holder.kitt.getText().toString());
        });

        holder.container.setOnLongClickListener(v -> {
            dialog.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView kitt, time;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            kitt = itemView.findViewById(R.id.txt_kitt);
            time = itemView.findViewById(R.id.txt_time);
            container = itemView.findViewById(R.id.container);
        }
    }
}
