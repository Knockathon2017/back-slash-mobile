package click.tagit;

/**
 *   * User: Anurag Singh
 *   * Date: 18/8/17
 *   * Time: 8:07 PM
 *
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import click.tagit.categorized.CategorizedFragment;
import click.tagit.grievance.GrievanceFragment;
import click.tagit.uncategorized.UncategorizedFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return GrievanceFragment.newInstance(1);
            case 1:
                return CategorizedFragment.newInstance(1);
            case 2:
                return UncategorizedFragment.newInstance(1);
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "GRIEVANCES";
            case 1:
                return "CATEGORIZED";
            case 2:
                return "UNCATEGORIZED";
        }
        return null;
    }

}
