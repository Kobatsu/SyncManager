package com.cargo.sbpd.sync.networklistener;

import com.cargo.sbpd.model.objects.NetworkSpeed;

/**
 * Created by leb on 02/08/2017.
 */
public interface INetworkSpeedListener {

    void updateUI(NetworkSpeed speed);
}
