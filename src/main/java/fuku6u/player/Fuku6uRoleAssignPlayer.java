package fuku6u.player;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class Fuku6uRoleAssignPlayer extends AbstractRoleAssignPlayer {

    public Fuku6uRoleAssignPlayer() {
        setSeerPlayer(new Fuku6u());
        setPossessedPlayer(new Fuku6u());
        setWerewolfPlayer(new Fuku6u());
        setVillagerPlayer(new Fuku6u());
        setBodyguardPlayer(new Fuku6u());
        setMediumPlayer(new Fuku6u());
    }

    @Override
    public String getName() {
        return "Fuku6u";
    }
}
