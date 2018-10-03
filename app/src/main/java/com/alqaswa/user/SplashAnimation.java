package com.alqaswa.user;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.alqaswa.user.Utils.splash.SplashAnimationHelper;

import java.util.ArrayList;

/**
 * Created by Mahesh on 7/19/2017.
 */

public class SplashAnimation extends AppCompatActivity {
    RelativeLayout splashAnimationLayout;
    private SplashAnimationHelper.SplashRouteAnimation splashRouteAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_animation);
        splashAnimationLayout = (RelativeLayout)findViewById(R.id.splashAnimationLayout);
        animateToHomeScreen();
        startProgressAnimation();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashAnimation.this, WelcomeActivity.class);
                startActivity(i);

                // close this activity
                finish();

            }
        },9000);
    }



    private void startProgressAnimation()
    {
        {
            this.splashRouteAnimation = new SplashAnimationHelper().createSplashAnimation(this);
            this.splashRouteAnimation.startAnimation(this.splashAnimationLayout);
        }
    }


    private void animateToHomeScreen()
    {

        AnimatorSet localAnimatorSet1 = new AnimatorSet();
        AnimatorSet localAnimatorSet2 = new AnimatorSet();
        AnimatorSet localAnimatorSet3 = new AnimatorSet();
        ArrayList localArrayList1 = new ArrayList();
        ArrayList localArrayList2 = new ArrayList();
        localArrayList1.add(ObjectAnimator.ofFloat(this.splashAnimationLayout, "alpha", new float[] { 1.0F, 0.0F }));

        localAnimatorSet2.setDuration(20000);
        localAnimatorSet2.playTogether(localArrayList1);

        localAnimatorSet3.playSequentially(localArrayList2);
        localAnimatorSet3.setDuration(500L);
        localAnimatorSet3.setStartDelay(50L);
        localAnimatorSet1.playSequentially(new Animator[] { localAnimatorSet2, localAnimatorSet3 });
        localAnimatorSet1.addListener(new Animator.AnimatorListener()
        {
            public void onAnimationCancel(Animator paramAnonymousAnimator) {}

            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
                if (SplashAnimation.this.splashRouteAnimation != null) {
                   // SplashAnimation.this.splashRouteAnimation.stopAnimation();
                }
            }

            public void onAnimationRepeat(Animator paramAnonymousAnimator) {}

            public void onAnimationStart(Animator paramAnonymousAnimator) {}
        });
        localAnimatorSet1.start();
    }


    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }



}
