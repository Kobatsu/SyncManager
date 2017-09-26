package com.cargo.sbpd.sync.filelisting;

/**
 * Created by Sébastien on 26/08/2017.
 *
 * Interface which if implemented can allow to filter which files should be listed in local or remote servers
 */

public interface ListFilter {
    boolean acceptLocal(String localPath);

    boolean acceptRemote(String remotePath);
}
