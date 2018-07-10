import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class NLTestPlayer implements Player {
    @Override
    public String getName() {
        return "NLTESTPLAYER";
    }

    @Override
    public void update(GameInfo gameInfo) {

    }

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {

    }

    @Override
    public void dayStart() {

    }

    @Override
    public String talk() {
        return null;
    }

    @Override
    public String whisper() {
        return null;
    }

    @Override
    public Agent vote() {
        return null;
    }

    @Override
    public Agent attack() {
        return null;
    }

    @Override
    public Agent divine() {
        return null;
    }

    @Override
    public Agent guard() {
        return null;
    }

    @Override
    public void finish() {

    }
}
