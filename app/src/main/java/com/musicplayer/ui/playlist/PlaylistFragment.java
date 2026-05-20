package com.musicplayer.ui.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.R;
import com.musicplayer.data.model.Song;
import com.musicplayer.viewmodel.PlayerViewModel;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private PlayerViewModel viewModel;
    private SongAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        RecyclerView recycler = view.findViewById(R.id.recycler_playlist);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(song -> {
            if (getActivity() instanceof SongSelectionListener) {
                ((SongSelectionListener) getActivity()).onSongSelected(song);
            }
        });
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        viewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) adapter.submitList(songs);
        });

        return view;
    }

    public interface SongSelectionListener {
        void onSongSelected(Song song);
    }

    // ─── RecyclerView Adapter ───
    private static class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

        private List<Song> songs;
        private final OnSongClickListener listener;

        interface OnSongClickListener {
            void onClick(Song song);
        }

        SongAdapter(OnSongClickListener listener) { this.listener = listener; }

        void submitList(List<Song> list) {
            this.songs = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Song song = songs.get(position);
            holder.title.setText(song.getTitle());
            holder.artist.setText(song.getArtist());
            long sec = song.getDuration() / 1000;
            holder.duration.setText(String.format("%d:%02d", sec / 60, sec % 60));
            holder.itemView.setOnClickListener(v -> listener.onClick(song));
        }

        @Override
        public int getItemCount() { return songs != null ? songs.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, artist, duration;
            ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.item_title);
                artist = v.findViewById(R.id.item_artist);
                duration = v.findViewById(R.id.item_duration);
            }
        }
    }
}
