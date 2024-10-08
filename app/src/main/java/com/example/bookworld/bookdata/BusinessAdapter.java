package com.example.bookworld.bookdata;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnBookClickListener listener;

    public BusinessAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText("by " + book.getAuthor());
        holder.price.setText("Ksh " + book.getPrice()); // Set price directly as a string
        Picasso.get().load(book.getThumbnailUrl()).into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView author;
        TextView price;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.bookThumbnail);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            price = itemView.findViewById(R.id.bookPrice); // Reference to book price TextView in item layout
        }
    }

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }
}
