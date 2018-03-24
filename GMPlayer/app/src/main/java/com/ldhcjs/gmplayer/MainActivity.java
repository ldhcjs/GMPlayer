package com.ldhcjs.gmplayer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int LOADER_ID = 10000;
    public static final String TAG = "GMPlayer";

    private RecyclerView mRecyclerView;
    private AudioAdapter mAudioAdapter;

    private TextView mTxtTitle;
    private ImageView mImgAlbumArt;
    private ImageButton mBtnPlayPause;
    private ImageButton mBtnForward;
    private ImageButton mBtnRewind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OS 6.0 이상의 경우 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {
                // READ External Stroage 권한이 있음.
                getAudioListFromMediaDatabase();
            }
        } else {
            //  OS 가 6.0 이전의 경우 권한 확인 필요없음.
            getAudioListFromMediaDatabase();
        }



        mTxtTitle = (TextView) findViewById(R.id.txt_title);
        mImgAlbumArt = (ImageView) findViewById(R.id.img_albumart);
        mBtnPlayPause = (ImageButton) findViewById(R.id.btn_play_pause);
        mBtnForward = (ImageButton) findViewById(R.id.btn_forward);
        mBtnRewind = (ImageButton) findViewById(R.id.btn_rewind);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        mBtnPlayPause.setOnClickListener(this);
        mBtnForward.setOnClickListener(this);
        mBtnRewind.setOnClickListener(this);

        registerBroadcast();
        updateUI();

        mAudioAdapter = new AudioAdapter(this, null);
        mRecyclerView.setAdapter(mAudioAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "뮤직 재생 준비 완료", Toast.LENGTH_SHORT).show();
            getAudioListFromMediaDatabase();
        }
    }

    private void getAudioListFromMediaDatabase() {

        /*
         * onCreateLoader 에서는 실제 DB의 값을 조회하는 Query 내용을 작성해 주면 됩니다.
         * projection : Media Database에서 조회하고자 하는 컬럼 ID입니다.
         * selection : 조건문 입니다. (db 에서 where절과 동일하며 IS_MUSIC값이 1인 내용만 조회 합니다.)
         * sortOrder : order 조건 값 입니다. (오디오 타이틀 기준으로 로케일 순으로 정렬 됩니다. 특수문자->한글->영어)
         * onLoadFinished 에서는 조회 결과가 Cursor를 통해 저장되어 리턴 됩니다.

         * 만약 Activity가 아니라 Fragment에서 LoaderManger를 사용하려고 하면 아래처럼 사용하시면 됩니다,
         * getLoaderManager().initLoader(int id, Bundle args, LoaderCallbacks<D> callback);
         */

        getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                };
                String selection = MediaStore.Audio.Media.IS_MUSIC + " =1";
                String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
                return new CursorLoader(getApplicationContext(), uri, projection, selection, null, sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data != null && data.getCount() > 0) {
                    while (data.moveToNext()) {
                        Log.i(TAG, "Title:" + data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    }
                    mAudioAdapter.swapCursor(data);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAudioAdapter.swapCursor(null);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_miniplayer:

                break;

            case R.id.btn_rewind:
                AudioApplication.getInstance().getServiceInterface().rewind();
                break;

            case R.id.btn_play_pause:
                AudioApplication.getInstance().getServiceInterface().togglePlay();
                break;

            case R.id.btn_forward:
                AudioApplication.getInstance().getServiceInterface().forward();
                break;
        }
    }

    private void updateUI() {
        if(AudioApplication.getInstance().getServiceInterface().isPlaying()) {
            mBtnPlayPause.setImageResource(R.drawable.ic_play_on);
        } else {
            mBtnPlayPause.setImageResource(R.drawable.ic_play_off);
        }

        AudioAdapter.AudioItem audioItem = AudioApplication.getInstance().getServiceInterface().getAudioItem();

        if(audioItem != null) {
            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), audioItem.mAlbumId);
            Glide.with(getApplicationContext()).load(albumArtUri).into(mImgAlbumArt);
            mTxtTitle.setText(audioItem.mTitle);
        } else {
            mImgAlbumArt.setImageResource(R.drawable.ic_default_player);
            mTxtTitle.setText("재생중인 음악이 없습니다.");
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    public void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void unregisterBroadcast() {
        unregisterReceiver(mBroadcastReceiver);
    }
}
