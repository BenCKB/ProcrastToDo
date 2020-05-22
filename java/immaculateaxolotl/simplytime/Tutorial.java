package immaculateaxolotl.simplytime;

import android.content.Intent;
import android.os.Bundle;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.util.ArrayList;
import java.util.List;

public class Tutorial extends OnboarderActivity {

    List<OnboarderPage> onboarderPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onboarderPages = new ArrayList<>();

        // Create your first page
        OnboarderPage onboarderPage1 = new OnboarderPage(R.string.tut1T, R.string.tut1D, R.drawable.tutpro);
        OnboarderPage onboarderPage2 = new OnboarderPage(R.string.tut2T, R.string.tut2D, R.drawable.tutadd);
        OnboarderPage onboarderPage3 = new OnboarderPage(R.string.tut3T, R.string.tut3D, R.drawable.tuthave);
        OnboarderPage onboarderPage4 = new OnboarderPage(R.string.tut4T, R.string.tut4D, R.drawable.tutdot);
        OnboarderPage onboarderPage5 = new OnboarderPage(R.string.tut5T, R.string.tut5D, R.drawable.tuttask);



        setSkipButtonTitle("Skip");
        setFinishButtonTitle("Finish");
        shouldDarkenButtonsLayout(true);


        onboarderPage1.setTitleTextSize(25);
        onboarderPage1.setDescriptionTextSize(15);
        onboarderPage2.setTitleTextSize(25);
        onboarderPage2.setDescriptionTextSize(15);
        onboarderPage3.setTitleTextSize(25);
        onboarderPage3.setDescriptionTextSize(15);
        onboarderPage4.setTitleTextSize(25);
        onboarderPage4.setDescriptionTextSize(15);
        onboarderPage5.setTitleTextSize(25);
        onboarderPage5.setDescriptionTextSize(15);

        onboarderPage1.setBackgroundColor(R.color.white);
        onboarderPage1.setTitleColor(R.color.black);
        onboarderPage1.setDescriptionColor(R.color.black);
        onboarderPage2.setBackgroundColor(R.color.white);
        onboarderPage2.setDescriptionColor(R.color.black);
        onboarderPage2.setTitleColor(R.color.black);
        onboarderPage3.setBackgroundColor(R.color.white);
        onboarderPage3.setDescriptionColor(R.color.black);
        onboarderPage3.setTitleColor(R.color.black);
        onboarderPage4.setBackgroundColor(R.color.white);
        onboarderPage4.setDescriptionColor(R.color.black);
        onboarderPage4.setTitleColor(R.color.black);
        onboarderPage5.setBackgroundColor(R.color.white);
        onboarderPage5.setDescriptionColor(R.color.black);
        onboarderPage5.setTitleColor(R.color.black);

        // Add your pages to the list
        onboarderPages.add(onboarderPage1);
        onboarderPages.add(onboarderPage2);
        onboarderPages.add(onboarderPage3);
        onboarderPages.add(onboarderPage4);
        onboarderPages.add(onboarderPage5);

        // And pass your pages to 'setOnboardPagesReady' method
        setOnboardPagesReady(onboarderPages);

    }
    @Override
    public void onSkipButtonPressed() {
        // Optional: by default it skips onboarder to the end
        super.onSkipButtonPressed();
    }

    @Override
    public void onFinishButtonPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
