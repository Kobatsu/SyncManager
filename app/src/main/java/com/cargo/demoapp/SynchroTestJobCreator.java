package com.cargo.demoapp;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by leb on 10/08/2017.
 */

public class SynchroTestJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        if (tag.equals(SyncTestJob.TAG)) {
            return new SyncTestJob();
        } else {
            return null;
        }
    }
}
