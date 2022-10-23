package com.example.demobranchdeeplink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override public void onStart() {
        super.onStart();
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).withData(getIntent() != null ? getIntent().getData() : null).init();
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        if (intent != null &&
                intent.hasExtra("branch_force_new_session") &&
                intent.getBooleanExtra("branch_force_new_session",false)) {
            Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit();
        }
    }

    private final Branch.BranchReferralInitListener branchReferralInitListener = new Branch.BranchReferralInitListener() {
        @Override
        public void onInitFinished(JSONObject linkProperties, BranchError error) {
            // do stuff with deep link data (nav to page, display content, etc)
            if (error != null) {
                Log.e("BRANCH SDK", error.toString());
            } else {
                Log.d("BRANCH DATA", linkProperties.toString());
                boolean branchLinkClicked = linkProperties.optBoolean("+clicked_branch_link");
                String deepLinkTest = linkProperties.optString("deep_link_test");
                Log.d("branchLinkClicked", Boolean.toString(branchLinkClicked));
                Log.d("deepLinkTest", deepLinkTest);
                if (branchLinkClicked && deepLinkTest.equals("other")) {
                    Intent intent = new Intent(MainActivity.this, OtherActivity.class);
                    startActivity(intent);
                }
            }
        }
    };

    public void createAndShareBranchLink(View v) {
        BranchUniversalObject buo = new BranchUniversalObject();
        LinkProperties lp = new LinkProperties()
                .addControlParameter("deep_link_test", "other");
        buo.generateShortUrl(this, lp, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    Log.i("BRANCH SDK", "got my Branch link to share: " + url);
                }
            }
        });
        ShareSheetStyle ss = new ShareSheetStyle(MainActivity.this, "Demo Branch link", "This is for the PM Tech challenge");
        buo.showShareSheet(this, lp,  ss,  new Branch.BranchLinkShareListener() {
            @Override
            public void onShareLinkDialogLaunched() {
            }
            @Override
            public void onShareLinkDialogDismissed() {
            }
            @Override
            public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
            }
            @Override
            public void onChannelSelected(String channelName) {
            }
        });
    }
}