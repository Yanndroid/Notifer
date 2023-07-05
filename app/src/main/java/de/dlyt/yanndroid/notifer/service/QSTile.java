package de.dlyt.yanndroid.notifer.service;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import de.dlyt.yanndroid.notifer.utils.Preferences;

public class QSTile extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        setTileState(new Preferences(this).isServiceEnabled());
    }

    @Override
    public void onClick() {
        super.onClick();
        Preferences preferences = new Preferences(this);
        boolean newState = !preferences.isServiceEnabled();
        setTileState(newState);
        preferences.setServiceEnabled(newState);
    }

    private void setTileState(boolean checked) {
        Tile tile = getQsTile();
        tile.setState(checked ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

}
