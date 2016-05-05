package com.neekoentertainment.roadtripper.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.deezer.sdk.model.Playlist;
import com.neekoentertainment.roadtripper.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Nicolas on 5/4/2016.
 */
public class PlaylistPickerActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_picker_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.playlist_picker_activity_title));
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        if (getIntent() != null && getIntent().getParcelableArrayListExtra(SplashScreenActivity.INTENT_EXTRA_PLAYLISTS_LIST) != null) {
            final ArrayList<Playlist> playlistList = getIntent().getParcelableArrayListExtra(SplashScreenActivity.INTENT_EXTRA_PLAYLISTS_LIST);
            final ListView listView = (ListView) findViewById(R.id.playlist_listview);
            final PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(getApplicationContext(), playlistList);
            if (listView != null) {
                listView.setAdapter(playlistsAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(SplashScreenActivity.RETURN_INTENT_EXTRA_SELECTED_PLAYLIST, playlistList.get(position));
                        setResult(AppCompatActivity.RESULT_OK, returnIntent);
                        finish();
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class PlaylistsAdapter extends BaseAdapter {

        private ArrayList<Playlist> mPlaylist;
        private Context mContext;

        public PlaylistsAdapter(Context context, ArrayList<Playlist> playlists) {
            mPlaylist = playlists;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mPlaylist.size();
        }

        @Override
        public Playlist getItem(int position) {
            return mPlaylist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mPlaylist.get(position).hashCode();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.playlist_view, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.cover = (ImageView) convertView.findViewById(R.id.playlist_cover);
                viewHolder.playlistTitle = (TextView) convertView.findViewById(R.id.playlist_title);
                viewHolder.creator = (TextView) convertView.findViewById(R.id.playlist_creator);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.playlistTitle.setText(getItem(position).getTitle());
            viewHolder.playlistTitle.setSelected(true);
            viewHolder.creator.setText(getItem(position).getCreator().getName());
            viewHolder.creator.setSelected(true);
            Picasso.with(mContext).load(getItem(position).getSmallImageUrl()).fit().centerCrop().into(viewHolder.cover);
            return convertView;
        }

        private class ViewHolder {
            ImageView cover;
            TextView playlistTitle;
            TextView creator;
        }
    }
}
